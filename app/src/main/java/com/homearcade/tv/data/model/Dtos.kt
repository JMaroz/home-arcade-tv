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
    val size: Long? = null,
    val scrapeStatus: String? = null,
    val raGameId: Int? = null,
    val mimeType: String? = null,
    val createdAt: Long = 0L
) {
    fun getFormattedSize(): String? {
        val s = size ?: return null
        if (s <= 0) return null
        val kb = s / 1024.0
        val mb = kb / 1024.0
        val gb = mb / 1024.0
        return when {
            gb >= 1.0 -> String.format(java.util.Locale.US, "%.1f GB", gb)
            mb >= 1.0 -> String.format(java.util.Locale.US, "%.1f MB", mb)
            else -> String.format(java.util.Locale.US, "%.0f KB", kb)
        }
    }

    fun getFileExtension(): String? {
        val name = fileName ?: originalName ?: return null
        val dot = name.lastIndexOf('.')
        return if (dot != -1 && dot < name.length - 1) {
            name.substring(dot + 1).uppercase()
        } else null
    }
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

@Serializable
data class HltbResponseDto(
    val found: Boolean = false,
    val mainStory: Int? = null,
    val mainExtra: Int? = null,
    val completionist: Int? = null,
    val hltbTitle: String? = null
) {
    fun formatStory(): String? = formatMinutes(mainStory)
    fun formatExtra(): String? = formatMinutes(mainExtra)
    fun formatCompletionist(): String? = formatMinutes(completionist)

    private fun formatMinutes(minutes: Int?): String? {
        if (minutes == null || minutes <= 0) return null
        val hours = minutes / 60
        val rem = minutes % 60
        return if (hours > 0) {
            if (rem >= 30) "${hours}h 30m" else "${hours}h"
        } else {
            "${rem}m"
        }
    }
}

