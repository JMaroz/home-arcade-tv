package com.homearcade.tv.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.tv.material3.ExperimentalTvMaterial3Api
import androidx.tv.material3.Text
import com.homearcade.tv.data.model.RomDto
import com.homearcade.tv.ui.theme.TextMain
import com.homearcade.tv.ui.theme.TextMuted

@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
fun TvShelfRow(
    title: String,
    items: List<RomDto>,
    modifier: Modifier = Modifier,
    serverUrl: String? = null,
    countSubtitle: String? = null,
    onItemFocused: ((RomDto) -> Unit)? = null,
    onItemClick: (RomDto) -> Unit
) {
    if (items.isEmpty()) return

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp)
    ) {
        // Shelf Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 48.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = title,
                color = TextMain,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )

            if (!countSubtitle.isNullOrEmpty()) {
                Text(
                    text = " • $countSubtitle",
                    color = TextMuted,
                    fontSize = 13.sp,
                    modifier = Modifier.padding(start = 6.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        // Horizontal Row with 48dp TV overscan safe padding
        LazyRow(
            contentPadding = PaddingValues(horizontal = 48.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            items(items, key = { it.id }) { item ->
                TvGameCard(
                    item = item,
                    serverUrl = serverUrl,
                    onFocused = { onItemFocused?.invoke(item) },
                    onClick = { onItemClick(item) }
                )
            }
        }
    }
}
