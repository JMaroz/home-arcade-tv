package com.homearcade.tv.ui.search

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Search
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.tv.material3.ExperimentalTvMaterial3Api
import androidx.tv.material3.Icon
import androidx.tv.material3.Text
import com.homearcade.tv.ui.components.TvGameCard
import com.homearcade.tv.ui.components.tvFocusable
import com.homearcade.tv.ui.theme.ArcadePink
import com.homearcade.tv.ui.theme.BgCard
import com.homearcade.tv.ui.theme.BgPrimary
import com.homearcade.tv.ui.theme.TextDim
import com.homearcade.tv.ui.theme.TextMain
import com.homearcade.tv.ui.theme.TextMuted

@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
fun SearchScreen(
    viewModel: SearchViewModel,
    onBackClick: () -> Unit,
    onNavigateToDetails: (romId: Int) -> Unit
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
        Column(modifier = Modifier.fillMaxSize()) {
            // Search Bar Header
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
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

                val inputShape = RoundedCornerShape(12.dp)
                Box(
                    modifier = Modifier
                        .fillMaxWidth(0.7f)
                        .clip(inputShape)
                        .background(BgCard)
                        .tvFocusable(shape = inputShape)
                        .padding(horizontal = 16.dp, vertical = 12.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = null,
                            tint = ArcadePink,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        BasicTextField(
                            value = uiState.query,
                            onValueChange = { viewModel.onQueryChanged(it) },
                            textStyle = TextStyle(
                                color = TextMain,
                                fontSize = 15.sp
                            ),
                            cursorBrush = SolidColor(ArcadePink),
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Search Results Count
            if (uiState.query.isNotEmpty()) {
                Text(
                    text = "${uiState.results.size} risultati trovati per \"${uiState.query}\"",
                    color = TextMuted,
                    fontSize = 14.sp
                )
                Spacer(modifier = Modifier.height(14.dp))
            }

            // Results Grid
            if (uiState.results.isEmpty() && uiState.query.isNotEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Nessun gioco trovato con questo nome.",
                        color = TextDim,
                        fontSize = 16.sp
                    )
                }
            } else {
                LazyVerticalGrid(
                    columns = GridCells.Adaptive(minSize = 145.dp),
                    contentPadding = PaddingValues(bottom = 32.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(uiState.results, key = { it.id }) { rom ->
                        TvGameCard(
                            item = rom,
                            serverUrl = uiState.serverUrl,
                            onClick = { onNavigateToDetails(rom.id) }
                        )
                    }
                }
            }
        }
    }
}
