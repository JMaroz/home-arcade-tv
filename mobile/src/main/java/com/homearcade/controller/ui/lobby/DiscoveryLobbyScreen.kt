package com.homearcade.controller.ui.lobby

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Gamepad
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.SettingsEthernet
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.homearcade.controller.data.LanGameRadar
import com.homearcade.controller.data.model.DiscoveredGameSession
import com.homearcade.controller.ui.theme.AccentGold
import com.homearcade.controller.ui.theme.AccentGreen
import com.homearcade.controller.ui.theme.ArcadePink
import com.homearcade.controller.ui.theme.ArcadeViolet
import com.homearcade.controller.ui.theme.BgCard
import com.homearcade.controller.ui.theme.BgPrimary
import com.homearcade.controller.ui.theme.TextDim
import com.homearcade.controller.ui.theme.TextMain
import com.homearcade.controller.ui.theme.TextMuted
import com.homearcade.controller.ui.theme.getSystemBadgeColor

@Composable
fun DiscoveryLobbyScreen(
    radar: LanGameRadar,
    onJoinSession: (session: DiscoveredGameSession, slot: Int) -> Unit
) {
    val sessions by radar.sessions.collectAsState()
    var userManuallyChangedSlot by remember { mutableStateOf(false) }
    var selectedSlot by remember { mutableIntStateOf(0) } // 0 = P1, 1 = P2, 2 = P3, 3 = P4
    var showManualIpDialog by remember { mutableStateOf(false) }

    LaunchedEffect(sessions) {
        val first = sessions.firstOrNull()
        if (first != null && !userManuallyChangedSlot) {
            selectedSlot = first.defaultSlot
        }
    }

    LaunchedEffect(Unit) {
        radar.startScanning()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(BgPrimary)
            .padding(24.dp)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Header Bar
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Gamepad,
                        contentDescription = null,
                        tint = ArcadePink,
                        modifier = Modifier.size(28.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = "HOMEARCADE",
                        color = TextMain,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 2.sp
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "CONTROLLER",
                        color = ArcadePink,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 2.sp
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    // Slot selection chips
                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(20.dp))
                            .background(BgCard)
                            .padding(4.dp),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        listOf(0 to "P1", 1 to "P2", 2 to "P3", 3 to "P4").forEach { (slot, label) ->
                            val isSelected = (selectedSlot == slot)
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(16.dp))
                                    .background(if (isSelected) ArcadePink else Color.Transparent)
                                    .clickable {
                                        selectedSlot = slot
                                        userManuallyChangedSlot = true
                                    }
                                    .padding(horizontal = 14.dp, vertical = 6.dp)
                            ) {
                                Text(
                                    text = label,
                                    color = if (isSelected) TextMain else TextMuted,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    IconButton(
                        onClick = { showManualIpDialog = true },
                        modifier = Modifier
                            .clip(CircleShape)
                            .background(BgCard)
                    ) {
                        Icon(
                            imageVector = Icons.Default.SettingsEthernet,
                            contentDescription = "Connessione Manuale",
                            tint = TextMuted
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Main Content Area
            if (sessions.isEmpty()) {
                // Empty state with pulsing radar
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    PulsingRadarView()
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .weight(1f),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    items(sessions) { session ->
                        GameSessionCard(
                            session = session,
                            slot = selectedSlot,
                            onJoin = { onJoinSession(session, selectedSlot) }
                        )
                    }
                }
            }
        }

        // Manual IP Connect Dialog
        if (showManualIpDialog) {
            ManualIpDialog(
                onDismiss = { showManualIpDialog = false },
                onConnect = { ip, port ->
                    showManualIpDialog = false
                    radar.probeDirectIp(ip, port) { probed ->
                        if (probed != null) {
                            onJoinSession(probed, selectedSlot)
                        }
                    }
                }
            )
        }
    }
}

@Composable
private fun GameSessionCard(
    session: DiscoveredGameSession,
    slot: Int,
    onJoin: () -> Unit
) {
    val badgeColor = getSystemBadgeColor(session.platform)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(BgCard)
            .border(1.dp, Color(0xFF242C44), RoundedCornerShape(16.dp))
            .padding(20.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(badgeColor.copy(alpha = 0.2f))
                        .border(1.dp, badgeColor, RoundedCornerShape(6.dp))
                        .padding(horizontal = 8.dp, vertical = 3.dp)
                ) {
                    Text(
                        text = session.platform.uppercase(),
                        color = badgeColor,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.width(10.dp))

                Text(
                    text = session.serverName,
                    color = TextDim,
                    fontSize = 12.sp
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = session.gameTitle,
                color = TextMain,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = "Host LAN: ${session.hostIp}:${session.port}",
                color = TextMuted,
                fontSize = 12.sp
            )
        }

        Spacer(modifier = Modifier.width(20.dp))

        Button(
            onClick = onJoin,
            colors = ButtonDefaults.buttonColors(
                containerColor = ArcadePink,
                contentColor = Color.White
            ),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.height(48.dp)
        ) {
            Icon(
                imageVector = Icons.Default.PlayArrow,
                contentDescription = null,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "Entra come Giocatore ${slot + 1}",
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp
            )
        }
    }
}

@Composable
private fun PulsingRadarView() {
    val infiniteTransition = rememberInfiniteTransition(label = "RadarPulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 0.8f,
        targetValue = 1.35f,
        animationSpec = infiniteRepeatable(
            animation = tween(1800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "PulseScale"
    )
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.6f,
        targetValue = 0.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(1800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "PulseAlpha"
    )

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(contentAlignment = Alignment.Center) {
            // Expanding pulse wave
            Box(
                modifier = Modifier
                    .size(140.dp)
                    .scale(pulseScale)
                    .clip(CircleShape)
                    .background(ArcadePink.copy(alpha = pulseAlpha))
            )

            // Inner circle
            Box(
                modifier = Modifier
                    .size(90.dp)
                    .clip(CircleShape)
                    .background(BgCard)
                    .border(2.dp, ArcadePink, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Refresh,
                    contentDescription = null,
                    tint = ArcadePink,
                    modifier = Modifier.size(36.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "Scansione rete locale in corso...",
            color = TextMain,
            fontSize = 17.sp,
            fontWeight = FontWeight.SemiBold
        )

        Spacer(modifier = Modifier.height(6.dp))

        Text(
            text = "Avvia una partita su HomeArcade TV per connetterti istantaneamente come secondo giocatore.",
            color = TextDim,
            fontSize = 13.sp,
            textAlign = TextAlign.Center,
            modifier = Modifier.width(420.dp)
        )
    }
}

@Composable
private fun ManualIpDialog(
    onDismiss: () -> Unit,
    onConnect: (ip: String, port: Int) -> Unit
) {
    var ipText by remember { mutableStateOf("") }
    var portText by remember { mutableStateOf("9877") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Connessione Manuale TV",
                color = TextMain,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column {
                Text(
                    text = "Inserisci l'indirizzo IP locale mostrato sulla schermata di pausa della TV:",
                    color = TextMuted,
                    fontSize = 13.sp
                )
                Spacer(modifier = Modifier.height(14.dp))
                OutlinedTextField(
                    value = ipText,
                    onValueChange = { ipText = it },
                    label = { Text("Indirizzo IP (es. 192.168.1.50)") },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = ArcadePink,
                        unfocusedBorderColor = Color(0xFF2E344A),
                        focusedTextColor = TextMain,
                        unfocusedTextColor = TextMain
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(10.dp))
                OutlinedTextField(
                    value = portText,
                    onValueChange = { portText = it },
                    label = { Text("Porta (Default 9877)") },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = ArcadePink,
                        unfocusedBorderColor = Color(0xFF2E344A),
                        focusedTextColor = TextMain,
                        unfocusedTextColor = TextMain
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val port = portText.toIntOrNull() ?: 9877
                    if (ipText.isNotBlank()) {
                        onConnect(ipText.trim(), port)
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = ArcadePink)
            ) {
                Text("Connetti", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Annulla", color = TextMuted)
            }
        },
        containerColor = BgCard
    )
}
