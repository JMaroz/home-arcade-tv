package com.homearcade.tv.data.api

import io.ktor.client.request.get
import io.ktor.http.HttpStatusCode
import com.homearcade.tv.data.model.HealthStatusDto
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withContext
import java.net.Inet4Address
import java.net.NetworkInterface

sealed interface DiscoveryState {
    data object Idle : DiscoveryState
    data class Searching(val currentTarget: String, val scanned: Int, val total: Int) : DiscoveryState
    data class Found(val url: String, val status: HealthStatusDto) : DiscoveryState
    data class Failed(val message: String) : DiscoveryState
}

class ServerDiscoveryManager {

    suspend fun testConnection(baseUrl: String): Result<HealthStatusDto> = withContext(Dispatchers.IO) {
        try {
            val api = ApiClient.create(baseUrl)
            val status = api.checkHealth()
            if (status.status == "ok" || status.status == "starting") {
                Result.success(status)
            } else {
                Result.failure(IllegalStateException("Il server ha risposto ma lo stato non è 'ok'."))
            }
        } catch (e: Exception) {
            // Also attempt fallback check on /api/setup/status
            try {
                val api = ApiClient.create(baseUrl)
                api.getSetupStatus()
                Result.success(HealthStatusDto("ok"))
            } catch (fallbackEx: Exception) {
                android.util.Log.w("ServerDiscovery", "Connection test failed for $baseUrl: ${e.message}")
                Result.failure(e)
            }
        }
    }

    fun discoverServer(): Flow<DiscoveryState> = flow {
        emit(DiscoveryState.Searching("Inizializzazione scansione di rete...", 0, 0))

        // 1. Check primary candidates (mDNS and common hostnames across standard ports 5000 and 9876)
        val priorityCandidates = listOf(
            "http://homeassistant.local:5000",
            "http://homeassistant.local:9876",
            "http://homeassistant:5000",
            "http://homeassistant:9876",
            "http://127.0.0.1:5000",
            "http://127.0.0.1:9876"
        )

        for (candidate in priorityCandidates) {
            emit(DiscoveryState.Searching(candidate, 0, priorityCandidates.size))
            if (probeUrl(candidate)) {
                val testResult = testConnection(candidate)
                if (testResult.isSuccess) {
                    emit(DiscoveryState.Found(candidate, testResult.getOrThrow()))
                    return@flow
                }
            }
        }

        // 2. Discover local subnet IP candidates
        val localIps = getLocalIpSubnetCandidates()
        if (localIps.isEmpty()) {
            emit(DiscoveryState.Failed("Nessun indirizzo IP di rete locale valido rilevato sulla TV."))
            return@flow
        }

        val total = localIps.size * 2 // 2 ports to test per IP (5000 and 9876)
        var scannedCount = 0

        // Process in batches of 15 concurrent probes for quick LAN scan
        val batches = localIps.chunked(15)
        for (batch in batches) {
            val foundMatch = withContext(Dispatchers.IO) {
                val deferredList = batch.flatMap { ip ->
                    listOf("http://$ip:5000", "http://$ip:9876").map { url ->
                        async {
                            val isAlive = probeUrl(url)
                            if (isAlive) url else null
                        }
                    }
                }
                val results = deferredList.awaitAll()
                val aliveUrls = results.filterNotNull()
                for (url in aliveUrls) {
                    val fullTest = testConnection(url)
                    if (fullTest.isSuccess) {
                        return@withContext Pair(url, fullTest.getOrThrow())
                    }
                }
                null
            }

            scannedCount += batch.size * 2
            emit(DiscoveryState.Searching("Scansione IP locali (${scannedCount}/$total)...", scannedCount, total))

            if (foundMatch != null) {
                emit(DiscoveryState.Found(foundMatch.first, foundMatch.second))
                return@flow
            }
        }

        emit(DiscoveryState.Failed("Nessun server HomeArcade rilevato sulla rete locale."))
    }.flowOn(Dispatchers.IO)

    private suspend fun probeUrl(url: String): Boolean {
        return try {
            val response = ApiClient.probeHttpClient.get("$url/api/health")
            response.status == HttpStatusCode.OK
        } catch (_: Exception) {
            false
        }
    }

    private fun getLocalIpSubnetCandidates(): List<String> {
        val candidates = mutableListOf<String>()
        try {
            val interfaces = NetworkInterface.getNetworkInterfaces().toList()
            for (iface in interfaces) {
                if (iface.isLoopback || !iface.isUp) continue
                for (addr in iface.inetAddresses.toList()) {
                    if (addr is Inet4Address && !addr.isLoopbackAddress) {
                        val hostAddress = addr.hostAddress ?: continue
                        val parts = hostAddress.split(".")
                        if (parts.size == 4) {
                            val prefix = "${parts[0]}.${parts[1]}.${parts[2]}"
                            val commonHosts = listOf(1, 67, 50, 2, 3, 4, 5, 10, 20, 100, 101, 150, 200)
                            for (host in commonHosts) {
                                val ip = "$prefix.$host"
                                if (!candidates.contains(ip)) {
                                    candidates.add(ip)
                                }
                            }
                            for (host in 1..254) {
                                val ip = "$prefix.$host"
                                if (!candidates.contains(ip)) {
                                    candidates.add(ip)
                                }
                            }
                        }
                    }
                }
            }
        } catch (_: Exception) {
            // fallback
        }
        return candidates
    }
}
