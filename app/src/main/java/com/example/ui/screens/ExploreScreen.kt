package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Clear
import androidx.compose.material.icons.outlined.ExpandLess
import androidx.compose.material.icons.outlined.ExpandMore
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.SearchOff
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.data.model.UtilityCatalog
import com.example.data.model.UtilityCategory
import com.example.data.model.UtilityIds
import com.example.ui.components.MonoChip
import com.example.ui.components.MonoEmptyState
import com.example.ui.components.UtilityListCard
import com.example.ui.theme.MonoTheme

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ExploreScreen(
    selectedCategory: UtilityCategory?,
    onSelectCategory: (UtilityCategory?) -> Unit,
    favoriteIds: Set<String>,
    onOpenTool: (String, String?) -> Unit,
    onToggleFavorite: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val palette = MonoTheme.palette
    var query by remember { mutableStateOf("") }

    // Collapsible category state when viewing All without search query
    val expandedState = remember {
        mutableStateMapOf<String, Boolean>().apply {
            UtilityCategory.entries.forEach { put(it.id, true) }
        }
    }

    val filteredTools = remember(query, selectedCategory) {
        UtilityCatalog.search(query, selectedCategory)
    }

    val grouped = remember(filteredTools) {
        UtilityCategory.entries.mapNotNull { cat ->
            val items = filteredTools.filter { it.category == cat }
            if (items.isEmpty()) null else cat to items
        }
    }

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
                        .padding(top = 8.dp),
                ) {
                    Text(
                        text = "Explore Utilities",
                        style = MaterialTheme.typography.headlineMedium,
                        color = palette.textPrimary,
                        fontWeight = FontWeight.Bold,
                    )
                    Text(
                        text = "${UtilityCatalog.allTools.size} offline tools organized by category & discipline",
                        style = MaterialTheme.typography.bodyMedium,
                        color = palette.textSecondary,
                    )
                    Spacer(Modifier.height(12.dp))
                    OutlinedTextField(
                        value = query,
                        onValueChange = { query = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("explore_search_input"),
                        placeholder = {
                            Text("Search chess, dice, basketball, cube...", color = palette.textMuted)
                        },
                        leadingIcon = {
                            Icon(Icons.Outlined.Search, contentDescription = null, tint = palette.textSecondary)
                        },
                        trailingIcon = {
                            if (query.isNotEmpty()) {
                                IconButton(onClick = { query = "" }) {
                                    Icon(Icons.Outlined.Clear, contentDescription = "Clear search", tint = palette.textSecondary)
                                }
                            }
                        },
                        singleLine = true,
                        shape = RoundedCornerShape(14.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = palette.cardPrimary,
                            unfocusedContainerColor = palette.cardPrimary,
                            focusedBorderColor = palette.accent,
                            unfocusedBorderColor = palette.border,
                            focusedTextColor = palette.textPrimary,
                            unfocusedTextColor = palette.textPrimary,
                        ),
                    )
                }
            }

            item {
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    contentPadding = PaddingValues(vertical = 4.dp),
                ) {
                    item {
                        MonoChip(
                            label = "All (${UtilityCatalog.allTools.size})",
                            selected = selectedCategory == null,
                            onClick = { onSelectCategory(null) },
                            modifier = Modifier.testTag("explore_chip_all"),
                        )
                    }
                    items(UtilityCategory.entries) { cat ->
                        val count = UtilityCatalog.byCategory(cat).size
                        MonoChip(
                            label = "${cat.title} ($count)",
                            selected = selectedCategory == cat,
                            onClick = { onSelectCategory(if (selectedCategory == cat) null else cat) },
                            modifier = Modifier.testTag("explore_chip_${cat.id}"),
                        )
                    }
                }
            }

            if (grouped.isEmpty()) {
                item {
                    MonoEmptyState(
                        icon = Icons.Outlined.SearchOff,
                        title = "No matching utilities",
                        description = "Try searching for a sport, game, puzzle, or timer name.",
                        actionText = "Reset Filters",
                        onAction = {
                            query = ""
                            onSelectCategory(null)
                        },
                    )
                }
            } else {
                grouped.forEach { (category, tools) ->
                    val isExpanded = selectedCategory != null || query.isNotBlank() || (expandedState[category.id] ?: true)

                    item(key = "banner_${category.id}") {
                        CategoryBannerHeader(
                            category = category,
                            count = tools.size,
                            expanded = isExpanded,
                            showToggle = selectedCategory == null && query.isBlank(),
                            onToggle = {
                                expandedState[category.id] = !(expandedState[category.id] ?: true)
                            },
                        )
                    }

                    if (isExpanded) {
                        // Show direct Sport Preset Launchers inside Scoreboard category
                        if (category == UtilityCategory.SCOREBOARD && query.isBlank()) {
                            item(key = "sport_shortcuts") {
                                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                    Text(
                                        text = "SPORT PRESETS",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = palette.textMuted,
                                        fontWeight = FontWeight.SemiBold,
                                    )
                                    FlowRow(
                                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                                        verticalArrangement = Arrangement.spacedBy(8.dp),
                                    ) {
                                        UtilityCatalog.sportShortcuts.forEach { (label, hint) ->
                                            MonoChip(
                                                label = label,
                                                selected = false,
                                                onClick = { onOpenTool(UtilityIds.SCOREBOARD, hint) },
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        // Group by subCategory inside each Category
                        val bySub = tools.groupBy { it.subCategory }
                        bySub.forEach { (subCat, subTools) ->
                            item(key = "sub_${category.id}_$subCat") {
                                Text(
                                    text = subCat.uppercase(),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = palette.textMuted,
                                    fontWeight = FontWeight.SemiBold,
                                    modifier = Modifier.padding(top = 4.dp),
                                )
                            }
                            items(subTools, key = { it.id }) { tool ->
                                UtilityListCard(
                                    tool = tool,
                                    isFavorite = favoriteIds.contains(tool.id),
                                    onOpen = { onOpenTool(tool.id, null) },
                                    onToggleFavorite = { onToggleFavorite(tool.id) },
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun CategoryBannerHeader(
    category: UtilityCategory,
    count: Int,
    expanded: Boolean,
    showToggle: Boolean,
    onToggle: () -> Unit,
) {
    val palette = MonoTheme.palette
    val shape = RoundedCornerShape(16.dp)
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(82.dp)
            .clip(shape)
            .border(1.dp, palette.border, shape)
            .clickable(enabled = showToggle, onClick = onToggle),
    ) {
        Image(
            painter = painterResource(id = category.bannerResId),
            contentDescription = category.title,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize(),
        )
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.horizontalGradient(
                        0.0f to Color(0xEB0A0A0A),
                        0.65f to Color(0xBA0A0A0A),
                        1.0f to Color(0x770A0A0A),
                    )
                )
        )
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 14.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier
                    .size(38.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(Color(0x99222222))
                    .border(1.dp, Color(0x44FFFFFF), RoundedCornerShape(10.dp)),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = category.icon,
                    contentDescription = category.title,
                    tint = Color.White,
                    modifier = Modifier.size(20.dp),
                )
            }
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = category.title,
                    style = MaterialTheme.typography.titleMedium,
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    text = category.subtitle,
                    style = MaterialTheme.typography.labelMedium,
                    color = Color(0xFFCCCCCC),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            Spacer(Modifier.width(8.dp))
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(50))
                    .background(Color(0xAA222222))
                    .padding(horizontal = 8.dp, vertical = 4.dp),
            ) {
                Text(
                    text = "$count",
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.White,
                    fontWeight = FontWeight.SemiBold,
                )
            }
            if (showToggle) {
                Spacer(Modifier.width(4.dp))
                Icon(
                    imageVector = if (expanded) Icons.Outlined.ExpandLess else Icons.Outlined.ExpandMore,
                    contentDescription = if (expanded) "Collapse" else "Expand",
                    tint = Color.White,
                )
            }
        }
    }
}
