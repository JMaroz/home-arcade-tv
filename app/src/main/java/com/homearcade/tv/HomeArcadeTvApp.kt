package com.homearcade.tv

import android.app.Application
import coil.Coil
import coil.ImageLoader
import coil.ImageLoaderFactory
import coil.disk.DiskCache
import coil.memory.MemoryCache
import coil.util.DebugLogger
import com.homearcade.tv.data.local.AppPreferences
import com.homearcade.tv.data.repository.ProfileRepository
import com.homearcade.tv.data.repository.RomRepository
import okhttp3.OkHttpClient
import java.util.concurrent.TimeUnit

class HomeArcadeTvApp : Application(), ImageLoaderFactory {

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
        Coil.setImageLoader(this)
    }

    override fun newImageLoader(): ImageLoader {
        return ImageLoader.Builder(this)
            .okHttpClient(coilOkHttpClient)
            .memoryCache {
                MemoryCache.Builder(this)
                    .maxSizePercent(0.25)
                    .build()
            }
            .diskCache {
                DiskCache.Builder()
                    .directory(cacheDir.resolve("rom_art_cache"))
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
