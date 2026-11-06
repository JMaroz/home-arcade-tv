package com.homearcade.controller

import com.homearcade.controller.data.model.DiscoveredGameSession
import com.homearcade.controller.data.model.GamepadLayoutMode
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test

class GamepadProtocolTest {

    @Test
    fun testDiscoveredGameSessionAttributes() {
        val session = DiscoveredGameSession(
            hostIp = "192.168.1.50",
            port = 9877,
            gameTitle = "Metal Slug 2",
            platform = "neogeo",
            romId = 42,
            serverName = "HomeArcade TV"
        )

        assertEquals("192.168.1.50", session.hostIp)
        assertEquals(9877, session.port)
        assertEquals("Metal Slug 2", session.gameTitle)
        assertEquals("neogeo", session.platform)
        assertEquals(42, session.romId)
        assertEquals(0, session.defaultSlot)
    }

    @Test
    fun testSlot0Player1Encoding() {
        val magic = 0x48.toByte()
        val slot = 0.toByte() // P1
        val buttonsMask = (1 shl 9) // START
        val frame = byteArrayOf(
            magic,
            slot,
            (buttonsMask and 0xFF).toByte(),
            ((buttonsMask shr 8) and 0xFF).toByte(),
            0,
            0
        )
        assertEquals(0.toByte(), frame[1]) // slot 0 is Player 1
        assertEquals(0x02.toByte(), frame[3]) // bit 9 is bit 1 of high byte
    }

    @Test
    fun testCompactBinaryFrameEncoding() {
        val magic = 0x48.toByte()
        val slot = 1.toByte() // P2
        val buttonsMask = (1 shl 8) or (1 shl 0) // COIN + A
        val ax = 1.0f
        val ay = -1.0f

        val axByte = (ax * 127).toInt().coerceIn(-128, 127).toByte()
        val ayByte = (ay * 127).toInt().coerceIn(-128, 127).toByte()

        val frame = byteArrayOf(
            magic,
            slot,
            (buttonsMask and 0xFF).toByte(),
            ((buttonsMask shr 8) and 0xFF).toByte(),
            axByte,
            ayByte
        )

        assertEquals(6, frame.size)
        assertEquals(0x48.toByte(), frame[0])
        assertEquals(1.toByte(), frame[1])
        assertEquals(0x01.toByte(), frame[2]) // button A is bit 0
        assertEquals(0x01.toByte(), frame[3]) // coin is bit 8 -> bit 0 of high byte
        assertEquals(127.toByte(), frame[4])
        assertEquals((-127).toByte(), frame[5])
    }

    @Test
    fun testLayoutModeDefaults() {
        val arcadeMode = GamepadLayoutMode.ARCADE_4_BTN
        val consoleMode = GamepadLayoutMode.CONSOLE_DIAMOND

        assertNotNull(arcadeMode)
        assertNotNull(consoleMode)
    }
}
