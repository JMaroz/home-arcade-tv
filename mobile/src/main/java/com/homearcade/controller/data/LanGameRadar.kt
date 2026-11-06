package com.homearcade.controller.data

import android.content.Context
import android.net.nsd.NsdManager
import android.net.nsd.NsdServiceInfo
import android.net.wifi.WifiManager
import android.util.Log
import com.homearcade.controller.data.model.DiscoveredGameSession
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import java.net.InetAddress
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.TimeUnit

class LanGameRadar(private val context: Context) {
    private val tag = "LanGameRadar"
    private val serviceType = "_homearcade._tcp."

    private val nsdManager = context.getSystemService(Context.NSD_SERVICE) as? NsdManager
    private var multicastLock: WifiManager.MulticastLock? = null
    private var isScanning = false

    private val sessionMap = ConcurrentHashMap<String, DiscoveredGameSession>()
    private val _sessions = MutableStateFlow<List<DiscoveredGameSession>>(emptyList())
    val sessions: StateFlow<List<DiscoveredGameSession>> = _sessions.asStateFlow()

    private val scope = CoroutineScope(Dispatchers.IO)
    private val httpClient = OkHttpClient.Builder()
        .connectTimeout(1500, TimeUnit.MILLISECONDS)
        .readTimeout(1500, TimeUnit.MILLISECONDS)
        .build()

    private val discoveryListener = object : NsdManager.DiscoveryListener {
        override fun onDiscoveryStarted(regType: String) {
            isScanning = true
            Log.d(tag, "mDNS Discovery started for $regType")
        }

        override fun onServiceFound(serviceInfo: NsdServiceInfo) {
            Log.d(tag, "Service found: ${serviceInfo.serviceName}")
            try {
                nsdManager?.resolveService(serviceInfo, object : NsdManager.ResolveListener {
                    override fun onResolveFailed(serviceInfo: NsdServiceInfo, errorCode: Int) {
                        Log.w(tag, "Resolve failed for ${serviceInfo.serviceName}: $errorCode")
                    }

                    override fun onServiceResolved(resolvedInfo: NsdServiceInfo) {
                        val host = resolvedInfo.host?.hostAddress ?: return
                        val port = resolvedInfo.port
                        val attributes = resolvedInfo.attributes

                        val gameTitle = attributes["game"]?.let { String(it, Charsets.UTF_8) } ?: "HomeArcade Game"
                        val platform = attributes["platform"]?.let { String(it, Charsets.UTF_8) } ?: "arcade"
                        val romId = attributes["rom_id"]?.let { String(it, Charsets.UTF_8).toIntOrNull() } ?: 0
                        val defaultSlot = attributes["default_slot"]?.let { String(it, Charsets.UTF_8).toIntOrNull() } ?: 0

                        val session = DiscoveredGameSession(
                            hostIp = host,
                            port = port,
                            gameTitle = gameTitle,
                            platform = platform,
                            romId = romId,
                            serverName = resolvedInfo.serviceName,
                            defaultSlot = defaultSlot
                        )

                        sessionMap[host] = session
                        _sessions.value = sessionMap.values.toList()
                        Log.i(tag, "Resolved session on $host:$port -> $gameTitle ($platform)")
                    }
                })
            } catch (e: Exception) {
                Log.w(tag, "Error resolving service", e)
            }
        }

        override fun onServiceLost(serviceInfo: NsdServiceInfo) {
            val key = sessionMap.keys.firstOrNull { sessionMap[it]?.serverName == serviceInfo.serviceName }
            if (key != null) {
                sessionMap.remove(key)
                _sessions.value = sessionMap.values.toList()
            }
        }

        override fun onDiscoveryStopped(serviceType: String) {
            isScanning = false
            Log.d(tag, "mDNS Discovery stopped")
        }

        override fun onStartDiscoveryFailed(serviceType: String, errorCode: Int) {
            isScanning = false
            Log.w(tag, "Discovery start failed: $errorCode")
        }

        override fun onStopDiscoveryFailed(serviceType: String, errorCode: Int) {
            isScanning = false
            Log.w(tag, "Discovery stop failed: $errorCode")
        }
    }

    fun startScanning() {
        if (isScanning) return
        try {
            val wifiManager = context.applicationContext.getSystemService(Context.WIFI_SERVICE) as? WifiManager
            multicastLock = wifiManager?.createMulticastLock("HomeArcadeRadarMulticastLock")?.apply {
                setReferenceCounted(true)
                acquire()
            }
            nsdManager?.discoverServices(serviceType, NsdManager.PROTOCOL_DNS_SD, discoveryListener)
        } catch (e: Exception) {
            Log.e(tag, "Failed to start mDNS discovery", e)
        }
    }

    fun stopScanning() {
        if (!isScanning) return
        try {
            nsdManager?.stopServiceDiscovery(discoveryListener)
        } catch (_: Exception) {}
        try {
            if (multicastLock?.isHeld == true) {
                multicastLock?.release()
            }
        } catch (_: Exception) {}
        multicastLock = null
        isScanning = false
    }

    fun probeDirectIp(ip: String, port: Int = 9877, onResult: (DiscoveredGameSession?) -> Unit) {
        scope.launch {
            try {
                val url = "http://$ip:$port/api/status"
                val request = Request.Builder().url(url).build()
                val response = httpClient.newCall(request).execute()
                if (response.isSuccessful) {
                    val bodyStr = response.body?.string() ?: ""
                    val json = JSONObject(bodyStr)
                    val session = DiscoveredGameSession(
                        hostIp = ip,
                        port = port,
                        gameTitle = json.optString("gameTitle", "HomeArcade Game"),
                        platform = json.optString("platform", "arcade"),
                        romId = json.optInt("romId", 0),
                        serverName = "HomeArcade TV ($ip)",
                        defaultSlot = json.optInt("suggestedSlot", 0)
                    )
                    sessionMap[ip] = session
                    _sessions.value = sessionMap.values.toList()
                    onResult(session)
                    return@launch
                }
            } catch (e: Exception) {
                Log.d(tag, "Direct IP probe failed for $ip:$port: ${e.message}")
            }
            onResult(null)
        }
    }
}
