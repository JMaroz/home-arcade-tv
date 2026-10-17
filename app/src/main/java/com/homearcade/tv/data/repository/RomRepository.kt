package com.homearcade.tv.data.repository

import com.homearcade.tv.data.api.ApiClient
import com.homearcade.tv.data.local.AppPreferences
import com.homearcade.tv.data.model.RomDto
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map

class RomRepository(private val preferences: AppPreferences) {

    private val _romsCache = MutableStateFlow<List<RomDto>>(emptyList())
    val romsCache: Flow<List<RomDto>> = _romsCache.asStateFlow()

    suspend fun fetchRoms(forceRefresh: Boolean = false): Result<List<RomDto>> {
        if (!forceRefresh && _romsCache.value.isNotEmpty()) {
            return Result.success(_romsCache.value)
        }

        val baseUrl = preferences.serverUrl.firstOrNull()
            ?: return Result.failure(IllegalStateException("Server URL non configurato."))

        return try {
            val api = ApiClient.create(baseUrl)
            val response = api.getRoms(excludeChildren = true)
            _romsCache.value = response.roms
            Result.success(response.roms)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getRomDetails(id: Int, forceRefresh: Boolean = false): Result<RomDto> {
        val cached = _romsCache.value.find { it.id == id }
        if (!forceRefresh && cached != null && !cached.description.isNullOrEmpty()) {
            return Result.success(cached)
        }

        val baseUrl = preferences.serverUrl.firstOrNull()
            ?: return Result.failure(IllegalStateException("Server URL non configurato."))

        return try {
            val api = ApiClient.create(baseUrl)
            val rom = api.getRomDetails(id)
            // Update in-memory cache with full details
            _romsCache.value = _romsCache.value.map { if (it.id == id) rom else it }
            Result.success(rom)
        } catch (e: Exception) {
            android.util.Log.e("RomRepository", "Failed to fetch ROM details for id $id: ${e.message}", e)
            if (cached != null) Result.success(cached) else Result.failure(e)
        }
    }

    suspend fun getHltb(id: Int): Result<com.homearcade.tv.data.model.HltbResponseDto> {
        val baseUrl = preferences.serverUrl.firstOrNull()
            ?: return Result.failure(IllegalStateException("Server URL non configurato."))

        return try {
            val api = ApiClient.create(baseUrl)
            val hltb = api.getHltb(id)
            Result.success(hltb)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun scrapeMeta(id: Int): Result<RomDto> {
        val baseUrl = preferences.serverUrl.firstOrNull()
            ?: return Result.failure(IllegalStateException("Server URL non configurato."))

        return try {
            val api = ApiClient.create(baseUrl)
            try { api.scrapeMeta(id) } catch (_: Exception) {}
            val updated = api.getRomDetails(id)
            _romsCache.value = _romsCache.value.map { if (it.id == id) updated else it }
            Result.success(updated)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getDiscs(id: Int): Result<List<RomDto>> {
        val baseUrl = preferences.serverUrl.firstOrNull()
            ?: return Result.failure(IllegalStateException("Server URL non configurato."))

        return try {
            val api = ApiClient.create(baseUrl)
            val discs = api.getDiscs(id)
            Result.success(discs)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun toggleFavorite(id: Int, isFavorite: Boolean): Result<RomDto> {
        val baseUrl = preferences.serverUrl.firstOrNull()
            ?: return Result.failure(IllegalStateException("Server URL non configurato."))

        return try {
            val api = ApiClient.create(baseUrl)
            val updated = api.toggleFavorite(id, isFavorite)
            _romsCache.value = _romsCache.value.map {
                if (it.id == id) it.copy(favorite = isFavorite) else it
            }
            Result.success(updated)
        } catch (e: Exception) {
            // Optimistic update rollback on error
            Result.failure(e)
        }
    }

    fun getRecentlyPlayed(): Flow<List<RomDto>> = romsCache.map { list ->
        list.filter { it.lastPlayed > 0 }
            .sortedByDescending { it.lastPlayed }
    }

    fun getFavorites(): Flow<List<RomDto>> = romsCache.map { list ->
        list.filter { it.favorite }
            .sortedBy { it.title.lowercase() }
    }

    fun getLatestAdded(): Flow<List<RomDto>> = romsCache.map { list ->
        list.sortedByDescending { it.createdAt }
    }

    fun getRomsBySystem(system: String): Flow<List<RomDto>> = romsCache.map { list ->
        list.filter { it.system.equals(system, ignoreCase = true) }
            .sortedBy { it.title.lowercase() }
    }

    fun getAllSystems(): Flow<List<String>> = romsCache.map { list ->
        list.map { it.system.lowercase() }
            .distinct()
            .sorted()
    }

    fun searchRoms(query: String): List<RomDto> {
        val clean = query.trim().lowercase()
        if (clean.isEmpty()) return emptyList()

        return _romsCache.value.filter { rom ->
            rom.title.lowercase().contains(clean) ||
                rom.system.lowercase().contains(clean) ||
                (rom.genre?.lowercase()?.contains(clean) == true) ||
                (rom.developer?.lowercase()?.contains(clean) == true) ||
                (rom.publisher?.lowercase()?.contains(clean) == true)
        }
    }
}
