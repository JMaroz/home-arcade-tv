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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
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
    val keyboardController = LocalSoftwareKeyboardController.current
    val textFocusRequester = remember { FocusRequester() }

    BackHandler {
        onBackClick()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(BgPrimary)
            .padding(horizontal = 48.dp, vertical = 20.dp)
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
                        .tvFocusable(
                            shape = inputShape,
                            onClick = {
                                textFocusRequester.requestFocus()
                                keyboardController?.show()
                            }
                        )
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
                        Box(modifier = Modifier.weight(1f)) {
                            if (uiState.query.isEmpty()) {
                                Text(
                                    text = "Cerca giochi o premi per digitare...",
                                    color = TextDim,
                                    fontSize = 14.sp
                                )
                            }
                            BasicTextField(
                                value = uiState.query,
                                onValueChange = { viewModel.onQueryChanged(it) },
                                textStyle = TextStyle(
                                    color = TextMain,
                                    fontSize = 14.sp
                                ),
                                cursorBrush = SolidColor(ArcadePink),
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                                keyboardActions = KeyboardActions(
                                    onSearch = { keyboardController?.hide() }
                                ),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .focusRequester(textFocusRequester)
                            )
                        }

                        if (uiState.query.isNotEmpty()) {
                            val clearShape = CircleShape
                            Box(
                                modifier = Modifier
                                    .size(24.dp)
                                    .tvFocusable(shape = clearShape, onClick = { viewModel.onQueryChanged("") })
                                    .clip(clearShape)
                                    .background(Color(0x33FFFFFF)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Cancella",
                                    tint = TextMuted,
                                    modifier = Modifier.size(14.dp)
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Quick Filters for remote control users
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(viewModel.quickTags) { tag ->
                    val isSelected = (tag == "Tutti" && uiState.query.isEmpty()) || uiState.query.equals(tag, ignoreCase = true)
                    val tagShape = RoundedCornerShape(16.dp)
                    Box(
                        modifier = Modifier
                            .tvFocusable(
                                shape = tagShape,
                                onClick = { viewModel.onQueryChanged(if (tag == "Tutti") "" else tag) }
                            )
                            .clip(tagShape)
                            .background(if (isSelected) ArcadePink else Color(0x26FFFFFF))
                            .padding(horizontal = 14.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = tag,
                            color = if (isSelected) Color.White else TextMain,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Search Results Count / Catalog label
            val titleText = if (uiState.query.isEmpty()) {
                "Catalogo Giochi (${uiState.results.size} titoli disponibili)"
            } else {
                "${uiState.results.size} risultati trovati per \"${uiState.query}\""
            }
            Text(
                text = titleText,
                color = TextMuted,
                fontSize = 13.sp
            )
            Spacer(modifier = Modifier.height(10.dp))

            // Results Grid
            if (uiState.results.isEmpty() && uiState.query.isNotEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Nessun gioco trovato per \"${uiState.query}\".",
                        color = TextDim,
                        fontSize = 16.sp
                    )
                }
            } else {
                LazyVerticalGrid(
                    columns = GridCells.Adaptive(minSize = 120.dp),
                    contentPadding = PaddingValues(bottom = 32.dp),
                    horizontalArrangement = Arrangement.spacedBy(14.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(
                        items = uiState.results,
                        key = { it.id },
                        contentType = { "game_card" }
                    ) { rom ->
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
