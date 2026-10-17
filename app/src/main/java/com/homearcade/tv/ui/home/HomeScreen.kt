package com.homearcade.tv.ui.home

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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.SportsEsports
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.tv.material3.ExperimentalTvMaterial3Api
import androidx.tv.material3.Icon
import androidx.tv.material3.Text
import com.homearcade.tv.ui.components.HeroBillboard
import com.homearcade.tv.ui.components.TvButton
import com.homearcade.tv.ui.components.TvLoadingSpinner
import com.homearcade.tv.ui.components.TvShelfRow
import com.homearcade.tv.ui.components.tvFocusable
import com.homearcade.tv.ui.theme.ArcadePink
import com.homearcade.tv.ui.theme.BgPrimary
import com.homearcade.tv.ui.theme.TextDim
import com.homearcade.tv.ui.theme.TextMain
import com.homearcade.tv.ui.theme.TextMuted
import com.homearcade.tv.ui.theme.getSystemColor

@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: HomeViewModel,
    onNavigateToDetails: (romId: Int) -> Unit,
    onLaunchGame: (romId: Int) -> Unit,
    onSearchClick: () -> Unit,
    onProfileClick: () -> Unit,
    onSettingsClick: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(BgPrimary)
    ) {
        if (uiState.isLoading && uiState.heroRom == null) {
            TvLoadingSpinner(size = 54.dp)
        } else if (uiState.errorMessage != null && uiState.heroRom == null) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(48.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Errore di Connessione",
                    color = ArcadePink,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = uiState.errorMessage ?: "",
                    color = TextMuted,
                    fontSize = 14.sp
                )
                Spacer(modifier = Modifier.height(24.dp))
                TvButton(
                    text = "Riprova",
                    icon = Icons.Default.Refresh,
                    onClick = { viewModel.loadCatalog(forceRefresh = true) }
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize()
            ) {
                // 1. Top TV Navigation Bar
                item {
                    TopTvNavigationBar(
                        profileName = uiState.profileName,
                        selectedSection = uiState.selectedSection,
                        onSelectSection = { viewModel.onSelectSection(it) },
                        onSearchClick = onSearchClick,
                        onProfileClick = onProfileClick,
                        onSettingsClick = onSettingsClick
                    )
                }

                // 2. Dynamic Hero Billboard
                item {
                    HeroBillboard(
                        item = uiState.heroRom,
                        serverUrl = uiState.serverUrl,
                        onPlayClick = {
                            uiState.heroRom?.let { onLaunchGame(it.id) }
                        },
                        onDetailsClick = {
                            uiState.heroRom?.let { onNavigateToDetails(it.id) }
                        },
                        onToggleFavorite = {
                            uiState.heroRom?.let { viewModel.toggleFavorite(it) }
                        }
                    )
                }

                // 3. Dynamic Shelves based on selected Section
                when (uiState.selectedSection) {
                    HomeSection.ALL -> {
                        // Recently Played
                        if (uiState.recentlyPlayed.isNotEmpty()) {
                            item {
                                TvShelfRow(
                                    title = "Continua a Giocare",
                                    items = uiState.recentlyPlayed,
                                    serverUrl = uiState.serverUrl,
                                    countSubtitle = "${uiState.recentlyPlayed.size} titoli",
                                    onItemFocused = { viewModel.onCardFocused(it) },
                                    onItemClick = { onNavigateToDetails(it.id) }
                                )
                            }
                        }

                        // Favorites
                        if (uiState.favorites.isNotEmpty()) {
                            item {
                                TvShelfRow(
                                    title = "I Tuoi Preferiti",
                                    items = uiState.favorites,
                                    serverUrl = uiState.serverUrl,
                                    countSubtitle = "${uiState.favorites.size} giochi",
                                    onItemFocused = { viewModel.onCardFocused(it) },
                                    onItemClick = { onNavigateToDetails(it.id) }
                                )
                            }
                        }

                        // Latest Added
                        if (uiState.latestAdded.isNotEmpty()) {
                            item {
                                TvShelfRow(
                                    title = "Ultimi Aggiunti",
                                    items = uiState.latestAdded.take(20),
                                    serverUrl = uiState.serverUrl,
                                    countSubtitle = "${uiState.latestAdded.size} giochi",
                                    onItemFocused = { viewModel.onCardFocused(it) },
                                    onItemClick = { onNavigateToDetails(it.id) }
                                )
                            }
                        }

                        // Console Shelves
                        uiState.allSystems.forEach { system ->
                            val systemRoms = uiState.systemShelves[system] ?: emptyList()
                            if (systemRoms.isNotEmpty()) {
                                item(key = "shelf_$system") {
                                    TvShelfRow(
                                        title = system.uppercase(),
                                        items = systemRoms,
                                        serverUrl = uiState.serverUrl,
                                        countSubtitle = "${systemRoms.size} titoli",
                                        onItemFocused = { viewModel.onCardFocused(it) },
                                        onItemClick = { onNavigateToDetails(it.id) }
                                    )
                                }
                            }
                        }
                    }

                    HomeSection.FAVORITES -> {
                        item {
                            TvShelfRow(
                                title = "Catalogo Preferiti",
                                items = uiState.favorites,
                                serverUrl = uiState.serverUrl,
                                countSubtitle = "${uiState.favorites.size} giochi",
                                onItemFocused = { viewModel.onCardFocused(it) },
                                onItemClick = { onNavigateToDetails(it.id) }
                            )
                        }
                    }

                    HomeSection.RECENT -> {
                        item {
                            TvShelfRow(
                                title = "Cronologia Recenti",
                                items = uiState.recentlyPlayed,
                                serverUrl = uiState.serverUrl,
                                countSubtitle = "${uiState.recentlyPlayed.size} giocati di recente",
                                onItemFocused = { viewModel.onCardFocused(it) },
                                onItemClick = { onNavigateToDetails(it.id) }
                            )
                        }
                    }

                    HomeSection.SYSTEMS -> {
                        // Console Pill Selector
                        item {
                            LazyRow(
                                contentPadding = PaddingValues(horizontal = 48.dp, vertical = 12.dp),
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                items(uiState.allSystems) { sys ->
                                    val isSelected = sys == uiState.selectedSystemFilter
                                    val pillShape = RoundedCornerShape(20.dp)
                                    val sysColor = getSystemColor(sys)

                                    Box(
                                        modifier = Modifier
                                            .tvFocusable(shape = pillShape, onClick = { viewModel.onSelectSystemFilter(sys) })
                                            .clip(pillShape)
                                            .background(if (isSelected) sysColor else Color(0x26FFFFFF))
                                            .padding(horizontal = 16.dp, vertical = 7.dp)
                                    ) {
                                        Text(
                                            text = sys.uppercase(),
                                            color = if (isSelected) Color.White else TextMain,
                                            fontSize = 13.sp,
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                                        )
                                    }
                                }
                            }
                        }

                        val activeSys = uiState.selectedSystemFilter ?: uiState.allSystems.firstOrNull() ?: ""
                        val filteredRoms = uiState.systemShelves[activeSys] ?: emptyList()
                        item {
                            TvShelfRow(
                                title = "Giochi per ${activeSys.uppercase()}",
                                items = filteredRoms,
                                serverUrl = uiState.serverUrl,
                                countSubtitle = "${filteredRoms.size} titoli",
                                onItemFocused = { viewModel.onCardFocused(it) },
                                onItemClick = { onNavigateToDetails(it.id) }
                            )
                        }
                    }
                }

                item {
                    Spacer(modifier = Modifier.height(48.dp))
                }
            }
        }
    }
}

@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
private fun TopTvNavigationBar(
    profileName: String,
    selectedSection: HomeSection,
    onSelectSection: (HomeSection) -> Unit,
    onSearchClick: () -> Unit,
    onProfileClick: () -> Unit,
    onSettingsClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 48.dp, vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Brand Logo
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = Icons.Default.SportsEsports,
                contentDescription = null,
                tint = ArcadePink,
                modifier = Modifier.size(28.dp)
            )
            Spacer(modifier = Modifier.width(10.dp))
            Row {
                Text(
                    text = "HomeArcade",
                    color = TextMain,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = " TV",
                    color = ArcadePink,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.ExtraBold
                )
            }
        }

        // Section Tabs
        Row(
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(start = 28.dp)
        ) {
            HomeSection.entries.forEach { section ->
                val isSelected = section == selectedSection
                val shape = RoundedCornerShape(20.dp)
                Box(
                    modifier = Modifier
                        .tvFocusable(shape = shape, onClick = { onSelectSection(section) })
                        .clip(shape)
                        .background(if (isSelected) ArcadePink else Color(0x26FFFFFF))
                        .padding(horizontal = 16.dp, vertical = 7.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = section.title,
                        color = if (isSelected) Color.White else TextMain,
                        fontSize = 13.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                    )
                }
            }
        }

        Spacer(modifier = Modifier.weight(1f))

        // Action Icons
        Row(
            horizontalArrangement = Arrangement.spacedBy(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Search
            NavIconButton(
                icon = Icons.Default.Search,
                label = "Cerca",
                onClick = onSearchClick
            )

            // Profile Pill
            val pillShape = RoundedCornerShape(20.dp)
            Row(
                modifier = Modifier
                    .tvFocusable(shape = pillShape, onClick = onProfileClick)
                    .clip(pillShape)
                    .background(Color(0x33FFFFFF))
                    .padding(horizontal = 12.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.AccountCircle,
                    contentDescription = null,
                    tint = ArcadePink,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = profileName,
                    color = TextMain,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }

            // Settings
            NavIconButton(
                icon = Icons.Default.Settings,
                label = "Impostazioni",
                onClick = onSettingsClick
            )
        }
    }
}

@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
private fun NavIconButton(
    icon: ImageVector,
    label: String,
    onClick: () -> Unit
) {
    val shape = CircleShape
    Box(
        modifier = Modifier
            .size(38.dp)
            .tvFocusable(shape = shape, onClick = onClick)
            .clip(shape)
            .background(Color(0x33FFFFFF)),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = TextMain,
            modifier = Modifier.size(18.dp)
        )
    }
}
