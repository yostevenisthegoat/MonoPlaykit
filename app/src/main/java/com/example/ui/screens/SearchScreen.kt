package com.example.ui.screens

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
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.Clear
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.SearchOff
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.model.UtilityCatalog
import com.example.data.model.UtilityCategory
import com.example.ui.components.MonoChip
import com.example.ui.components.MonoEmptyState
import com.example.ui.components.UtilityListCard
import com.example.ui.theme.MonoTheme

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun SearchScreen(
    recentSearches: List<String>,
    favoriteIds: Set<String>,
    onBack: () -> Unit,
    onOpenTool: (String, String?) -> Unit,
    onSaveSearch: (String) -> Unit,
    onClearRecentSearches: () -> Unit,
    onToggleFavorite: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val palette = MonoTheme.palette
    var query by remember { mutableStateOf("") }
    var categoryFilter by remember { mutableStateOf<UtilityCategory?>(null) }

    val results = remember(query, categoryFilter) {
        UtilityCatalog.search(query, categoryFilter)
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
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .statusBarsPadding()
                        .padding(top = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    IconButton(
                        onClick = onBack,
                        modifier = Modifier.minimumInteractiveComponentSize(),
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Outlined.ArrowBack,
                            contentDescription = "Back",
                            tint = palette.textPrimary,
                        )
                    }
                    OutlinedTextField(
                        value = query,
                        onValueChange = { query = it },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("search_screen_input"),
                        placeholder = { Text("Search tools, sports, puzzles...", color = palette.textMuted) },
                        leadingIcon = { Icon(Icons.Outlined.Search, contentDescription = null, tint = palette.textSecondary) },
                        trailingIcon = {
                            if (query.isNotEmpty()) {
                                IconButton(onClick = { query = "" }) {
                                    Icon(Icons.Outlined.Clear, contentDescription = "Clear", tint = palette.textSecondary)
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
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    item {
                        MonoChip(
                            label = "All",
                            selected = categoryFilter == null,
                            onClick = { categoryFilter = null },
                        )
                    }
                    items(UtilityCategory.entries) { cat ->
                        MonoChip(
                            label = cat.title,
                            selected = categoryFilter == cat,
                            onClick = { categoryFilter = if (categoryFilter == cat) null else cat },
                        )
                    }
                }
            }

            if (query.isBlank() && categoryFilter == null) {
                if (recentSearches.isNotEmpty()) {
                    item {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Text(
                                    text = "Recently Searched",
                                    style = MaterialTheme.typography.titleSmall,
                                    color = palette.textPrimary,
                                    fontWeight = FontWeight.SemiBold,
                                )
                                TextButton(onClick = onClearRecentSearches) {
                                    Text("Clear", color = palette.textMuted)
                                }
                            }
                            FlowRow(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp),
                            ) {
                                recentSearches.forEach { term ->
                                    MonoChip(
                                        label = term,
                                        selected = false,
                                        onClick = { query = term },
                                    )
                                }
                            }
                        }
                    }
                }

                item {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            text = "Suggested Searches",
                            style = MaterialTheme.typography.titleSmall,
                            color = palette.textPrimary,
                            fontWeight = FontWeight.SemiBold,
                        )
                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            UtilityCatalog.searchSuggestions.forEach { suggestion ->
                                MonoChip(
                                    label = suggestion,
                                    selected = false,
                                    onClick = { query = suggestion },
                                )
                            }
                        }
                    }
                }
            }

            if (results.isEmpty()) {
                item {
                    MonoEmptyState(
                        icon = Icons.Outlined.SearchOff,
                        title = "No tools found for \"$query\"",
                        description = "Try searching \"Chess\", \"Dice\", \"Basketball\", \"Cube\", or \"Stopwatch\".",
                    )
                }
            } else {
                item {
                    Spacer(Modifier.height(2.dp))
                    Text(
                        text = if (query.isBlank() && categoryFilter == null) "All Utilities" else "${results.size} Results",
                        style = MaterialTheme.typography.labelLarge,
                        color = palette.textMuted,
                    )
                }
                items(results, key = { it.id }) { tool ->
                    UtilityListCard(
                        tool = tool,
                        isFavorite = favoriteIds.contains(tool.id),
                        onOpen = {
                            if (query.isNotBlank()) onSaveSearch(query)
                            onOpenTool(tool.id, query.takeIf { it.isNotBlank() })
                        },
                        onToggleFavorite = { onToggleFavorite(tool.id) },
                    )
                }
            }
        }
    }
}
