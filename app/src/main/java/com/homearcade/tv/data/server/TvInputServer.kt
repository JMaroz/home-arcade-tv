package com.homearcade.tv.data.server

import android.content.Context
import android.view.InputDevice
import android.net.nsd.NsdManager
import android.net.nsd.NsdServiceInfo
import android.net.wifi.WifiManager
import android.util.Base64
import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStream
import java.io.InputStreamReader
import java.io.OutputStream
import java.net.ServerSocket
import java.net.Socket
import java.security.MessageDigest
import java.util.concurrent.ConcurrentHashMap

/**
 * Embedded ultra-low latency WebSocket & HTTP server for HomeArcade TV.
 * - Serves web_controller.html on GET / for guest web players via QR Code.
 * - Serves /api/status returning current game and connected slots.
 * - Handles WebSocket connections on /ws/gamepad receiving real-time input frames.
 * - Registers local mDNS service (_homearcade._tcp) via NsdManager.
 */
class TvInputServer(
    private val context: Context,
    private val gameTitle: String,
    private val platform: String,
    private val romId: Int,
    private val onInputReceived: (slot: Int, mask: Int, ax: Float, ay: Float) -> Unit,
    private val onSlotChanged: (slot: Int, isConnected: Boolean) -> Unit
) {
    private val tag = "TvInputServer"
    private val serverPort = 9877

    private var serverSocket: ServerSocket? = null
    private var serverJob: Job? = null
    private val scope = CoroutineScope(Dispatchers.IO)

    private val connectedSlots = ConcurrentHashMap<Int, Socket>()
    private var multicastLock: WifiManager.MulticastLock? = null
    private var nsdManager: NsdManager? = null
    private var registrationListener: NsdManager.RegistrationListener? = null

    private var cachedWebController: ByteArray? = null

    init {
        try {
            val assetStream = context.assets.open("web_controller.html")
            cachedWebController = assetStream.readBytes()
            assetStream.close()
        } catch (e: Exception) {
            Log.e(tag, "Failed to load web_controller.html from assets", e)
        }
    }

    fun start() {
        if (serverJob != null && serverJob?.isActive == true) return

        serverJob = scope.launch {
            try {
                serverSocket = ServerSocket(serverPort)
                Log.i(tag, "HomeArcade TV Input Server started on port $serverPort")

                registerMdns()

                while (isActive) {
                    val socket = serverSocket?.accept() ?: break
                    launch {
                        handleClient(socket)
                    }
                }
            } catch (e: Exception) {
                if (isActive) {
                    Log.e(tag, "Error in server socket loop", e)
                }
            }
        }
    }

    fun stop() {
        unregisterMdns()
        try {
            serverSocket?.close()
        } catch (_: Exception) {}
        serverSocket = null

        connectedSlots.forEach { (slot, socket) ->
            try {
                socket.close()
            } catch (_: Exception) {}
            onSlotChanged(slot, false)
        }
        connectedSlots.clear()

        serverJob?.cancel()
        serverJob = null
        Log.i(tag, "HomeArcade TV Input Server stopped")
    }

    private suspend fun handleClient(socket: Socket) = withContext(Dispatchers.IO) {
        var assignedSlot = -1
        try {
            socket.tcpNoDelay = true
            val input = socket.getInputStream()
            val output = socket.getOutputStream()
            val reader = BufferedReader(InputStreamReader(input, Charsets.UTF_8))

            // Read HTTP request line
            val requestLine = reader.readLine() ?: return@withContext
            val parts = requestLine.split(" ")
            if (parts.size < 2) return@withContext

            val method = parts[0]
            val path = parts[1]

            // Read HTTP headers
            val headers = mutableMapOf<String, String>()
            var line: String? = reader.readLine()
            while (!line.isNullOrEmpty()) {
                val colonIdx = line.indexOf(':')
                if (colonIdx > 0) {
                    val key = line.substring(0, colonIdx).trim().lowercase()
                    val value = line.substring(colonIdx + 1).trim()
                    headers[key] = value
                }
                line = reader.readLine()
            }

            // Check if WebSocket upgrade
            val isWs = headers["upgrade"]?.equals("websocket", ignoreCase = true) == true
            if (isWs && path.startsWith("/ws/gamepad")) {
                val requestedSlot = extractSlotFromPath(path)
                assignedSlot = allocateSlot(requestedSlot, socket)

                val clientKey = headers["sec-websocket-key"] ?: ""
                val acceptKey = computeAcceptKey(clientKey)

                val response = "HTTP/1.1 101 Switching Protocols\r\n" +
                        "Upgrade: websocket\r\n" +
                        "Connection: Upgrade\r\n" +
                        "Sec-WebSocket-Accept: $acceptKey\r\n\r\n"
                output.write(response.toByteArray(Charsets.ISO_8859_1))
                output.flush()

                onSlotChanged(assignedSlot, true)
                readWebSocketFrames(input, output, assignedSlot)
            } else if (method == "GET") {
                handleHttpRequest(path, output)
            }
        } catch (e: Exception) {
            Log.d(tag, "Client connection ended: ${e.message}")
        } finally {
            if (assignedSlot >= 0) {
                connectedSlots.remove(assignedSlot)
                onSlotChanged(assignedSlot, false)
            }
            try {
                socket.close()
            } catch (_: Exception) {}
        }
    }

    private fun handleHttpRequest(path: String, output: OutputStream) {
        val cleanPath = path.substringBefore("?")
        when (cleanPath) {
            "/", "/controller", "/index.html" -> {
                val body = cachedWebController ?: "<h3>HomeArcade Controller Offline</h3>".toByteArray(Charsets.UTF_8)
                val response = "HTTP/1.1 200 OK\r\n" +
                        "Content-Type: text/html; charset=UTF-8\r\n" +
                        "Content-Length: ${body.size}\r\n" +
                        "Connection: close\r\n\r\n"
                output.write(response.toByteArray(Charsets.ISO_8859_1))
                output.write(body)
                output.flush()
            }
            "/api/status" -> {
                val hasController = hasPhysicalGamepad()
                val json = JSONObject().apply {
                    put("gameTitle", gameTitle)
                    put("platform", platform)
                    put("romId", romId)
                    put("hasPhysicalGamepad", hasController)
                    put("suggestedSlot", if (hasController) 1 else 0)
                    put("activeSlots", connectedSlots.keys.toList())
                }.toString()
                val body = json.toByteArray(Charsets.UTF_8)
                val response = "HTTP/1.1 200 OK\r\n" +
                        "Content-Type: application/json; charset=UTF-8\r\n" +
                        "Content-Length: ${body.size}\r\n" +
                        "Access-Control-Allow-Origin: *\r\n" +
                        "Connection: close\r\n\r\n"
                output.write(response.toByteArray(Charsets.ISO_8859_1))
                output.write(body)
                output.flush()
            }
            else -> {
                val notFound = "HTTP/1.1 404 Not Found\r\nContent-Length: 0\r\nConnection: close\r\n\r\n"
                output.write(notFound.toByteArray(Charsets.ISO_8859_1))
                output.flush()
            }
        }
    }

    private fun hasPhysicalGamepad(): Boolean {
        try {
            val deviceIds = InputDevice.getDeviceIds()
            for (id in deviceIds) {
                val device = InputDevice.getDevice(id) ?: continue
                val sources = device.sources
                if ((sources and InputDevice.SOURCE_GAMEPAD) == InputDevice.SOURCE_GAMEPAD ||
                    (sources and InputDevice.SOURCE_JOYSTICK) == InputDevice.SOURCE_JOYSTICK
                ) {
                    if (!device.isVirtual && device.name?.contains("remote", ignoreCase = true) != true) {
                        return true
                    }
                }
            }
        } catch (_: Exception) {}
        return false
    }

    private fun extractSlotFromPath(path: String): Int {
        val query = path.substringAfter("?", "")
        val params = query.split("&").associate {
            val p = it.split("=")
            if (p.size == 2) p[0] to p[1] else "" to ""
        }
        val slotParam = params["slot"] ?: params["player"]
        val parsed = slotParam?.toIntOrNull()
        if (parsed != null && parsed in 0..3) {
            return parsed
        }
        // Slots: 0 is Player 1, 1 is Player 2, 2 is Player 3, 3 is Player 4
        // Default to P1 if no physical gamepad, else P2
        return if (hasPhysicalGamepad()) 1 else 0
    }

    private fun allocateSlot(requestedSlot: Int, socket: Socket): Int {
        if (!connectedSlots.containsKey(requestedSlot)) {
            connectedSlots[requestedSlot] = socket
            return requestedSlot
        }
        for (candidate in 0..3) {
            if (!connectedSlots.containsKey(candidate)) {
                connectedSlots[candidate] = socket
                return candidate
            }
        }
        connectedSlots[requestedSlot] = socket
        return requestedSlot
    }

    private fun readWebSocketFrames(input: InputStream, output: OutputStream, slot: Int) {
        while (true) {
            val b0 = input.read()
            if (b0 == -1) break
            val opcode = b0 and 0x0F
            if (opcode == 0x08) { // Connection Close
                break
            }

            val b1 = input.read()
            if (b1 == -1) break
            val isMasked = (b1 and 0x80) != 0
            var length = (b1 and 0x7F).toLong()

            if (length == 126L) {
                val hi = input.read()
                val lo = input.read()
                if (hi == -1 || lo == -1) break
                length = ((hi shl 8) or lo).toLong()
            } else if (length == 127L) {
                var len = 0L
                for (i in 0 until 8) {
                    val b = input.read()
                    if (b == -1) return
                    len = (len shl 8) or (b.toLong() and 0xFF)
                }
                length = len
            }

            val mask = ByteArray(4)
            if (isMasked) {
                readFully(input, mask)
            }

            val payload = ByteArray(length.toInt())
            readFully(input, payload)

            if (isMasked) {
                for (i in payload.indices) {
                    payload[i] = (payload[i].toInt() xor mask[i % 4].toInt()).toByte()
                }
            }

            when (opcode) {
                0x01 -> { // Text Frame (JSON)
                    val text = String(payload, Charsets.UTF_8)
                    handleTextMessage(slot, text)
                }
                0x02 -> { // Binary Frame
                    handleBinaryMessage(slot, payload)
                }
                0x09 -> { // Ping -> Send Pong
                    output.write(0x8A) // FIN + Pong
                    output.write(payload.size)
                    output.write(payload)
                    output.flush()
                }
            }
        }
    }

    private fun handleTextMessage(defaultSlot: Int, text: String) {
        try {
            val json = JSONObject(text)
            val slot = json.optInt("slot", defaultSlot)
            val mask = json.optInt("mask", 0)
            val ax = json.optDouble("ax", 0.0).toFloat()
            val ay = json.optDouble("ay", 0.0).toFloat()
            onInputReceived(slot, mask, ax, ay)
        } catch (_: Exception) {}
    }

    private fun handleBinaryMessage(defaultSlot: Int, data: ByteArray) {
        // Compact binary format: [magic(0x48), slot(1B), maskLow(1B), maskHigh(1B), ax(1B signed), ay(1B signed)]
        if (data.size >= 6 && data[0] == 0x48.toByte()) {
            val slot = data[1].toInt() and 0xFF
            val mask = (data[2].toInt() and 0xFF) or ((data[3].toInt() and 0xFF) shl 8)
            val ax = data[4].toInt() / 127f
            val ay = data[5].toInt() / 127f
            onInputReceived(slot, mask, ax, ay)
        }
    }

    private fun readFully(input: InputStream, buffer: ByteArray) {
        var offset = 0
        while (offset < buffer.size) {
            val read = input.read(buffer, offset, buffer.size - offset)
            if (read == -1) break
            offset += read
        }
    }

    private fun computeAcceptKey(clientKey: String): String {
        val magic = "258EAFA5-E914-47DA-95CA-C5AB0DC85B11"
        val sha1 = MessageDigest.getInstance("SHA-1").digest((clientKey.trim() + magic).toByteArray(Charsets.ISO_8859_1))
        return Base64.encodeToString(sha1, Base64.NO_WRAP)
    }

    private fun registerMdns() {
        try {
            val wifiManager = context.applicationContext.getSystemService(Context.WIFI_SERVICE) as? WifiManager
            multicastLock = wifiManager?.createMulticastLock("HomeArcadeTvMulticastLock")?.apply {
                setReferenceCounted(true)
                acquire()
            }

            nsdManager = context.getSystemService(Context.NSD_SERVICE) as? NsdManager
            val serviceInfo = NsdServiceInfo().apply {
                serviceName = "HomeArcade TV"
                serviceType = "_homearcade._tcp"
                port = serverPort
                setAttribute("game", gameTitle)
                setAttribute("platform", platform)
                setAttribute("rom_id", romId.toString())
                setAttribute("default_slot", if (hasPhysicalGamepad()) "1" else "0")
                setAttribute("version", "1.0")
            }

            registrationListener = object : NsdManager.RegistrationListener {
                override fun onServiceRegistered(serviceInfo: NsdServiceInfo) {
                    Log.i(tag, "mDNS Service registered: ${serviceInfo.serviceName}")
                }
                override fun onRegistrationFailed(serviceInfo: NsdServiceInfo, errorCode: Int) {
                    Log.w(tag, "mDNS Registration failed: $errorCode")
                }
                override fun onServiceUnregistered(serviceInfo: NsdServiceInfo) {
                    Log.i(tag, "mDNS Service unregistered")
                }
                override fun onUnregistrationFailed(serviceInfo: NsdServiceInfo, errorCode: Int) {
                    Log.w(tag, "mDNS Unregistration failed: $errorCode")
                }
            }

            nsdManager?.registerService(serviceInfo, NsdManager.PROTOCOL_DNS_SD, registrationListener)
        } catch (e: Exception) {
            Log.w(tag, "mDNS registration skipped or failed", e)
        }
    }

    private fun unregisterMdns() {
        try {
            registrationListener?.let {
                nsdManager?.unregisterService(it)
            }
        } catch (_: Exception) {}
        registrationListener = null

        try {
            if (multicastLock?.isHeld == true) {
                multicastLock?.release()
            }
        } catch (_: Exception) {}
        multicastLock = null
    }
}
