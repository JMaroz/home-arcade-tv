package com.homearcade.tv

import android.app.Application
import coil3.ImageLoader
import coil3.PlatformContext
import coil3.SingletonImageLoader
import coil3.disk.DiskCache
import coil3.memory.MemoryCache
import coil3.network.okhttp.OkHttpNetworkFetcherFactory
import coil3.request.crossfade
import coil3.util.DebugLogger
import com.homearcade.tv.data.local.AppPreferences
import com.homearcade.tv.data.repository.ProfileRepository
import com.homearcade.tv.data.repository.RomRepository
import okhttp3.OkHttpClient
import okio.Path.Companion.toOkioPath
import java.util.concurrent.TimeUnit

class HomeArcadeTvApp : Application(), SingletonImageLoader.Factory {

    lateinit var preferences: AppPreferences
        private set

    lateinit var romRepository: RomRepository
        private set

    lateinit var profileRepository: ProfileRepository
        private set

    private val coilOkHttpClient by lazy {
        OkHttpClient.Builder()
            .connectTimeout(15, TimeUnit.SECONDS)
            .readTimeout(15, TimeUnit.SECONDS)
            .followRedirects(true)
            .followSslRedirects(true)
            .addInterceptor { chain ->
                val request = chain.request().newBuilder()
                    .header("User-Agent", "HomeArcadeTV/1.0 (Android TV)")
                    .build()
                chain.proceed(request)
            }
            .build()
    }

    override fun onCreate() {
        super.onCreate()

        preferences = AppPreferences(this)
        romRepository = RomRepository(preferences)
        profileRepository = ProfileRepository(preferences)

        // Initialize Coil singleton explicitly
        SingletonImageLoader.setSafe(this)
    }

    override fun newImageLoader(context: PlatformContext): ImageLoader {
        return ImageLoader.Builder(context)
            .components {
                add(
                    OkHttpNetworkFetcherFactory(
                        callFactory = { coilOkHttpClient }
                    )
                )
            }
            .memoryCache {
                MemoryCache.Builder()
                    .maxSizePercent(context, 0.25)
                    .build()
            }
            .diskCache {
                DiskCache.Builder()
                    .directory(cacheDir.resolve("rom_art_cache").toOkioPath())
                    .maxSizeBytes(150L * 1024 * 1024) // 150MB disk cache for box art
                    .build()
            }
            .crossfade(true)
            .apply {
                if (BuildConfig.DEBUG) {
                    logger(DebugLogger())
                }
            }
            .build()
    }
}
