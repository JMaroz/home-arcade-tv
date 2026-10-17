package com.homearcade.tv.ui.profiles

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.tv.material3.ExperimentalTvMaterial3Api
import androidx.tv.material3.Icon
import androidx.tv.material3.Text
import com.homearcade.tv.data.model.ProfileDto
import com.homearcade.tv.ui.components.TvButton
import com.homearcade.tv.ui.components.TvLoadingSpinner
import com.homearcade.tv.ui.components.tvFocusable
import com.homearcade.tv.ui.theme.ArcadePink
import com.homearcade.tv.ui.theme.BgCard
import com.homearcade.tv.ui.theme.BgPrimary
import com.homearcade.tv.ui.theme.TextDim
import com.homearcade.tv.ui.theme.TextMain
import com.homearcade.tv.ui.theme.TextMuted

@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
fun ProfilePickerScreen(
    viewModel: ProfileViewModel,
    onProfileSelected: () -> Unit
) {
    val profiles by viewModel.profiles.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.errorMessage.collectAsState()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(BgPrimary)
            .padding(48.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Chi sta giocando?",
                color = TextMain,
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = "Seleziona il tuo profilo per accedere ai tuoi salvataggi e preferiti",
                color = TextMuted,
                fontSize = 15.sp,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(48.dp))

            if (isLoading) {
                TvLoadingSpinner(size = 48.dp)
            } else if (error != null) {
                Text(
                    text = error ?: "Errore nel caricamento dei profili",
                    color = ArcadePink,
                    fontSize = 14.sp
                )
                Spacer(modifier = Modifier.height(16.dp))
                TvButton(
                    text = "Riprova",
                    icon = Icons.Default.Refresh,
                    onClick = { viewModel.loadProfiles() }
                )
            } else {
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 24.dp),
                    horizontalArrangement = Arrangement.spacedBy(28.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    items(profiles, key = { it.id }) { profile ->
                        ProfileCard(
                            profile = profile,
                            onClick = {
                                viewModel.onProfileSelected(profile, onProfileSelected)
                            }
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
private fun ProfileCard(
    profile: ProfileDto,
    onClick: () -> Unit
) {
    val cardShape = RoundedCornerShape(14.dp)
    val parsedColor = try {
        Color(android.graphics.Color.parseColor(profile.color))
    } catch (_: Exception) {
        ArcadePink
    }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.width(140.dp)
    ) {
        Box(
            modifier = Modifier
                .size(130.dp)
                .tvFocusable(shape = cardShape, onClick = onClick)
                .clip(cardShape)
                .background(BgCard),
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .size(72.dp)
                    .clip(CircleShape)
                    .background(parsedColor),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.AccountCircle,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(54.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = profile.name,
            color = TextMain,
            fontSize = 16.sp,
            fontWeight = FontWeight.SemiBold
        )
    }
}
