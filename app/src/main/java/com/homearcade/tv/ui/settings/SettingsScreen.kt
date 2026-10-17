package com.homearcade.tv.ui.settings

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Dns
import androidx.compose.material.icons.filled.LinkOff
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.SportsEsports
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.tv.material3.ExperimentalTvMaterial3Api
import androidx.tv.material3.Icon
import androidx.tv.material3.Text
import com.homearcade.tv.ui.components.TvButton
import com.homearcade.tv.ui.components.tvFocusable
import com.homearcade.tv.ui.theme.AccentGreen
import com.homearcade.tv.ui.theme.ArcadePink
import com.homearcade.tv.ui.theme.BgCard
import com.homearcade.tv.ui.theme.BgPrimary
import com.homearcade.tv.ui.theme.TextDim
import com.homearcade.tv.ui.theme.TextMain
import com.homearcade.tv.ui.theme.TextMuted

@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel,
    onBackClick: () -> Unit,
    onChangeProfile: () -> Unit,
    onServerDisconnected: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()

    BackHandler {
        onBackClick()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(BgPrimary)
            .padding(horizontal = 48.dp, vertical = 24.dp)
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxWidth(0.75f)
        ) {
            // Header
            item {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    val backShape = CircleShape
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .tvFocusable(shape = backShape, onClick = onBackClick)
                            .clip(backShape)
                            .background(Color(0x33FFFFFF)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Indietro",
                            tint = TextMain,
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(16.dp))

                    Text(
                        text = "Impostazioni",
                        color = TextMain,
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.height(28.dp))
            }

            // Server Connection Card
            item {
                SettingsCard(
                    title = "Server HomeArcade",
                    subtitle = if (uiState.serverUrl.isNotEmpty()) uiState.serverUrl else "Nessun server",
                    icon = Icons.Default.Dns,
                    badge = "ONLINE",
                    badgeColor = AccentGreen
                )
                Spacer(modifier = Modifier.height(14.dp))
            }

            // Profile Card & Switch
            item {
                SettingsCard(
                    title = "Profilo Attivo",
                    subtitle = uiState.activeProfileName,
                    icon = Icons.Default.Person,
                    actionText = "Cambia Profilo",
                    onAction = onChangeProfile
                )
                Spacer(modifier = Modifier.height(14.dp))
            }

            // Streaming Engine Toggle
            item {
                SettingsCard(
                    title = "Motore di Riproduzione Predefinito",
                    subtitle = if (uiState.preferNativeStream) "WebRTC Nativo (60 FPS, Sub-1ms Latency)" else "WebAssembly (EmulatorJS in-browser)",
                    icon = Icons.Default.Memory,
                    actionText = if (uiState.preferNativeStream) "Usa WebAssembly" else "Usa WebRTC",
                    onAction = { viewModel.toggleStreamingEngine() }
                )
                Spacer(modifier = Modifier.height(14.dp))
            }

            // Gamepad Diagnostics
            item {
                val gamepadsText = if (uiState.connectedGamepads.isNotEmpty()) {
                    uiState.connectedGamepads.joinToString(", ")
                } else {
                    "Nessun controller fisico rilevato (usa telecomando o abbina un pad Bluetooth)"
                }

                SettingsCard(
                    title = "Controller e Gamepad",
                    subtitle = gamepadsText,
                    icon = Icons.Default.SportsEsports
                )
                Spacer(modifier = Modifier.height(28.dp))
            }

            // Disconnect Server Button
            item {
                TvButton(
                    text = "Disconnetti e Cambia Server",
                    icon = Icons.Default.LinkOff,
                    onClick = {
                        viewModel.disconnectServer(onServerDisconnected)
                    }
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "HomeArcade TV v${uiState.appVersion} • Sviluppato per Android TV",
                    color = TextDim,
                    fontSize = 12.sp
                )
                Spacer(modifier = Modifier.height(32.dp))
            }
        }
    }
}

@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
private fun SettingsCard(
    title: String,
    subtitle: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    badge: String? = null,
    badgeColor: Color = AccentGreen,
    actionText: String? = null,
    onAction: (() -> Unit)? = null
) {
    val shape = RoundedCornerShape(12.dp)
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(shape)
            .background(BgCard)
            .padding(18.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = ArcadePink,
            modifier = Modifier.size(24.dp)
        )

        Spacer(modifier = Modifier.width(16.dp))

        Column(modifier = Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = title,
                    color = TextMain,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold
                )

                if (badge != null) {
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = badge,
                        color = Color.White,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier
                            .background(badgeColor, RoundedCornerShape(4.dp))
                            .padding(horizontal = 5.dp, vertical = 2.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(3.dp))

            Text(
                text = subtitle,
                color = TextMuted,
                fontSize = 13.sp
            )
        }

        if (actionText != null && onAction != null) {
            TvButton(
                text = actionText,
                onClick = onAction
            )
        }
    }
}
