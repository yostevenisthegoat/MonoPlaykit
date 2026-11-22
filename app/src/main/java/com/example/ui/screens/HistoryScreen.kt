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
import androidx.compose.material.icons.outlined.DeleteOutline
import androidx.compose.material.icons.outlined.DeleteSweep
import androidx.compose.material.icons.outlined.History
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.data.local.HistoryRecordEntity
import com.example.data.model.UtilityCatalog
import com.example.data.model.UtilityCategory
import com.example.ui.components.MonoCard
import com.example.ui.components.MonoChip
import com.example.ui.components.MonoEmptyState
import com.example.ui.theme.MonoTheme
import com.example.ui.theme.TabularNumeralStyle
import com.example.util.TimeFormatter

@Composable
fun HistoryScreen(
    records: List<HistoryRecordEntity>,
    onDeleteRecord: (Long) -> Unit,
    onClearAll: () -> Unit,
    onOpenTool: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val palette = MonoTheme.palette
    var selectedFilter by remember { mutableStateOf<String?>(null) }
    var selectedRecord by remember { mutableStateOf<HistoryRecordEntity?>(null) }
    var showClearDialog by remember { mutableStateOf(false) }

    val filtered = remember(records, selectedFilter) {
        if (selectedFilter == null) records else records.filter { it.categoryId == selectedFilter }
    }

    val filterCategories = remember {
        listOf(
            null to "All",
            UtilityCategory.SCOREBOARD.id to "Scoreboard",
            UtilityCategory.BOARD_GAMES.id to "Board Games",
            UtilityCategory.PUZZLES.id to "Puzzles",
            UtilityCategory.DICE_RANDOMIZERS.id to "Dice & RNG",
            UtilityCategory.TIMERS_COUNTERS.id to "Timers",
            UtilityCategory.CARD_GAMES.id to "Card Games",
            UtilityCategory.PHYSICAL_TOYS.id to "Toys",
        )
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
                        .padding(top = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "History",
                            style = MaterialTheme.typography.headlineMedium,
                            color = palette.textPrimary,
                            fontWeight = FontWeight.Bold,
                        )
                        Text(
                            text = "${records.size} saved local sessions & records",
                            style = MaterialTheme.typography.bodyMedium,
                            color = palette.textSecondary,
                        )
                    }
                    if (records.isNotEmpty()) {
                        IconButton(
                            onClick = { showClearDialog = true },
                            modifier = Modifier
                                .minimumInteractiveComponentSize()
                                .testTag("clear_history_button"),
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.DeleteSweep,
                                contentDescription = "Clear all history",
                                tint = palette.textSecondary,
                            )
                        }
                    }
                }
            }

            item {
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(filterCategories) { (catId, label) ->
                        MonoChip(
                            label = label,
                            selected = selectedFilter == catId,
                            onClick = { selectedFilter = catId },
                        )
                    }
                }
            }

            if (filtered.isEmpty()) {
                item {
                    MonoEmptyState(
                        icon = Icons.Outlined.History,
                        title = "No history recorded yet",
                        description = "Cube solves, scoreboard matches, stopwatch runs, and dice rolls saved locally will appear here.",
                    )
                }
            } else {
                items(filtered, key = { it.id }) { record ->
                    val tool = UtilityCatalog.findById(record.toolId)
                    MonoCard(
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("history_record_${record.id}"),
                        onClick = { selectedRecord = record },
                        contentPadding = PaddingValues(14.dp),
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(42.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(palette.surfaceSecondary)
                                    .border(1.dp, palette.border, RoundedCornerShape(12.dp)),
                                contentAlignment = Alignment.Center,
                            ) {
                                Icon(
                                    imageVector = tool?.icon ?: Icons.Outlined.History,
                                    contentDescription = record.title,
                                    tint = palette.textPrimary,
                                    modifier = Modifier.size(20.dp),
                                )
                            }
                            Spacer(Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically,
                                ) {
                                    Text(
                                        text = record.title,
                                        style = MaterialTheme.typography.titleSmall,
                                        color = palette.textPrimary,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis,
                                        modifier = Modifier.weight(1f),
                                    )
                                    if (record.primaryValue.isNotBlank()) {
                                        Spacer(Modifier.width(8.dp))
                                        Text(
                                            text = record.primaryValue,
                                            style = TabularNumeralStyle.copy(
                                                fontSize = MaterialTheme.typography.titleSmall.fontSize,
                                                fontWeight = FontWeight.Bold,
                                            ),
                                            color = palette.accent,
                                        )
                                    }
                                }
                                Spacer(Modifier.height(2.dp))
                                Text(
                                    text = record.summary,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = palette.textSecondary,
                                    maxLines = 2,
                                    overflow = TextOverflow.Ellipsis,
                                )
                                Spacer(Modifier.height(4.dp))
                                Text(
                                    text = TimeFormatter.formatTimestamp(record.timestamp),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = palette.textMuted,
                                )
                            }
                            IconButton(
                                onClick = { onDeleteRecord(record.id) },
                                modifier = Modifier.minimumInteractiveComponentSize(),
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.DeleteOutline,
                                    contentDescription = "Delete record",
                                    tint = palette.textMuted,
                                    modifier = Modifier.size(19.dp),
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    selectedRecord?.let { rec ->
        AlertDialog(
            onDismissRequest = { selectedRecord = null },
            containerColor = palette.cardElevated,
            titleContentColor = palette.textPrimary,
            textContentColor = palette.textSecondary,
            title = { Text(rec.title) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    if (rec.primaryValue.isNotBlank()) {
                        Text(
                            text = rec.primaryValue,
                            style = TabularNumeralStyle.copy(
                                fontSize = MaterialTheme.typography.headlineMedium.fontSize,
                                fontWeight = FontWeight.Bold,
                            ),
                            color = palette.textPrimary,
                        )
                    }
                    Text(rec.summary)
                    if (rec.detailText.isNotBlank()) {
                        Text(
                            text = rec.detailText,
                            style = MaterialTheme.typography.bodyMedium,
                            color = palette.textMuted,
                        )
                    }
                    Text(
                        text = "Recorded: ${TimeFormatter.formatTimestamp(rec.timestamp)}",
                        style = MaterialTheme.typography.labelMedium,
                        color = palette.textMuted,
                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        val toolId = rec.toolId
                        selectedRecord = null
                        onOpenTool(toolId)
                    }
                ) {
                    Text("Open Tool", color = palette.accent)
                }
            },
            dismissButton = {
                TextButton(onClick = { selectedRecord = null }) {
                    Text("Close", color = palette.textSecondary)
                }
            },
        )
    }

    if (showClearDialog) {
        AlertDialog(
            onDismissRequest = { showClearDialog = false },
            containerColor = palette.cardElevated,
            titleContentColor = palette.textPrimary,
            textContentColor = palette.textSecondary,
            title = { Text("Clear All History?") },
            text = { Text("This permanently deletes all saved cube solves, scoreboards, timer logs, and dice history on this device.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        onClearAll()
                        showClearDialog = false
                    }
                ) {
                    Text("Clear All", color = palette.teamRed)
                }
            },
            dismissButton = {
                TextButton(onClick = { showClearDialog = false }) {
                    Text("Cancel", color = palette.textSecondary)
                }
            },
        )
    }
}
