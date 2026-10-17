package com.homearcade.tv.ui.setup

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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.SettingsEthernet
import androidx.compose.material.icons.filled.SportsEsports
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.tv.material3.ExperimentalTvMaterial3Api
import androidx.tv.material3.Icon
import androidx.tv.material3.Text
import com.homearcade.tv.data.api.DiscoveryState
import com.homearcade.tv.ui.components.TvButton
import com.homearcade.tv.ui.components.TvLoadingSpinner
import com.homearcade.tv.ui.components.tvFocusable
import com.homearcade.tv.ui.theme.AccentGreen
import com.homearcade.tv.ui.theme.AccentRed
import com.homearcade.tv.ui.theme.ArcadePink
import com.homearcade.tv.ui.theme.BgCard
import com.homearcade.tv.ui.theme.BgPrimary
import com.homearcade.tv.ui.theme.TextDim
import com.homearcade.tv.ui.theme.TextMain
import com.homearcade.tv.ui.theme.TextMuted

@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
fun SetupScreen(
    viewModel: SetupViewModel,
    onConnected: () -> Unit
) {
    val discoveryState by viewModel.discoveryState.collectAsState()
    val manualUrl by viewModel.manualUrl.collectAsState()
    val isTesting by viewModel.isManualTesting.collectAsState()
    val manualError by viewModel.manualError.collectAsState()

    var showManualInput by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(BgPrimary)
            .padding(48.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.width(620.dp)
        ) {
            // App Branding Icon & Title
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Icon(
                    imageVector = Icons.Default.SportsEsports,
                    contentDescription = null,
                    tint = ArcadePink,
                    modifier = Modifier.size(48.dp)
                )
                Spacer(modifier = Modifier.width(16.dp))
                Row {
                    Text(
                        text = "HomeArcade",
                        color = TextMain,
                        fontSize = 38.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = " TV",
                        color = ArcadePink,
                        fontSize = 38.sp,
                        fontWeight = FontWeight.ExtraBold
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            Text(
                text = "L'app si collega al tuo Home Assistant con l'add-on HomeArcade per riprodurre la tua libreria retro gaming.",
                color = TextMuted,
                fontSize = 15.sp,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(40.dp))

            if (showManualInput) {
                // Section: Manual IP Configuration
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(BgCard)
                        .padding(24.dp)
                ) {
                    Text(
                        text = "Configurazione Manuale Server",
                        color = TextMain,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Text Input box
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color(0xFF0A0E17))
                            .tvFocusable(shape = RoundedCornerShape(8.dp))
                            .padding(horizontal = 14.dp, vertical = 12.dp)
                    ) {
                        BasicTextField(
                            value = manualUrl,
                            onValueChange = { viewModel.setManualUrl(it) },
                            textStyle = TextStyle(
                                color = TextMain,
                                fontSize = 15.sp
                            ),
                            cursorBrush = SolidColor(ArcadePink),
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    if (manualError != null) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = manualError ?: "",
                            color = AccentRed,
                            fontSize = 12.sp
                        )
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        TvButton(
                            text = if (isTesting) "Verifica in corso..." else "Connetti",
                            icon = Icons.Default.CheckCircle,
                            isPrimary = true,
                            onClick = {
                                viewModel.connectToServer(manualUrl, onConnected)
                            }
                        )

                        TvButton(
                            text = "Torna alla Ricerca",
                            icon = Icons.Default.Refresh,
                            onClick = {
                                showManualInput = false
                                viewModel.startDiscovery()
                            }
                        )
                    }
                }
            } else {
                // Section: Automatic Discovery
                when (val state = discoveryState) {
                    is DiscoveryState.Idle, is DiscoveryState.Searching -> {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(14.dp))
                                .background(BgCard)
                                .padding(32.dp)
                        ) {
                            TvLoadingSpinner(size = 40.dp)
                            Spacer(modifier = Modifier.height(18.dp))
                            Text(
                                text = "Ricerca automatica del server HomeArcade...",
                                color = TextMain,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            val target = if (state is DiscoveryState.Searching) state.currentTarget else ""
                            Text(
                                text = target,
                                color = TextDim,
                                fontSize = 12.sp
                            )
                            Spacer(modifier = Modifier.height(24.dp))
                            TvButton(
                                text = "Inserisci IP Manualmente",
                                icon = Icons.Default.SettingsEthernet,
                                onClick = { showManualInput = true }
                            )
                        }
                    }

                    is DiscoveryState.Found -> {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(14.dp))
                                .background(BgCard)
                                .padding(28.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = null,
                                tint = AccentGreen,
                                modifier = Modifier.size(44.dp)
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = "Server HomeArcade Trovato!",
                                color = TextMain,
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = state.url,
                                color = ArcadePink,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Medium
                            )
                            Spacer(modifier = Modifier.height(24.dp))
                            Row(horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                                TvButton(
                                    text = "Connetti Ora",
                                    icon = Icons.Default.CheckCircle,
                                    isPrimary = true,
                                    onClick = {
                                        viewModel.connectToServer(state.url, onConnected)
                                    }
                                )
                                TvButton(
                                    text = "Manuale",
                                    icon = Icons.Default.SettingsEthernet,
                                    onClick = { showManualInput = true }
                                )
                            }
                        }
                    }

                    is DiscoveryState.Failed -> {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(14.dp))
                                .background(BgCard)
                                .padding(28.dp)
                        ) {
                            Text(
                                text = "Nessun server rilevato automaticamente",
                                color = TextMain,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = state.message,
                                color = TextMuted,
                                fontSize = 13.sp,
                                textAlign = TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(24.dp))
                            Row(horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                                TvButton(
                                    text = "Riprova Ricerca",
                                    icon = Icons.Default.Refresh,
                                    isPrimary = true,
                                    onClick = { viewModel.startDiscovery() }
                                )
                                TvButton(
                                    text = "Configura IP Manuale",
                                    icon = Icons.Default.SettingsEthernet,
                                    onClick = { showManualInput = true }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
