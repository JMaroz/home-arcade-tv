package com.homearcade.tv.data.repository

import com.homearcade.tv.data.api.ApiClient
import com.homearcade.tv.data.local.AppPreferences
import com.homearcade.tv.data.model.ProfileDto
import kotlinx.coroutines.flow.firstOrNull

class ProfileRepository(private val preferences: AppPreferences) {

    suspend fun getProfiles(): Result<List<ProfileDto>> {
        val baseUrl = preferences.serverUrl.firstOrNull()
            ?: return Result.failure(IllegalStateException("Server URL non configurato."))

        return try {
            val api = ApiClient.create(baseUrl)
            val list = api.getProfiles()
            if (list.isEmpty()) {
                Result.success(listOf(ProfileDto(id = 1, name = "Player 1", color = "#EC4899")))
            } else {
                Result.success(list)
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun selectProfile(profile: ProfileDto) {
        preferences.setActiveProfile(
            id = profile.id,
            name = profile.name,
            color = profile.color
        )
    }
}
