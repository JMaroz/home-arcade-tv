package com.homearcade.tv.data.api

import io.ktor.client.HttpClient
import io.ktor.client.engine.okhttp.OkHttp
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logger
import io.ktor.client.plugins.logging.Logging
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json
import okhttp3.OkHttpClient
import java.util.concurrent.TimeUnit

object ApiClient {

    val httpClient: HttpClient by lazy {
        HttpClient(OkHttp) {
            engine {
                preconfigured = OkHttpClient.Builder()
                    .connectTimeout(15, TimeUnit.SECONDS)
                    .readTimeout(20, TimeUnit.SECONDS)
                    .writeTimeout(20, TimeUnit.SECONDS)
                    .retryOnConnectionFailure(true)
                    .build()
            }

            install(ContentNegotiation) {
                json(Json {
                    ignoreUnknownKeys = true
                    isLenient = true
                    coerceInputValues = true
                    encodeDefaults = true
                })
            }

            install(HttpTimeout) {
                requestTimeoutMillis = 20_000
                connectTimeoutMillis = 12_000
                socketTimeoutMillis = 20_000
            }

            // Minimal logging on Android TV to avoid main thread latency
            install(Logging) {
                logger = object : Logger {
                    override fun log(message: String) {
                        android.util.Log.d("HomeArcadeHttp", message)
                    }
                }
                level = LogLevel.NONE
            }
        }
    }

    fun create(baseUrl: String): HomeArcadeApi {
        val sanitizedUrl = if (baseUrl.endsWith("/")) baseUrl.removeSuffix("/") else baseUrl
        return HomeArcadeApi(httpClient, sanitizedUrl)
    }
}
