package com.homearcade.controller.ui.gamepad

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.Vibration
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.homearcade.controller.data.GamepadWebSocketClient
import com.homearcade.controller.data.model.ConnectionStatus
import com.homearcade.controller.data.model.DiscoveredGameSession
import com.homearcade.controller.data.model.GamepadLayoutMode
import com.homearcade.controller.ui.theme.AccentBlue
import com.homearcade.controller.ui.theme.AccentGold
import com.homearcade.controller.ui.theme.AccentGreen
import com.homearcade.controller.ui.theme.AccentRed
import com.homearcade.controller.ui.theme.ArcadePink
import com.homearcade.controller.ui.theme.ArcadeViolet
import com.homearcade.controller.ui.theme.BgCard
import com.homearcade.controller.ui.theme.BgPrimary
import com.homearcade.controller.ui.theme.TextDim
import com.homearcade.controller.ui.theme.TextMain
import com.homearcade.controller.ui.theme.TextMuted
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.roundToInt
import kotlin.math.sin
import kotlin.math.sqrt

@Composable
fun ArcadeGamepadScreen(
    session: DiscoveredGameSession,
    slot: Int,
    client: GamepadWebSocketClient,
    onDisconnect: () -> Unit
) {
    val context = LocalContext.current
    val connectionStatus by client.status.collectAsState()

    var isHapticEnabled by remember { mutableStateOf(true) }
    var layoutMode by remember {
        val defaultMode = if (session.platform.equals("snes", ignoreCase = true) ||
            session.platform.equals("ps1", ignoreCase = true) ||
            session.platform.equals("psx", ignoreCase = true)
        ) {
            GamepadLayoutMode.CONSOLE_DIAMOND
        } else {
            GamepadLayoutMode.ARCADE_4_BTN
        }
        mutableStateOf(defaultMode)
    }

    var buttonsMask by remember { mutableIntStateOf(0) }
    var axisX by remember { mutableFloatStateOf(0f) }
    var axisY by remember { mutableFloatStateOf(0f) }

    val vibrator = remember {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
            vibratorManager?.defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
        }
    }

    fun triggerHaptic() {
        if (!isHapticEnabled) return
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                vibrator?.vibrate(VibrationEffect.createPredefined(VibrationEffect.EFFECT_CLICK))
            } else {
                @Suppress("DEPRECATION")
                vibrator?.vibrate(15)
            }
        } catch (_: Exception) {}
    }

    fun updateButton(bit: Int, isPressed: Boolean) {
        val oldMask = buttonsMask
        buttonsMask = if (isPressed) {
            buttonsMask or (1 shl bit)
        } else {
            buttonsMask and (1 shl bit).inv()
        }
        if (isPressed && oldMask != buttonsMask) {
            triggerHaptic()
        }
        client.sendInput(buttonsMask, axisX, axisY)
    }

    fun updateAxes(newX: Float, newY: Float) {
        val wasActive = (axisX != 0f || axisY != 0f)
        axisX = newX
        axisY = newY

        // Also update digital D-Pad bits 12(Up), 13(Down), 14(Left), 15(Right)
        var mask = buttonsMask
        mask = if (newY < -0.35f) mask or (1 shl 12) else mask and (1 shl 12).inv()
        mask = if (newY > 0.35f) mask or (1 shl 13) else mask and (1 shl 13).inv()
        mask = if (newX < -0.35f) mask or (1 shl 14) else mask and (1 shl 14).inv()
        mask = if (newX > 0.35f) mask or (1 shl 15) else mask and (1 shl 15).inv()

        val isNowActive = (newX != 0f || newY != 0f)
        if (!wasActive && isNowActive) {
            triggerHaptic()
        }

        buttonsMask = mask
        client.sendInput(buttonsMask, axisX, axisY)
    }

    // Connect on entry, disconnect on exit
    DisposableEffect(session, slot) {
        client.connect(session.hostIp, session.port, slot)
        onDispose {
            client.disconnect()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(BgPrimary)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Top Status Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(44.dp)
                    .background(Color(0xFF0F1422))
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = onDisconnect,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Disconnetti",
                            tint = TextMuted
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(ArcadePink)
                            .padding(horizontal = 8.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = "GIOCATORE ${slot + 1}",
                            color = TextMain,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    Text(
                        text = "• ${session.gameTitle}",
                        color = TextDim,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    // Connection Status Dot
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .clip(CircleShape)
                            .background(
                                when (connectionStatus) {
                                    ConnectionStatus.CONNECTED -> AccentGreen
                                    ConnectionStatus.CONNECTING -> AccentGold
                                    else -> AccentRed
                                }
                            )
                    )

                    Spacer(modifier = Modifier.width(16.dp))

                    // Layout Mode Switcher
                    IconButton(
                        onClick = {
                            layoutMode = if (layoutMode == GamepadLayoutMode.ARCADE_4_BTN) {
                                GamepadLayoutMode.CONSOLE_DIAMOND
                            } else {
                                GamepadLayoutMode.ARCADE_4_BTN
                            }
                        },
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Dashboard,
                            contentDescription = "Cambia Layout",
                            tint = if (layoutMode == GamepadLayoutMode.ARCADE_4_BTN) ArcadePink else ArcadeViolet
                        )
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    // Haptic Feedback Toggle
                    IconButton(
                        onClick = { isHapticEnabled = !isHapticEnabled },
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Vibration,
                            contentDescription = "Feedback Aptico",
                            tint = if (isHapticEnabled) AccentGreen else TextDim
                        )
                    }
                }
            }

            // Main Controller Canvas
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 24.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // LEFT: Analog D-Pad (8-Way Directional Thumbstick)
                Box(
                    modifier = Modifier.size(200.dp),
                    contentAlignment = Alignment.Center
                ) {
                    VirtualDPad(
                        axisX = axisX,
                        axisY = axisY,
                        onAxesChanged = { x, y -> updateAxes(x, y) }
                    )
                }

                // CENTER: System Buttons (INSERT COIN & START)
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // INSERT COIN (Bit 8)
                    ArcadePillButton(
                        label = "INSERT COIN",
                        bit = 8,
                        color = AccentGold,
                        isPressed = ((buttonsMask and (1 shl 8)) != 0),
                        onStateChanged = { pressed -> updateButton(8, pressed) }
                    )

                    // START (Bit 9)
                    ArcadePillButton(
                        label = "START",
                        bit = 9,
                        color = AccentGreen,
                        isPressed = ((buttonsMask and (1 shl 9)) != 0),
                        onStateChanged = { pressed -> updateButton(9, pressed) }
                    )
                }

                // RIGHT: Action Buttons
                Box(
                    modifier = Modifier.size(240.dp),
                    contentAlignment = Alignment.Center
                ) {
                    if (layoutMode == GamepadLayoutMode.ARCADE_4_BTN) {
                        // Neo Geo 4-Button Arc Layout (A: Shoot, B: Jump, C: Grenade, D: Special)
                        ArcadeCurveButtons(
                            buttonsMask = buttonsMask,
                            onButtonStateChanged = { bit, pressed -> updateButton(bit, pressed) }
                        )
                    } else {
                        // Console Diamond Layout (A, B, X, Y)
                        ConsoleDiamondButtons(
                            buttonsMask = buttonsMask,
                            onButtonStateChanged = { bit, pressed -> updateButton(bit, pressed) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun VirtualDPad(
    axisX: Float,
    axisY: Float,
    onAxesChanged: (Float, Float) -> Unit
) {
    val maxRadiusPx = 90f

    Box(
        modifier = Modifier
            .size(190.dp)
            .clip(CircleShape)
            .background(Color(0xFF131722))
            .border(2.dp, Color(0xFF28314A), CircleShape)
            .pointerInput(Unit) {
                detectDragGestures(
                    onDragStart = { offset ->
                        val center = Offset(size.width / 2f, size.height / 2f)
                        val delta = offset - center
                        val dist = delta.getDistance()
                        val angle = atan2(delta.y, delta.x)
                        val clampedDist = dist.coerceAtMost(maxRadiusPx)
                        val normX = (cos(angle) * (clampedDist / maxRadiusPx)).coerceIn(-1f, 1f)
                        val normY = (sin(angle) * (clampedDist / maxRadiusPx)).coerceIn(-1f, 1f)
                        onAxesChanged(normX, normY)
                    },
                    onDrag = { change, _ ->
                        change.consume()
                        val center = Offset(size.width / 2f, size.height / 2f)
                        val delta = change.position - center
                        val dist = delta.getDistance()
                        val angle = atan2(delta.y, delta.x)
                        val clampedDist = dist.coerceAtMost(maxRadiusPx)
                        val normX = (cos(angle) * (clampedDist / maxRadiusPx)).coerceIn(-1f, 1f)
                        val normY = (sin(angle) * (clampedDist / maxRadiusPx)).coerceIn(-1f, 1f)
                        onAxesChanged(normX, normY)
                    },
                    onDragEnd = {
                        onAxesChanged(0f, 0f)
                    },
                    onDragCancel = {
                        onAxesChanged(0f, 0f)
                    }
                )
            },
        contentAlignment = Alignment.Center
    ) {
        // Crosshair background guides
        Canvas(modifier = Modifier.fillMaxSize()) {
            val center = Offset(size.width / 2f, size.height / 2f)
            drawCircle(color = Color(0xFF1E2538), radius = maxRadiusPx, center = center, style = Stroke(1.5f))
        }

        // Thumb nub indicating drag position
        val thumbOffset = IntOffset(
            x = (axisX * (maxRadiusPx - 15f)).roundToInt(),
            y = (axisY * (maxRadiusPx - 15f)).roundToInt()
        )

        Box(
            modifier = Modifier
                .offset { thumbOffset }
                .size(68.dp)
                .clip(CircleShape)
                .background(ArcadePink)
                .border(2.dp, Color(0xFFF472B6), CircleShape)
        )
    }
}

@Composable
private fun ArcadeCurveButtons(
    buttonsMask: Int,
    onButtonStateChanged: (bit: Int, isPressed: Boolean) -> Unit
) {
    Box(modifier = Modifier.fillMaxSize()) {
        // Button A (Shoot - Red) - Bit 0
        ArcadeRoundButton(
            label = "A",
            bit = 0,
            color = AccentRed,
            isPressed = ((buttonsMask and (1 shl 0)) != 0),
            modifier = Modifier
                .align(Alignment.BottomStart)
                .offset(x = 10.dp, y = (-12).dp),
            onStateChanged = { onButtonStateChanged(0, it) }
        )

        // Button B (Jump - Blue) - Bit 1
        ArcadeRoundButton(
            label = "B",
            bit = 1,
            color = AccentBlue,
            isPressed = ((buttonsMask and (1 shl 1)) != 0),
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .offset(x = (-10).dp, y = (-42).dp),
            onStateChanged = { onButtonStateChanged(1, it) }
        )

        // Button C (Grenade - Green) - Bit 2
        ArcadeRoundButton(
            label = "C",
            bit = 2,
            color = AccentGreen,
            isPressed = ((buttonsMask and (1 shl 2)) != 0),
            modifier = Modifier
                .align(Alignment.TopCenter)
                .offset(x = 10.dp, y = 30.dp),
            onStateChanged = { onButtonStateChanged(2, it) }
        )

        // Button D (Special - Gold) - Bit 3
        ArcadeRoundButton(
            label = "D",
            bit = 3,
            color = AccentGold,
            isPressed = ((buttonsMask and (1 shl 3)) != 0),
            modifier = Modifier
                .align(Alignment.TopEnd)
                .offset(x = (-10).dp, y = 10.dp),
            onStateChanged = { onButtonStateChanged(3, it) }
        )
    }
}

@Composable
private fun ConsoleDiamondButtons(
    buttonsMask: Int,
    onButtonStateChanged: (bit: Int, isPressed: Boolean) -> Unit
) {
    Box(modifier = Modifier.fillMaxSize()) {
        // Button Y (Top - Green) - Bit 3
        ArcadeRoundButton(
            label = "Y",
            bit = 3,
            color = AccentGreen,
            isPressed = ((buttonsMask and (1 shl 3)) != 0),
            modifier = Modifier.align(Alignment.TopCenter),
            onStateChanged = { onButtonStateChanged(3, it) }
        )

        // Button X (Left - Blue) - Bit 2
        ArcadeRoundButton(
            label = "X",
            bit = 2,
            color = AccentBlue,
            isPressed = ((buttonsMask and (1 shl 2)) != 0),
            modifier = Modifier.align(Alignment.CenterStart),
            onStateChanged = { onButtonStateChanged(2, it) }
        )

        // Button B (Right - Red) - Bit 1
        ArcadeRoundButton(
            label = "B",
            bit = 1,
            color = AccentRed,
            isPressed = ((buttonsMask and (1 shl 1)) != 0),
            modifier = Modifier.align(Alignment.CenterEnd),
            onStateChanged = { onButtonStateChanged(1, it) }
        )

        // Button A (Bottom - Gold) - Bit 0
        ArcadeRoundButton(
            label = "A",
            bit = 0,
            color = AccentGold,
            isPressed = ((buttonsMask and (1 shl 0)) != 0),
            modifier = Modifier.align(Alignment.BottomCenter),
            onStateChanged = { onButtonStateChanged(0, it) }
        )
    }
}

@Composable
private fun ArcadeRoundButton(
    label: String,
    bit: Int,
    color: Color,
    isPressed: Boolean,
    modifier: Modifier = Modifier,
    onStateChanged: (Boolean) -> Unit
) {
    Box(
        modifier = modifier
            .size(72.dp)
            .clip(CircleShape)
            .background(if (isPressed) color else color.copy(alpha = 0.85f))
            .border(
                width = 3.dp,
                color = if (isPressed) Color.White else color.copy(alpha = 0.5f),
                shape = CircleShape
            )
            .pointerInput(bit) {
                detectTapGestures(
                    onPress = {
                        onStateChanged(true)
                        tryAwaitRelease()
                        onStateChanged(false)
                    }
                )
            },
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            color = Color.White,
            fontSize = 22.sp,
            fontWeight = FontWeight.Black
        )
    }
}

@Composable
private fun ArcadePillButton(
    label: String,
    bit: Int,
    color: Color,
    isPressed: Boolean,
    onStateChanged: (Boolean) -> Unit
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(20.dp))
            .background(if (isPressed) color else Color(0xFF1A1F2E))
            .border(
                width = 2.dp,
                color = if (isPressed) Color.White else color,
                shape = RoundedCornerShape(20.dp)
            )
            .pointerInput(bit) {
                detectTapGestures(
                    onPress = {
                        onStateChanged(true)
                        tryAwaitRelease()
                        onStateChanged(false)
                    }
                )
            }
            .padding(horizontal = 22.dp, vertical = 10.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            color = if (isPressed) Color.Black else color,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.sp
        )
    }
}
