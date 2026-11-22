package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ArrowDownward
import androidx.compose.material.icons.outlined.ArrowUpward
import androidx.compose.material.icons.outlined.StarBorder
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.data.model.UtilityTool
import com.example.ui.components.MonoCard
import com.example.ui.components.MonoEmptyState
import com.example.ui.theme.MonoTheme

@Composable
fun FavoritesScreen(
    favoriteTools: List<UtilityTool>,
    onOpenTool: (String) -> Unit,
    onMoveFavorite: (String, Int) -> Unit,
    onRemoveFavorite: (String) -> Unit,
    onExploreClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val palette = MonoTheme.palette

    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.TopCenter,
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .widthIn(max = 840.dp),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 28.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .statusBarsPadding()
                        .padding(top = 8.dp, bottom = 4.dp),
                ) {
                    Text(
                        text = "Favorites",
                        style = MaterialTheme.typography.headlineMedium,
                        color = palette.textPrimary,
                        fontWeight = FontWeight.Bold,
                    )
                    Text(
                        text = "Pin and reorder your most-used tabletop tools",
                        style = MaterialTheme.typography.bodyMedium,
                        color = palette.textSecondary,
                    )
                }
            }

            if (favoriteTools.isEmpty()) {
                item {
                    MonoEmptyState(
                        icon = Icons.Outlined.StarBorder,
                        title = "No favorites pinned yet",
                        description = "Tap the star icon on any utility in Explore or inside a tool to pin it here and on Home.",
                        actionText = "Browse Utilities",
                        onAction = onExploreClick,
                    )
                }
            } else {
                itemsIndexed(favoriteTools, key = { _, tool -> tool.id }) { index, tool ->
                    MonoCard(
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("favorite_item_${tool.id}"),
                        onClick = { onOpenTool(tool.id) },
                        contentPadding = PaddingValues(start = 14.dp, end = 8.dp, top = 12.dp, bottom = 6.dp),
                    ) {
                        Column(modifier = Modifier.fillMaxWidth()) {
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(end = 6.dp),
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(44.dp)
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(palette.surfaceSecondary)
                                        .border(1.dp, palette.border, RoundedCornerShape(12.dp)),
                                    contentAlignment = Alignment.Center,
                                ) {
                                    Icon(
                                        imageVector = tool.icon,
                                        contentDescription = tool.title,
                                        tint = palette.textPrimary,
                                        modifier = Modifier.size(22.dp),
                                    )
                                }
                                Spacer(Modifier.width(12.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "${tool.category.title} • ${tool.subCategory}",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = palette.textMuted,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis,
                                    )
                                    Text(
                                        text = tool.title,
                                        style = MaterialTheme.typography.titleMedium,
                                        color = palette.textPrimary,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis,
                                    )
                                    Text(
                                        text = tool.subtitle,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = palette.textSecondary,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis,
                                    )
                                }
                            }
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.End,
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                IconButton(
                                    onClick = { onMoveFavorite(tool.id, -1) },
                                    enabled = index > 0,
                                    modifier = Modifier.minimumInteractiveComponentSize(),
                                ) {
                                    Icon(
                                        imageVector = Icons.Outlined.ArrowUpward,
                                        contentDescription = "Move up",
                                        tint = if (index > 0) palette.textPrimary else palette.textMuted.copy(alpha = 0.35f),
                                        modifier = Modifier.size(18.dp),
                                    )
                                }
                                IconButton(
                                    onClick = { onMoveFavorite(tool.id, 1) },
                                    enabled = index < favoriteTools.lastIndex,
                                    modifier = Modifier.minimumInteractiveComponentSize(),
                                ) {
                                    Icon(
                                        imageVector = Icons.Outlined.ArrowDownward,
                                        contentDescription = "Move down",
                                        tint = if (index < favoriteTools.lastIndex) palette.textPrimary else palette.textMuted.copy(alpha = 0.35f),
                                        modifier = Modifier.size(18.dp),
                                    )
                                }
                                IconButton(
                                    onClick = { onRemoveFavorite(tool.id) },
                                    modifier = Modifier.minimumInteractiveComponentSize(),
                                ) {
                                    Icon(
                                        imageVector = Icons.Outlined.StarBorder,
                                        contentDescription = "Unpin ${tool.title}",
                                        tint = palette.textSecondary,
                                        modifier = Modifier.size(20.dp),
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
