package com.homearcade.tv.ui.components

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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Star
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.tv.material3.ExperimentalTvMaterial3Api
import androidx.tv.material3.Icon
import androidx.tv.material3.Text
import coil3.compose.AsyncImage
import com.homearcade.tv.data.model.RomDto
import com.homearcade.tv.ui.theme.AccentGold
import com.homearcade.tv.ui.theme.BgPrimary
import com.homearcade.tv.ui.theme.TextDim
import com.homearcade.tv.ui.theme.TextMain
import com.homearcade.tv.ui.theme.TextMuted
import com.homearcade.tv.ui.theme.getSystemColor

@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
fun HeroBillboard(
    item: RomDto?,
    modifier: Modifier = Modifier,
    serverUrl: String? = null,
    playButtonFocusRequester: FocusRequester? = null,
    onPlayClick: () -> Unit,
    onDetailsClick: () -> Unit,
    onToggleFavorite: () -> Unit
) {
    val backdropUrl = item?.getFullArtUrl(serverUrl)
    val sysColor = if (item != null) getSystemColor(item.system) else Color.Transparent

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(380.dp)
            .background(BgPrimary)
    ) {
        // High-res Background Artwork
        if (!backdropUrl.isNullOrEmpty()) {
            AsyncImage(
                model = backdropUrl,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                alignment = Alignment.CenterEnd,
                modifier = Modifier
                    .fillMaxSize()
                    .align(Alignment.CenterEnd)
            )
        }

        // Horizontal scrim gradient (left-to-right fade for text readability)
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.horizontalGradient(
                        colors = listOf(
                            BgPrimary,
                            BgPrimary.copy(alpha = 0.95f),
                            BgPrimary.copy(alpha = 0.70f),
                            Color.Transparent
                        ),
                        startX = 0f,
                        endX = 1100f
                    )
                )
        )

        // Vertical bottom gradient (smooth transition into lower shelves)
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color.Transparent,
                            BgPrimary.copy(alpha = 0.5f),
                            BgPrimary
                        ),
                        startY = 150f
                    )
                )
        )

        // Content Details
        if (item != null) {
            Column(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(start = 48.dp, bottom = 24.dp)
                    .width(580.dp)
            ) {
                // System Badge & Metadata Row
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = item.system.uppercase(),
                        color = Color.White,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.ExtraBold,
                        modifier = Modifier
                            .background(sysColor, RoundedCornerShape(4.dp))
                            .padding(horizontal = 7.dp, vertical = 2.dp)
                    )

                    if (item.releaseYear != null && item.releaseYear > 0) {
                        Text(
                            text = "${item.releaseYear}",
                            color = TextMuted,
                            fontSize = 12.sp
                        )
                    }

                    if (!item.players.isNullOrEmpty()) {
                        Text(
                            text = "•  ${item.players} Giocatori",
                            color = TextMuted,
                            fontSize = 12.sp
                        )
                    }

                    if (!item.genre.isNullOrEmpty()) {
                        Text(
                            text = "•  ${item.genre}",
                            color = TextMuted,
                            fontSize = 12.sp
                        )
                    }

                    if (item.rating > 0) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Star,
                                contentDescription = null,
                                tint = AccentGold,
                                modifier = Modifier.height(13.dp)
                            )
                            Text(
                                text = " ${item.rating}/5",
                                color = TextMain,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Title
                Text(
                    text = item.title,
                    color = TextMain,
                    fontSize = 28.sp,
                    fontWeight = FontWeight.ExtraBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(6.dp))

                // Description
                val synopsis = item.description ?: "Nessuna descrizione disponibile per questo titolo retro."
                Text(
                    text = synopsis,
                    color = TextDim,
                    fontSize = 13.sp,
                    lineHeight = 18.sp,
                    maxLines = 3,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Action Buttons
                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    TvButton(
                        text = "Gioca Subito",
                        icon = Icons.Default.PlayArrow,
                        isPrimary = true,
                        modifier = if (playButtonFocusRequester != null) Modifier.focusRequester(playButtonFocusRequester) else Modifier,
                        onClick = onPlayClick
                    )

                    TvButton(
                        text = "Dettagli",
                        icon = Icons.Default.Info,
                        onClick = onDetailsClick
                    )

                    TvButton(
                        text = if (item.favorite) "Nei Preferiti" else "Aggiungi",
                        icon = if (item.favorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                        onClick = onToggleFavorite
                    )
                }
            }
        }
    }
}
