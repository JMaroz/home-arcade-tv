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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Star
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
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
import com.homearcade.tv.ui.theme.ArcadePink
import com.homearcade.tv.ui.theme.BgCard
import com.homearcade.tv.ui.theme.TextMain
import com.homearcade.tv.ui.theme.TextMuted
import com.homearcade.tv.ui.theme.getSystemColor

@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
fun TvGameCard(
    item: RomDto,
    modifier: Modifier = Modifier,
    serverUrl: String? = null,
    onFocused: (() -> Unit)? = null,
    onClick: () -> Unit
) {
    val cardShape = RoundedCornerShape(10.dp)
    val formattedArt = item.getFullArtUrl(serverUrl)
    val sysColor = getSystemColor(item.system)

    Column(
        modifier = modifier
            .width(145.dp)
            .padding(vertical = 8.dp)
    ) {
        Box(
            modifier = Modifier
                .width(145.dp)
                .height(205.dp)
                .tvFocusable(
                    shape = cardShape,
                    onFocusChanged = { if (it) onFocused?.invoke() },
                    onClick = onClick
                )
                .clip(cardShape)
                .background(BgCard)
        ) {
            // Box Art Image
            if (!formattedArt.isNullOrEmpty()) {
                AsyncImage(
                    model = formattedArt,
                    contentDescription = item.title,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(BgCard),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = item.title.take(3).uppercase(),
                        color = TextMuted,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            // Top Badges (System & Favorite/Rating)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // System console badge
                Text(
                    text = item.system.uppercase(),
                    color = Color.White,
                    fontSize = 9.sp,
                    fontWeight = FontWeight.ExtraBold,
                    modifier = Modifier
                        .background(
                            color = sysColor,
                            shape = RoundedCornerShape(4.dp)
                        )
                        .padding(horizontal = 5.dp, vertical = 2.dp)
                )

                Spacer(modifier = Modifier.weight(1f))

                // Favorite heart
                if (item.favorite) {
                    Box(
                        modifier = Modifier
                            .background(Color(0xDD080A10), RoundedCornerShape(4.dp))
                            .padding(horizontal = 4.dp, vertical = 2.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Favorite,
                            contentDescription = "Preferito",
                            tint = ArcadePink,
                            modifier = Modifier.size(11.dp)
                        )
                    }
                } else if (item.rating > 0) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .background(Color(0xDD080A10), RoundedCornerShape(4.dp))
                            .padding(horizontal = 4.dp, vertical = 2.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Star,
                            contentDescription = "Rating",
                            tint = AccentGold,
                            modifier = Modifier.size(11.dp)
                        )
                        Text(
                            text = "${item.rating}",
                            color = TextMain,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.padding(start = 2.dp)
                        )
                    }
                }
            }

            // Bottom Shadow gradient
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(60.dp)
                    .align(Alignment.BottomCenter)
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(Color.Transparent, Color(0xE6080A10))
                        )
                    )
            )

            // Multi-disc indicator badge
            if (item.isPlaylist || item.discGroup != null) {
                Text(
                    text = "MULTI-DISC",
                    color = Color.White,
                    fontSize = 8.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(end = 6.dp, bottom = 6.dp)
                        .background(Color(0xCC3B82F6), RoundedCornerShape(3.dp))
                        .padding(horizontal = 4.dp, vertical = 1.dp)
                )
            } else if (item.minutesPlayed > 0) {
                Text(
                    text = "${item.minutesPlayed}m",
                    color = TextMuted,
                    fontSize = 9.sp,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(end = 6.dp, bottom = 6.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        // Title
        Text(
            text = item.title,
            color = TextMain,
            fontSize = 13.sp,
            fontWeight = FontWeight.Medium,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )

        // System name or Year
        val subText = buildString {
            append(item.system.uppercase())
            if (item.releaseYear != null && item.releaseYear > 0) {
                append(" • ${item.releaseYear}")
            }
        }
        Text(
            text = subText,
            color = TextMuted,
            fontSize = 11.sp,
            maxLines = 1
        )
    }
}
