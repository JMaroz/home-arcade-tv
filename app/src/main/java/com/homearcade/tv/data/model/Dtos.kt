package com.homearcade.tv.data.model

import kotlinx.serialization.Serializable

@Serializable
data class RomDto(
    val id: Int,
    val title: String,
    val system: String,
    val slug: String? = null,
    val originalName: String? = null,
    val fileName: String? = null,
    val artUrl: String? = null,
    val wheelArtUrl: String? = null,
    val videoUrl: String? = null,
    val favorite: Boolean = false,
    val rating: Int = 0,
    val lastPlayed: Long = 0L,
    val playCount: Int = 0,
    val minutesPlayed: Int = 0,
    val playStatus: String? = null,
    val discGroup: String? = null,
    val discNumber: Int? = null,
    val isPlaylist: Boolean = false,
    val parentM3uId: Int? = null,
    val description: String? = null,
    val releaseYear: Int? = null,
    val developer: String? = null,
    val publisher: String? = null,
    val genre: String? = null,
    val players: String? = null,
    val romHash: String? = null,
    val communityScore: Int? = null,
    val createdAt: Long = 0L
) {
    fun getFullArtUrl(baseUrl: String?): String? {
        if (artUrl.isNullOrEmpty()) {
            // Default fallback to server art endpoint by id
            return if (!baseUrl.isNullOrEmpty()) {
                val cleanBase = baseUrl.trim().removeSuffix("/")
                "$cleanBase/api/roms/$id/art"
            } else null
        }
        if (baseUrl.isNullOrEmpty()) return artUrl
        val cleanBase = baseUrl.trim().removeSuffix("/")
        return if (artUrl.startsWith("http://") || artUrl.startsWith("https://")) {
            artUrl
        } else if (artUrl.startsWith("/")) {
            "$cleanBase$artUrl"
        } else {
            "$cleanBase/$artUrl"
        }
    }

    fun getFullWheelLogoUrl(baseUrl: String?): String? {
        if (wheelArtUrl.isNullOrEmpty() || baseUrl.isNullOrEmpty()) return wheelArtUrl
        val cleanBase = baseUrl.trim().removeSuffix("/")
        return if (wheelArtUrl.startsWith("http://") || wheelArtUrl.startsWith("https://")) {
            wheelArtUrl
        } else if (wheelArtUrl.startsWith("/")) {
            "$cleanBase$wheelArtUrl"
        } else {
            "$cleanBase/$wheelArtUrl"
        }
    }
}

@Serializable
data class RomsResponseDto(
    val roms: List<RomDto> = emptyList(),
    val total: Int = 0,
    val hasMore: Boolean = false
)

@Serializable
data class ProfileDto(
    val id: Int,
    val name: String,
    val color: String = "#EC4899",
    val createdAt: Long = 0L
)

@Serializable
data class SaveStateDto(
    val slot: Int = 0,
    val timestamp: Long = 0L,
    val screenshotUrl: String? = null
)

@Serializable
data class SetupStatusDto(
    val isComplete: Boolean = true,
    val arch: String? = null,
    val totalCores: Int = 0,
    val installedCores: Int = 0
)

@Serializable
data class HealthStatusDto(
    val status: String = "ok"
)

@Serializable
data class FavoriteToggleRequestDto(
    val favorite: Boolean
)
