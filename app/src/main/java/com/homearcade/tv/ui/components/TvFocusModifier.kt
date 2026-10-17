package com.homearcade.tv.ui.components

import android.view.KeyEvent
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.homearcade.tv.ui.theme.ArcadeGlow
import com.homearcade.tv.ui.theme.ArcadePink
import com.homearcade.tv.ui.theme.FocusBorderColor

/**
 * Custom TV focus modifier with 1.08x spring scale, neon arcade glow, and D-pad click handling.
 */
@Composable
fun Modifier.tvFocusable(
    enabled: Boolean = true,
    shape: Shape = RoundedCornerShape(12.dp),
    focusedScale: Float = 1.08f,
    focusedBorderWidth: Dp = 2.5.dp,
    focusedBorderColor: Color = FocusBorderColor,
    unfocusedBorderColor: Color = Color.Transparent,
    onFocusChanged: ((Boolean) -> Unit)? = null,
    onClick: (() -> Unit)? = null
): Modifier {
    var isFocused by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(
        targetValue = if (isFocused) focusedScale else 1.0f,
        animationSpec = spring(dampingRatio = 0.85f, stiffness = 500f),
        label = "tv_focus_scale"
    )

    val shadowElevation by animateFloatAsState(
        targetValue = if (isFocused) 16f else 0f,
        animationSpec = tween(durationMillis = 180),
        label = "tv_focus_shadow"
    )

    val interactionSource = remember { MutableInteractionSource() }

    return this
        .scale(scale)
        .shadow(
            elevation = shadowElevation.dp,
            shape = shape,
            ambientColor = ArcadeGlow,
            spotColor = ArcadePink
        )
        .border(
            width = if (isFocused) focusedBorderWidth else 1.dp,
            color = if (isFocused) focusedBorderColor else unfocusedBorderColor,
            shape = shape
        )
        .onFocusChanged { focusState ->
            isFocused = focusState.isFocused
            onFocusChanged?.invoke(focusState.isFocused)
        }
        .onKeyEvent { keyEvent ->
            if (keyEvent.nativeKeyEvent.action == KeyEvent.ACTION_UP) {
                when (keyEvent.nativeKeyEvent.keyCode) {
                    KeyEvent.KEYCODE_DPAD_CENTER,
                    KeyEvent.KEYCODE_ENTER,
                    KeyEvent.KEYCODE_NUMPAD_ENTER,
                    KeyEvent.KEYCODE_BUTTON_A -> {
                        onClick?.invoke()
                        true
                    }
                    else -> false
                }
            } else false
        }
        .focusable(enabled = enabled, interactionSource = interactionSource)
        .clickable(
            interactionSource = interactionSource,
            indication = null,
            enabled = enabled
        ) {
            onClick?.invoke()
        }
}
