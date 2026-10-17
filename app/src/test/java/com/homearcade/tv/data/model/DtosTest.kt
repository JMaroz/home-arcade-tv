package com.homearcade.tv.data.model

import com.homearcade.tv.ui.home.HomeSection
import com.homearcade.tv.ui.theme.getSystemColor
import kotlinx.serialization.json.Json
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class DtosTest {

    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
        coerceInputValues = true
    }

    @Test
    fun testRomDtoDeserialization() {
        val sampleJson = """
            {
                "id": 42,
                "title": "Super Mario World",
                "system": "snes",
                "slug": "super-mario-world",
                "fileName": "smw.sfc",
                "artUrl": "/api/roms/42/art",
                "favorite": true,
                "rating": 5,
                "lastPlayed": 1690000000,
                "playCount": 10,
                "minutesPlayed": 120,
                "releaseYear": 1990,
                "developer": "Nintendo",
                "players": "2"
            }
        """.trimIndent()

        val rom = json.decodeFromString<RomDto>(sampleJson)

        assertEquals(42, rom.id)
        assertEquals("Super Mario World", rom.title)
        assertEquals("snes", rom.system)
        assertTrue(rom.favorite)
        assertEquals(5, rom.rating)
        assertEquals(120, rom.minutesPlayed)
        assertEquals(1990, rom.releaseYear)
        assertEquals("Nintendo", rom.developer)
        assertEquals("2", rom.players)
    }

    @Test
    fun testRomsResponseDtoDeserialization() {
        val listJson = """
            {
                "roms": [
                    { "id": 1, "title": "Sonic", "system": "genesis" },
                    { "id": 2, "title": "Crash Bandicoot", "system": "ps1", "favorite": true }
                ],
                "total": 2,
                "hasMore": false
            }
        """.trimIndent()

        val response = json.decodeFromString<RomsResponseDto>(listJson)

        assertEquals(2, response.total)
        assertFalse(response.hasMore)
        assertEquals(2, response.roms.size)
        assertEquals("Sonic", response.roms[0].title)
        assertTrue(response.roms[1].favorite)
    }

    @Test
    fun testGetFullArtUrl() {
        val romRelative = RomDto(id = 10, title = "Game", system = "nes", artUrl = "/api/roms/10/art")
        assertEquals(
            "http://192.168.1.50:9876/api/roms/10/art",
            romRelative.getFullArtUrl("http://192.168.1.50:9876")
        )

        val romAbsolute = RomDto(id = 11, title = "Game 2", system = "nes", artUrl = "https://cdn.example.com/art.jpg")
        assertEquals(
            "https://cdn.example.com/art.jpg",
            romAbsolute.getFullArtUrl("http://192.168.1.50:9876")
        )

        val romNull = RomDto(id = 12, title = "Game 3", system = "snes")
        assertEquals(
            "http://192.168.1.50:9876/api/roms/12/art",
            romNull.getFullArtUrl("http://192.168.1.50:9876")
        )
    }

    @Test
    fun testSystemColors() {
        assertNotNull(getSystemColor("snes"))
        assertNotNull(getSystemColor("ps1"))
        assertNotNull(getSystemColor("n64"))
        assertNotNull(getSystemColor("genesis"))
        assertNotNull(getSystemColor("arcade"))
    }

    @Test
    fun testHomeSections() {
        assertEquals("all", HomeSection.ALL.id)
        assertEquals("favorites", HomeSection.FAVORITES.id)
        assertEquals("recent", HomeSection.RECENT.id)
        assertEquals("systems", HomeSection.SYSTEMS.id)
    }
}
