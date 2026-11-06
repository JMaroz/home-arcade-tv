package com.homearcade.controller.data

import android.util.Log
import com.homearcade.controller.data.model.ConnectionStatus
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okhttp3.WebSocket
import okhttp3.WebSocketListener
import okio.ByteString.Companion.toByteString
import org.json.JSONObject
import java.util.concurrent.TimeUnit

class GamepadWebSocketClient {
    private val tag = "GamepadWsClient"

    private val httpClient = OkHttpClient.Builder()
        .connectTimeout(3000, TimeUnit.MILLISECONDS)
        .readTimeout(0, TimeUnit.MILLISECONDS) // Indefinite for WebSocket
        .build()

    private var webSocket: WebSocket? = null

    private val _status = MutableStateFlow(ConnectionStatus.DISCONNECTED)
    val status: StateFlow<ConnectionStatus> = _status.asStateFlow()

    private var currentSlot: Int = 1
    private var currentHost: String = ""
    private var currentPort: Int = 9877

    fun connect(host: String, port: Int = 9877, slot: Int = 1) {
        disconnect()

        currentHost = host
        currentPort = port
        currentSlot = slot
        _status.value = ConnectionStatus.CONNECTING

        val url = "ws://$host:$port/ws/gamepad?slot=$slot"
        val request = Request.Builder().url(url).build()

        webSocket = httpClient.newWebSocket(request, object : WebSocketListener() {
            override fun onOpen(webSocket: WebSocket, response: Response) {
                Log.i(tag, "WebSocket connected to $url (Slot P$slot)")
                _status.value = ConnectionStatus.CONNECTED
            }

            override fun onMessage(webSocket: WebSocket, text: String) {
                Log.d(tag, "Received WS text: $text")
            }

            override fun onClosing(webSocket: WebSocket, code: Int, reason: String) {
                Log.i(tag, "WebSocket closing: $code / $reason")
                webSocket.close(1000, null)
                _status.value = ConnectionStatus.DISCONNECTED
            }

            override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
                Log.e(tag, "WebSocket failure: ${t.message}", t)
                _status.value = ConnectionStatus.ERROR
            }

            override fun onClosed(webSocket: WebSocket, code: Int, reason: String) {
                Log.i(tag, "WebSocket closed")
                _status.value = ConnectionStatus.DISCONNECTED
            }
        })
    }

    fun sendInput(mask: Int, axisX: Float, axisY: Float) {
        val ws = webSocket ?: return
        if (_status.value != ConnectionStatus.CONNECTED) return

        // Send compact 6-byte binary frame for sub-millisecond transfer:
        // [0x48(magic), slot(1B), maskLow(1B), maskHigh(1B), ax(1B signed), ay(1B signed)]
        val axByte = (axisX * 127).toInt().coerceIn(-128, 127).toByte()
        val ayByte = (axisY * 127).toInt().coerceIn(-128, 127).toByte()

        val binaryFrame = byteArrayOf(
            0x48.toByte(),
            currentSlot.toByte(),
            (mask and 0xFF).toByte(),
            ((mask shr 8) and 0xFF).toByte(),
            axByte,
            ayByte
        )
        ws.send(binaryFrame.toByteString())
    }

    fun disconnect() {
        try {
            webSocket?.close(1000, "User disconnected")
        } catch (_: Exception) {}
        webSocket = null
        _status.value = ConnectionStatus.DISCONNECTED
    }
}
