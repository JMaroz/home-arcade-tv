package com.homearcade.tv.ui.details

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.DiscFull
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.HourglassTop
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material.icons.filled.Timer
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.tv.material3.ExperimentalTvMaterial3Api
import androidx.tv.material3.Icon
import androidx.tv.material3.Text
import coil3.compose.AsyncImage
import com.homearcade.tv.ui.components.TvButton
import com.homearcade.tv.ui.components.TvLoadingSpinner
import com.homearcade.tv.ui.components.TvShelfRow
import com.homearcade.tv.ui.components.tvFocusable
import com.homearcade.tv.ui.theme.AccentCyan
import com.homearcade.tv.ui.theme.AccentGold
import com.homearcade.tv.ui.theme.ArcadePink
import com.homearcade.tv.ui.theme.BgCard
import com.homearcade.tv.ui.theme.BgPrimary
import com.homearcade.tv.ui.theme.BorderColor
import com.homearcade.tv.ui.theme.TextDim
import com.homearcade.tv.ui.theme.TextMain
import com.homearcade.tv.ui.theme.TextMuted
import com.homearcade.tv.ui.theme.getRecommendedCore
import com.homearcade.tv.ui.theme.getSystemColor
import com.homearcade.tv.ui.theme.getSystemDisplayName
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
fun DetailsScreen(
    viewModel: DetailsViewModel,
    onBackClick: () -> Unit,
    onLaunchGame: (romId: Int, engine: String) -> Unit,
    onNavigateToRom: (romId: Int) -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsState()
    val rom = uiState.rom

    BackHandler {
        onBackClick()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(BgPrimary)
    ) {
        if (uiState.isLoading) {
            TvLoadingSpinner(size = 54.dp)
        } else if (rom != null) {
            val backdropUrl = rom.getFullArtUrl(uiState.serverUrl)
            val sysColor = getSystemColor(rom.system)

            // High-res Background Artwork
            if (!backdropUrl.isNullOrEmpty()) {
                AsyncImage(
                    model = backdropUrl,
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    alignment = Alignment.TopEnd,
                    modifier = Modifier.fillMaxSize()
                )
            }

            // Dark Scrim Gradients for text contrast
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.horizontalGradient(
                            colors = listOf(
                                BgPrimary,
                                BgPrimary.copy(alpha = 0.95f),
                                BgPrimary.copy(alpha = 0.80f),
                                Color.Transparent
                            ),
                            startX = 0f,
                            endX = 1200f
                        )
                    )
            )

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                Color.Transparent,
                                BgPrimary.copy(alpha = 0.6f),
                                BgPrimary
                            ),
                            startY = 200f
                        )
                    )
            )

            // Scrollable Content
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 48.dp, vertical = 24.dp)
            ) {
                // Back Button
                item {
                    val shape = CircleShape
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .tvFocusable(shape = shape, onClick = onBackClick)
                            .clip(shape)
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
                    Spacer(modifier = Modifier.height(18.dp))
                }

                // Metadata Badges Row
                item {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Text(
                            text = "${rom.system.uppercase()} • ${getSystemDisplayName(rom.system)}",
                            color = Color.White,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.ExtraBold,
                            modifier = Modifier
                                .background(sysColor, RoundedCornerShape(4.dp))
                                .padding(horizontal = 8.dp, vertical = 3.dp)
                        )

                        rom.getFileExtension()?.let { ext ->
                            Text(
                                text = ext,
                                color = TextMain,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier
                                    .background(Color(0x26FFFFFF), RoundedCornerShape(4.dp))
                                    .padding(horizontal = 7.dp, vertical = 3.dp)
                            )
                        }

                        if (rom.releaseYear != null && rom.releaseYear > 0) {
                            Text(
                                text = "${rom.releaseYear}",
                                color = TextMuted,
                                fontSize = 13.sp
                            )
                        }

                        if (!rom.genre.isNullOrEmpty()) {
                            Text(
                                text = "•  ${rom.genre}",
                                color = TextMuted,
                                fontSize = 13.sp
                            )
                        }

                        if (!rom.players.isNullOrEmpty()) {
                            Text(
                                text = "•  ${rom.players} Giocatori",
                                color = TextMuted,
                                fontSize = 13.sp
                            )
                        }

                        if (rom.rating > 0) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Star,
                                    contentDescription = null,
                                    tint = AccentGold,
                                    modifier = Modifier.height(14.dp)
                                )
                                Text(
                                    text = " ${rom.rating}/5",
                                    color = TextMain,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))
                }

                // Title
                item {
                    Text(
                        text = rom.title,
                        color = TextMain,
                        fontSize = 34.sp,
                        fontWeight = FontWeight.ExtraBold,
                        lineHeight = 40.sp,
                        modifier = Modifier.width(760.dp)
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    val developerInfo = listOfNotNull(rom.developer, rom.publisher)
                        .distinct()
                        .joinToString(" • ")
                    if (developerInfo.isNotEmpty()) {
                        Text(
                            text = developerInfo,
                            color = TextMuted,
                            fontSize = 13.sp
                        )
                    }

                    Spacer(modifier = Modifier.height(18.dp))
                }

                // Action Buttons
                item {
                    val targetRomId = uiState.selectedDiscId ?: rom.id
                    val engineName = if (uiState.preferNativeStream) "webrtc" else "wasm"

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        TvButton(
                            text = "Gioca Ora",
                            icon = Icons.Default.PlayArrow,
                            isPrimary = true,
                            onClick = {
                                onLaunchGame(targetRomId, engineName)
                            }
                        )

                        TvButton(
                            text = if (rom.favorite) "Nei Preferiti" else "Aggiungi",
                            icon = if (rom.favorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                            onClick = { viewModel.toggleFavorite() }
                        )

                        TvButton(
                            text = if (uiState.preferNativeStream) "Motore: WebRTC (60fps)" else "Motore: WebAssembly",
                            icon = Icons.Default.Memory,
                            onClick = { viewModel.toggleEngine() }
                        )

                        TvButton(
                            text = if (uiState.isScraping) "Ricerca Info..." else "Cerca Info Online",
                            icon = Icons.Default.CloudDownload,
                            onClick = { viewModel.scrapeMetadata() }
                        )
                    }

                    Spacer(modifier = Modifier.height(24.dp))
                }

                // Detailed Informational Cards (Game stats, File specs, HowLongToBeat)
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(end = 48.dp),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        // Card 1: Statistiche di Gioco
                        InfoCard(
                            title = "STATISTICHE DI GIOCO",
                            icon = Icons.Default.Timer,
                            iconTint = ArcadePink,
                            modifier = Modifier.weight(1f)
                        ) {
                            val playTimeStr = if (rom.minutesPlayed > 0) {
                                val h = rom.minutesPlayed / 60
                                val m = rom.minutesPlayed % 60
                                if (h > 0) "${h}h ${m}m" else "${m}m"
                            } else {
                                "0m"
                            }
                            InfoRow(label = "Tempo Giocato", value = playTimeStr)
                            InfoRow(label = "Sessioni", value = "${rom.playCount} partite")
                            InfoRow(label = "Ultimo Avvio", value = formatTimestamp(rom.lastPlayed))
                        }

                        // Card 2: Scheda Tecnica File
                        InfoCard(
                            title = "SCHEDA TECNICA & FILE",
                            icon = Icons.Default.Storage,
                            iconTint = AccentCyan,
                            modifier = Modifier.weight(1.3f)
                        ) {
                            val fileNameDisplay = rom.fileName ?: rom.originalName ?: "Standard ROM"
                            InfoRow(label = "File", value = fileNameDisplay)
                            InfoRow(label = "Dimensione", value = rom.getFormattedSize() ?: "N/D")
                            InfoRow(label = "Core", value = getRecommendedCore(rom.system))
                        }

                        // Card 3: HowLongToBeat
                        InfoCard(
                            title = "HOWLONGTOBEAT",
                            icon = Icons.Default.HourglassTop,
                            iconTint = AccentGold,
                            modifier = Modifier.weight(1.1f)
                        ) {
                            val hltb = uiState.hltb
                            if (hltb != null && hltb.found) {
                                InfoRow(label = "Storia Principale", value = hltb.formatStory() ?: "N/D")
                                InfoRow(label = "Storia + Extra", value = hltb.formatExtra() ?: "N/D")
                                InfoRow(label = "Completista", value = hltb.formatCompletionist() ?: "N/D")
                            } else {
                                Text(
                                    text = "Nessun dato HLTB disponibile",
                                    color = TextDim,
                                    fontSize = 12.sp,
                                    modifier = Modifier.padding(vertical = 4.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))
                }

                // Multi-disc / Variant Selector
                if (uiState.discs.size > 1) {
                    item {
                        Text(
                            text = "Seleziona Disco / Variante:",
                            color = TextMain,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(8.dp))

                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            items(uiState.discs) { disc ->
                                val isSelected = disc.id == (uiState.selectedDiscId ?: rom.id)
                                val pillShape = RoundedCornerShape(8.dp)
                                val label = disc.discNumber?.let { "Disco $it" } ?: disc.originalName ?: disc.fileName ?: disc.title

                                Box(
                                    modifier = Modifier
                                        .tvFocusable(shape = pillShape, onClick = { viewModel.selectDisc(disc.id) })
                                        .clip(pillShape)
                                        .background(if (isSelected) ArcadePink else Color(0x33FFFFFF))
                                        .padding(horizontal = 14.dp, vertical = 8.dp)
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            imageVector = Icons.Default.DiscFull,
                                            contentDescription = null,
                                            tint = TextMain,
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = label,
                                            color = TextMain,
                                            fontSize = 12.sp,
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                        )
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(24.dp))
                    }
                }

                // Plot / Description
                item {
                    Text(
                        text = "Descrizione",
                        color = TextMain,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = if (!rom.description.isNullOrEmpty()) {
                            rom.description
                        } else {
                            "Nessuna sinossi disponibile per questo gioco. Premi 'Cerca Info Online' in alto per scaricare automaticamente trame, generi e metadati."
                        },
                        color = TextDim,
                        fontSize = 14.sp,
                        lineHeight = 22.sp,
                        modifier = Modifier.width(760.dp)
                    )
                    Spacer(modifier = Modifier.height(24.dp))
                }

                // Save states info
                if (uiState.saveStates.isNotEmpty()) {
                    item {
                        Text(
                            text = "Stati di Salvataggio (${uiState.saveStates.size})",
                            color = TextMain,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(8.dp))

                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            items(uiState.saveStates) { slot ->
                                Row(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(BgCard)
                                        .padding(horizontal = 12.dp, vertical = 8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Save,
                                        contentDescription = null,
                                        tint = ArcadePink,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "Slot #${slot.slot}",
                                        color = TextMain,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(24.dp))
                    }
                }

                // Related Games Shelf for this Platform
                if (uiState.relatedGames.isNotEmpty()) {
                    item {
                        TvShelfRow(
                            title = "Altri titoli per ${getSystemDisplayName(rom.system)}",
                            items = uiState.relatedGames,
                            serverUrl = uiState.serverUrl,
                            countSubtitle = "${uiState.relatedGames.size} titoli disponibili",
                            onItemClick = { onNavigateToRom(it.id) }
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                    }
                }

                item {
                    Spacer(modifier = Modifier.height(48.dp))
                }
            }
        }
    }
}

@Composable
private fun InfoCard(
    title: String,
    icon: ImageVector,
    iconTint: Color,
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(BgCard)
            .border(1.dp, BorderColor, RoundedCornerShape(12.dp))
            .padding(14.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = iconTint,
                modifier = Modifier.size(16.dp)
            )
            Text(
                text = title,
                color = TextMuted,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 0.5.sp
            )
        }
        Spacer(modifier = Modifier.height(8.dp))
        content()
    }
}

@Composable
private fun InfoRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            color = TextDim,
            fontSize = 12.sp
        )
        Text(
            text = value,
            color = TextMain,
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.padding(start = 8.dp)
        )
    }
}

private fun formatTimestamp(timestamp: Long): String {
    if (timestamp <= 0) return "Mai avviato"
    val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
    return sdf.format(Date(timestamp))
}
