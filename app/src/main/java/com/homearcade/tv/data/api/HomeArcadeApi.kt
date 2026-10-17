package com.homearcade.tv.data.api

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import io.ktor.client.request.patch
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType
import com.homearcade.tv.data.model.FavoriteToggleRequestDto
import com.homearcade.tv.data.model.HealthStatusDto
import com.homearcade.tv.data.model.ProfileDto
import com.homearcade.tv.data.model.RomDto
import com.homearcade.tv.data.model.RomsResponseDto
import com.homearcade.tv.data.model.SaveStateDto
import com.homearcade.tv.data.model.SetupStatusDto

class HomeArcadeApi(
    private val client: HttpClient,
    val baseUrl: String
) {
    suspend fun checkHealth(): HealthStatusDto {
        return client.get("$baseUrl/api/health").body()
    }

    suspend fun getSetupStatus(): SetupStatusDto {
        return client.get("$baseUrl/api/setup/status").body()
    }

    suspend fun getRoms(excludeChildren: Boolean = true): RomsResponseDto {
        return client.get("$baseUrl/api/roms") {
            parameter("exclude_children", excludeChildren.toString())
        }.body()
    }

    suspend fun getRomDetails(id: Int): RomDto {
        return client.get("$baseUrl/api/roms/$id").body()
    }

    suspend fun getDiscs(id: Int): List<RomDto> {
        return client.get("$baseUrl/api/roms/$id/discs").body()
    }

    suspend fun getProfiles(): List<ProfileDto> {
        return client.get("$baseUrl/api/profiles").body()
    }

    suspend fun getSaveStates(id: Int): List<SaveStateDto> {
        return client.get("$baseUrl/api/roms/$id/save-states").body()
    }

    suspend fun toggleFavorite(id: Int, isFavorite: Boolean): RomDto {
        return client.patch("$baseUrl/api/roms/$id/favorite") {
            contentType(ContentType.Application.Json)
            setBody(FavoriteToggleRequestDto(favorite = isFavorite))
        }.body()
    }

    fun getStreamPlayerUrl(romId: Int, profileId: Int = 1): String {
        return "$baseUrl/api/roms/$romId/stream-player?profile=$profileId"
    }

    fun getWasmPlayerUrl(romId: Int, profileId: Int = 1): String {
        return "$baseUrl/api/roms/$romId/player?profile=$profileId"
    }
}
