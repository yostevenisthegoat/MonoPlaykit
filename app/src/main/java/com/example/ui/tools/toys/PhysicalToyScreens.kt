package com.example.ui.tools.toys

import android.os.SystemClock
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.DeleteOutline
import androidx.compose.material.icons.outlined.Pause
import androidx.compose.material.icons.outlined.PlayArrow
import androidx.compose.material.icons.outlined.RadioButtonUnchecked
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material.icons.outlined.Remove
import androidx.compose.material.icons.outlined.Save
import androidx.compose.material.icons.outlined.TouchApp
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.local.ChecklistProjectEntity
import com.example.data.model.UtilityIds
import com.example.ui.MainViewModel
import com.example.ui.components.KeepScreenOnEffect
import com.example.ui.components.MonoCard
import com.example.ui.components.MonoChip
import com.example.ui.components.MonoEmptyState
import com.example.ui.components.MonoPrimaryButton
import com.example.ui.components.MonoSecondaryButton
import com.example.ui.components.MonoToolTopBar
import com.example.ui.theme.MonoTheme
import com.example.ui.theme.TabularNumeralStyle
import com.example.util.TimeFormatter
import kotlinx.coroutines.delay
import kotlin.math.roundToInt

// -----------------------------------------------------------------------------
// Fidget Spinner Tracker & Tap RPM Estimator
// -----------------------------------------------------------------------------

@Composable
fun FidgetSpinnerScreen(
    viewModel: MainViewModel,
    isFavorite: Boolean,
    onBack: () -> Unit,
    onToggleFavorite: () -> Unit,
) {
    val palette = MonoTheme.palette
    val history by viewModel.observeHistoryForTool(UtilityIds.FIDGET_SPINNER)
        .collectAsStateWithLifecycle(initialValue = emptyList())

    var running by remember { mutableStateOf(false) }
    var elapsedMs by remember { mutableLongStateOf(0L) }
    var startNanos by remember { mutableLongStateOf(0L) }

    // Optical/Tap RPM estimator
    val revTimestamps = remember { mutableStateListOf<Long>() }
    var revsPerTap by remember { mutableIntStateOf(1) }
    var measuredRpm by remember { mutableIntStateOf(0) }

    LaunchedEffect(running) {
        if (running) {
            startNanos = SystemClock.elapsedRealtimeNanos() - elapsedMs * 1_000_000L
            while (running) {
                elapsedMs = (SystemClock.elapsedRealtimeNanos() - startNanos) / 1_000_000L
                delay(24L)
            }
        }
    }

    val bestMs = remember(history) { history.maxOfOrNull { it.numericValueMs } ?: 0L }

    Column(modifier = Modifier.fillMaxSize()) {
        MonoToolTopBar(
            title = "Fidget Spinner Tracker",
            subtitle = "Spin duration & tap RPM estimator",
            onBack = onBack,
            isFavorite = isFavorite,
            onToggleFavorite = onToggleFavorite,
        )
        LazyColumn(
            modifier = Modifier.weight(1f).navigationBarsPadding(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            item {
                MonoCard(elevated = true, contentPadding = PaddingValues(22.dp)) {
                    Column(modifier = Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("SPIN DURATION", style = MaterialTheme.typography.labelMedium, color = palette.textMuted)
                        Spacer(Modifier.height(6.dp))
                        Text(
                            text = TimeFormatter.formatStopwatch(elapsedMs),
                            style = TabularNumeralStyle.copy(fontSize = 52.sp, fontWeight = FontWeight.Bold),
                            color = palette.textPrimary,
                        )
                        Spacer(Modifier.height(6.dp))
                        Text(
                            text = if (bestMs > 0L) "Personal Best: ${TimeFormatter.formatStopwatch(bestMs)}" else "No saved spins yet",
                            style = MaterialTheme.typography.labelMedium,
                            color = palette.accent,
                        )
                        Spacer(Modifier.height(16.dp))
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            MonoPrimaryButton(
                                text = if (running) "Stop Spin" else "Start Spin",
                                icon = if (running) Icons.Outlined.Pause else Icons.Outlined.PlayArrow,
                                onClick = {
                                    if (running) {
                                        running = false
                                        val finalMs = elapsedMs
                                        viewModel.feedbackAccent()
                                        if (finalMs > 300L) {
                                            viewModel.logHistory(
                                                toolId = UtilityIds.FIDGET_SPINNER,
                                                title = "Spinner Run",
                                                summary = if (measuredRpm > 0) "Measured peak ~$measuredRpm RPM" else "Free spin duration",
                                                primaryValue = TimeFormatter.formatStopwatch(finalMs),
                                                numericValueMs = finalMs,
                                            )
                                        }
                                    } else {
                                        elapsedMs = 0L
                                        running = true
                                        viewModel.feedbackAccent()
                                    }
                                },
                                modifier = Modifier.weight(1f),
                            )
                            MonoSecondaryButton(
                                text = "Reset",
                                onClick = { running = false; elapsedMs = 0L },
                            )
                        }
                    }
                }
            }

            // Tap RPM Estimator
            item {
                MonoCard(contentPadding = PaddingValues(16.dp)) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Column {
                            Text("Tap RPM Estimator", style = MaterialTheme.typography.titleMedium, color = palette.textPrimary)
                            Text("Tap each time marked spoke passes", style = MaterialTheme.typography.bodyMedium, color = palette.textSecondary)
                        }
                        Text("$measuredRpm RPM", style = TabularNumeralStyle.copy(fontSize = 20.sp, fontWeight = FontWeight.Bold), color = palette.accent)
                    }
                    Spacer(Modifier.height(10.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        MonoChip("1 rev / tap", revsPerTap == 1, { revsPerTap = 1; revTimestamps.clear(); measuredRpm = 0 })
                        MonoChip("5 revs / tap", revsPerTap == 5, { revsPerTap = 5; revTimestamps.clear(); measuredRpm = 0 })
                    }
                    Spacer(Modifier.height(10.dp))
                    MonoSecondaryButton(
                        text = "Tap Spoke Mark",
                        icon = Icons.Outlined.TouchApp,
                        onClick = {
                            val now = SystemClock.elapsedRealtime()
                            if (revTimestamps.isNotEmpty() && now - revTimestamps.last() > 4000L) {
                                revTimestamps.clear()
                            }
                            revTimestamps.add(now)
                            if (revTimestamps.size > 6) revTimestamps.removeAt(0)
                            if (revTimestamps.size >= 2) {
                                val spanMs = revTimestamps.last() - revTimestamps.first()
                                val intervals = revTimestamps.size - 1
                                if (spanMs > 0) {
                                    val rpm = (intervals * revsPerTap * 60_000.0) / spanMs
                                    measuredRpm = rpm.roundToInt()
                                }
                            }
                            viewModel.feedbackClick()
                        },
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
            }

            items(history.take(10), key = { it.id }) { rec ->
                MonoCard(contentPadding = PaddingValues(horizontal = 14.dp, vertical = 10.dp)) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text(rec.primaryValue, style = TabularNumeralStyle.copy(fontWeight = FontWeight.Bold), color = palette.textPrimary)
                        Text(rec.summary, style = MaterialTheme.typography.bodyMedium, color = palette.textSecondary)
                    }
                }
            }
        }
    }
}

// -----------------------------------------------------------------------------
// Yo-Yo & Kendama Trackers
// -----------------------------------------------------------------------------

private val yoyoTricks = listOf(
    "Sleeper" to "Starter",
    "Forward Pass" to "Starter",
    "Trapeze" to "Intermediate",
    "Brain Twister" to "Intermediate",
    "Split the Atom" to "Intermediate",
    "Double or Nothing" to "Advanced",
    "Matrix" to "Advanced",
    "Boingy-Boingy" to "Advanced",
    "Kwijibo" to "Expert",
    "DNA Bind" to "Expert",
)

private val kendamaTricks = listOf(
    "Big Cup" to "Starter",
    "Small Cup" to "Starter",
    "Base Cup" to "Starter",
    "Spike" to "Starter",
    "Moshikame" to "Intermediate",
    "Around Japan" to "Intermediate",
    "Airplane" to "Intermediate",
    "Lighthouse" to "Advanced",
    "Lunar" to "Advanced",
    "Whirlwind" to "Expert",
)

@Composable
fun TrickPracticeTrackerScreen(
    toolId: String,
    title: String,
    tricks: List<Pair<String, String>>,
    viewModel: MainViewModel,
    isFavorite: Boolean,
    onBack: () -> Unit,
    onToggleFavorite: () -> Unit,
) {
    val palette = MonoTheme.palette
    val context = LocalContext.current
    val history by viewModel.observeHistoryForTool(toolId).collectAsStateWithLifecycle(initialValue = emptyList())

    var landedCount by remember { mutableIntStateOf(0) }
    var streak by remember { mutableIntStateOf(0) }
    var bestStreak by remember { mutableIntStateOf(0) }
    var selectedTrick by remember { mutableStateOf(tricks.first().first) }
    var timerRunning by remember { mutableStateOf(false) }
    var sessionMs by remember { mutableLongStateOf(0L) }
    var startMs by remember { mutableLongStateOf(0L) }

    LaunchedEffect(timerRunning) {
        if (timerRunning) {
            startMs = SystemClock.elapsedRealtime() - sessionMs
            while (timerRunning) {
                sessionMs = SystemClock.elapsedRealtime() - startMs
                delay(100L)
            }
        }
    }

    val bestSavedStreak = remember(history) {
        history.maxOfOrNull { it.numericValueMs.toInt() } ?: 0
    }

    Column(modifier = Modifier.fillMaxSize()) {
        MonoToolTopBar(
            title = title,
            subtitle = "Active: $selectedTrick",
            onBack = onBack,
            isFavorite = isFavorite,
            onToggleFavorite = onToggleFavorite,
            actions = {
                IconButton(onClick = {
                    viewModel.logHistory(
                        toolId = toolId,
                        title = "$title — $selectedTrick",
                        summary = "Landed $landedCount • Best streak $bestStreak • ${TimeFormatter.formatMinSec(sessionMs)}",
                        primaryValue = "${bestStreak}x streak",
                        numericValueMs = bestStreak.toLong(),
                    )
                    Toast.makeText(context, "Practice session saved", Toast.LENGTH_SHORT).show()
                }) {
                    Icon(Icons.Outlined.Save, contentDescription = "Save session", tint = palette.textPrimary)
                }
            },
        )
        LazyColumn(
            modifier = Modifier.weight(1f).navigationBarsPadding(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            item {
                MonoCard(elevated = true, contentPadding = PaddingValues(20.dp)) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Column {
                            Text("LANDED", style = MaterialTheme.typography.labelSmall, color = palette.textMuted)
                            Text("$landedCount", style = TabularNumeralStyle.copy(fontSize = 42.sp, fontWeight = FontWeight.Bold), color = palette.textPrimary)
                        }
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("STREAK", style = MaterialTheme.typography.labelSmall, color = palette.textMuted)
                            Text("${streak}x", style = TabularNumeralStyle.copy(fontSize = 42.sp, fontWeight = FontWeight.Bold), color = palette.accent)
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text("BEST", style = MaterialTheme.typography.labelSmall, color = palette.textMuted)
                            Text("${maxOf(bestStreak, bestSavedStreak)}x", style = TabularNumeralStyle.copy(fontSize = 42.sp, fontWeight = FontWeight.Bold), color = palette.success)
                        }
                    }
                    Spacer(Modifier.height(12.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Text("Practice Timer: ${TimeFormatter.formatMinSec(sessionMs)}", style = TabularNumeralStyle, color = palette.textSecondary)
                        IconButton(onClick = { timerRunning = !timerRunning }) {
                            Icon(if (timerRunning) Icons.Outlined.Pause else Icons.Outlined.PlayArrow, contentDescription = "Toggle timer", tint = palette.textPrimary)
                        }
                    }
                    Spacer(Modifier.height(10.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        MonoPrimaryButton(
                            text = "Landed +1",
                            onClick = {
                                landedCount++
                                streak++
                                if (streak > bestStreak) bestStreak = streak
                                viewModel.feedbackAccent()
                            },
                            modifier = Modifier.weight(2f),
                        )
                        MonoSecondaryButton(
                            text = "Miss",
                            onClick = {
                                streak = 0
                                viewModel.feedbackClick()
                            },
                            modifier = Modifier.weight(1f),
                        )
                    }
                }
            }

            item {
                Text("Trick Reference Guide", style = MaterialTheme.typography.titleMedium, color = palette.textPrimary)
            }
            items(tricks) { (trick, tier) ->
                val selected = trick == selectedTrick
                MonoCard(
                    borderColor = if (selected) palette.accent else palette.border,
                    onClick = { selectedTrick = trick },
                    contentPadding = PaddingValues(horizontal = 14.dp, vertical = 12.dp),
                ) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Text(trick, style = MaterialTheme.typography.titleSmall, color = palette.textPrimary)
                        Text(tier, style = MaterialTheme.typography.labelMedium, color = if (selected) palette.accent else palette.textMuted)
                    }
                }
            }
        }
    }
}

@Composable
fun YoYoTrackerScreen(viewModel: MainViewModel, isFavorite: Boolean, onBack: () -> Unit, onToggleFavorite: () -> Unit) =
    TrickPracticeTrackerScreen(UtilityIds.YOYO_TRACKER, "Yo-Yo Practice Tracker", yoyoTricks, viewModel, isFavorite, onBack, onToggleFavorite)

@Composable
fun KendamaTrackerScreen(viewModel: MainViewModel, isFavorite: Boolean, onBack: () -> Unit, onToggleFavorite: () -> Unit) =
    TrickPracticeTrackerScreen(UtilityIds.KENDAMA_TRACKER, "Kendama Tracker", kendamaTricks, viewModel, isFavorite, onBack, onToggleFavorite)

// -----------------------------------------------------------------------------
// LEGO Brick Inventory & Model Kit Checklist (Room Persisted)
// -----------------------------------------------------------------------------

@Composable
fun ChecklistProjectToolScreen(
    projectType: String, // "LEGO" or "MODEL_KIT"
    title: String,
    subtitle: String,
    itemPlaceholder: String,
    viewModel: MainViewModel,
    isFavorite: Boolean,
    onBack: () -> Unit,
    onToggleFavorite: () -> Unit,
) {
    val palette = MonoTheme.palette
    val projects by viewModel.observeProjects(projectType).collectAsStateWithLifecycle(initialValue = emptyList())
    var selectedProject by remember { mutableStateOf<ChecklistProjectEntity?>(null) }

    // Keep selectedProject synchronized if updated or deleted
    val activeProject = projects.find { it.id == selectedProject?.id } ?: projects.firstOrNull()

    val items by (if (activeProject != null) viewModel.observeProjectItems(activeProject.id) else kotlinx.coroutines.flow.flowOf(emptyList()))
        .collectAsStateWithLifecycle(initialValue = emptyList())

    var showNewProjectDialog by remember { mutableStateOf(false) }
    var showAddItemDialog by remember { mutableStateOf(false) }
    var showNotesDialog by remember { mutableStateOf(false) }

    Column(modifier = Modifier.fillMaxSize()) {
        MonoToolTopBar(
            title = title,
            subtitle = activeProject?.name ?: subtitle,
            onBack = onBack,
            isFavorite = isFavorite,
            onToggleFavorite = onToggleFavorite,
            actions = {
                IconButton(onClick = { showNewProjectDialog = true }) {
                    Icon(Icons.Outlined.Add, contentDescription = "New project", tint = palette.textPrimary)
                }
            },
        )

        LazyColumn(
            modifier = Modifier.weight(1f).navigationBarsPadding(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            if (projects.isEmpty()) {
                item {
                    MonoEmptyState(
                        icon = Icons.Outlined.CheckCircle,
                        title = "No projects yet",
                        description = if (projectType == "LEGO") {
                            "Create a LEGO build project to track needed bricks, parts, and building progress."
                        } else {
                            "Create a Model Kit project to track runners, assembly steps, and paint notes."
                        },
                        actionText = "Create Project",
                        onAction = { showNewProjectDialog = true },
                    )
                }
            } else {
                item {
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(projects, key = { it.id }) { proj ->
                            MonoChip(proj.name, activeProject?.id == proj.id, { selectedProject = proj })
                        }
                    }
                }

                activeProject?.let { proj ->
                    val completedCount = items.count { it.isChecked || (it.targetCount > 1 && it.currentCount >= it.targetCount) }
                    val progress = if (items.isEmpty()) 0f else completedCount.toFloat() / items.size.toFloat()

                    item {
                        MonoCard(elevated = true, contentPadding = PaddingValues(16.dp)) {
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(proj.name, style = MaterialTheme.typography.titleLarge, color = palette.textPrimary)
                                    if (proj.notes.isNotBlank()) {
                                        Text(proj.notes, style = MaterialTheme.typography.bodyMedium, color = palette.textSecondary)
                                    }
                                }
                                IconButton(onClick = {
                                    viewModel.deleteProject(proj.id)
                                    selectedProject = null
                                }) {
                                    Icon(Icons.Outlined.DeleteOutline, contentDescription = "Delete project", tint = palette.textMuted)
                                }
                            }
                            Spacer(Modifier.height(10.dp))
                            LinearProgressIndicator(
                                progress = { progress },
                                modifier = Modifier.fillMaxWidth().height(8.dp).clip(RoundedCornerShape(50)),
                                color = palette.accent,
                                trackColor = palette.surfaceSecondary,
                            )
                            Spacer(Modifier.height(6.dp))
                            Text(
                                text = "$completedCount / ${items.size} completed (${(progress * 100).roundToInt()}%)",
                                style = MaterialTheme.typography.labelMedium,
                                color = palette.textSecondary,
                            )
                            Spacer(Modifier.height(10.dp))
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                MonoPrimaryButton("Add Item", { showAddItemDialog = true }, icon = Icons.Outlined.Add, modifier = Modifier.weight(1f))
                                MonoSecondaryButton("Notes", { showNotesDialog = true }, modifier = Modifier.weight(1f))
                            }
                        }
                    }

                    items(items, key = { it.id }) { item ->
                        MonoCard(contentPadding = PaddingValues(horizontal = 12.dp, vertical = 10.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Checkbox(
                                    checked = item.isChecked || (item.targetCount > 1 && item.currentCount >= item.targetCount),
                                    onCheckedChange = { checked ->
                                        viewModel.updateProjectItem(
                                            item.copy(
                                                isChecked = checked,
                                                currentCount = if (checked) item.targetCount else 0,
                                            )
                                        )
                                    },
                                )
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(item.name, style = MaterialTheme.typography.titleSmall, color = palette.textPrimary)
                                    if (item.detail.isNotBlank()) {
                                        Text(item.detail, style = MaterialTheme.typography.bodyMedium, color = palette.textSecondary)
                                    }
                                }
                                if (item.targetCount > 1) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        IconButton(onClick = {
                                            val next = (item.currentCount - 1).coerceAtLeast(0)
                                            viewModel.updateProjectItem(item.copy(currentCount = next, isChecked = next >= item.targetCount))
                                        }, modifier = Modifier.size(30.dp)) {
                                            Icon(Icons.Outlined.Remove, contentDescription = "Less", tint = palette.textSecondary, modifier = Modifier.size(16.dp))
                                        }
                                        Text(
                                            text = "${item.currentCount}/${item.targetCount}",
                                            style = TabularNumeralStyle.copy(fontSize = 13.sp, fontWeight = FontWeight.Bold),
                                            color = palette.textPrimary,
                                        )
                                        IconButton(onClick = {
                                            val next = (item.currentCount + 1).coerceAtMost(item.targetCount)
                                            viewModel.updateProjectItem(item.copy(currentCount = next, isChecked = next >= item.targetCount))
                                        }, modifier = Modifier.size(30.dp)) {
                                            Icon(Icons.Outlined.Add, contentDescription = "More", tint = palette.textPrimary, modifier = Modifier.size(16.dp))
                                        }
                                    }
                                }
                                IconButton(onClick = { viewModel.deleteProjectItem(item.id) }, modifier = Modifier.size(32.dp)) {
                                    Icon(Icons.Outlined.DeleteOutline, contentDescription = "Delete item", tint = palette.textMuted, modifier = Modifier.size(18.dp))
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (showNewProjectDialog) {
        var name by remember { mutableStateOf("") }
        var notes by remember { mutableStateOf("") }
        AlertDialog(
            onDismissRequest = { showNewProjectDialog = false },
            containerColor = palette.cardElevated,
            titleContentColor = palette.textPrimary,
            title = { Text("New Project") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Project Name") }, singleLine = true)
                    OutlinedTextField(value = notes, onValueChange = { notes = it }, label = { Text("Notes (Set #, Scale, etc.)") })
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    if (name.isNotBlank()) {
                        viewModel.createProject(projectType, name.trim(), notes = notes.trim())
                    }
                    showNewProjectDialog = false
                }) { Text("Create", color = palette.accent) }
            },
            dismissButton = {
                TextButton(onClick = { showNewProjectDialog = false }) { Text("Cancel", color = palette.textSecondary) }
            },
        )
    }

    if (showAddItemDialog && activeProject != null) {
        var itemName by remember { mutableStateOf("") }
        var detail by remember { mutableStateOf("") }
        var qtyStr by remember { mutableStateOf("1") }
        AlertDialog(
            onDismissRequest = { showAddItemDialog = false },
            containerColor = palette.cardElevated,
            titleContentColor = palette.textPrimary,
            title = { Text("Add Checklist Item") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(value = itemName, onValueChange = { itemName = it }, label = { Text(itemPlaceholder) }, singleLine = true)
                    OutlinedTextField(value = detail, onValueChange = { detail = it }, label = { Text("Color / Step / Detail (Optional)") }, singleLine = true)
                    OutlinedTextField(
                        value = qtyStr,
                        onValueChange = { qtyStr = it.filter { c -> c.isDigit() }.take(4) },
                        label = { Text("Target Quantity") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    if (itemName.isNotBlank()) {
                        val qty = (qtyStr.toIntOrNull() ?: 1).coerceAtLeast(1)
                        viewModel.addProjectItem(activeProject.id, itemName.trim(), detail.trim(), 0, qty)
                    }
                    showAddItemDialog = false
                }) { Text("Add", color = palette.accent) }
            },
            dismissButton = {
                TextButton(onClick = { showAddItemDialog = false }) { Text("Cancel", color = palette.textSecondary) }
            },
        )
    }

    if (showNotesDialog && activeProject != null) {
        var draft by remember(activeProject.id) { mutableStateOf(activeProject.notes) }
        AlertDialog(
            onDismissRequest = { showNotesDialog = false },
            containerColor = palette.cardElevated,
            titleContentColor = palette.textPrimary,
            title = { Text("Project Notes") },
            text = {
                OutlinedTextField(value = draft, onValueChange = { draft = it }, minLines = 3, label = { Text("Build & Paint Notes") })
            },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.updateProject(activeProject.copy(notes = draft))
                    showNotesDialog = false
                }) { Text("Save", color = palette.accent) }
            },
        )
    }
}

@Composable
fun LegoInventoryScreen(viewModel: MainViewModel, isFavorite: Boolean, onBack: () -> Unit, onToggleFavorite: () -> Unit) =
    ChecklistProjectToolScreen("LEGO", "LEGO Brick & Build Tracker", "Inventory & build progress", "Part Name (e.g. 2x4 Plate)", viewModel, isFavorite, onBack, onToggleFavorite)

@Composable
fun ModelKitChecklistScreen(viewModel: MainViewModel, isFavorite: Boolean, onBack: () -> Unit, onToggleFavorite: () -> Unit) =
    ChecklistProjectToolScreen("MODEL_KIT", "Model Kit Checklist", "Assembly steps & notes", "Runner / Step (e.g. Torso Assembly)", viewModel, isFavorite, onBack, onToggleFavorite)

// -----------------------------------------------------------------------------
// Metronome & Instrument Practice Timer
// -----------------------------------------------------------------------------

@Composable
fun MetronomeScreen(
    viewModel: MainViewModel,
    isFavorite: Boolean,
    keepScreenAwake: Boolean,
    onBack: () -> Unit,
    onToggleFavorite: () -> Unit,
) {
    val palette = MonoTheme.palette
    KeepScreenOnEffect(keepScreenAwake)

    var bpm by remember { mutableIntStateOf(120) }
    var beatsPerBar by remember { mutableIntStateOf(4) }
    var playing by remember { mutableStateOf(false) }
    var currentBeat by remember { mutableIntStateOf(0) }
    var practiceMs by remember { mutableLongStateOf(0L) }
    val tapTimes = remember { mutableStateListOf<Long>() }

    LaunchedEffect(playing, bpm, beatsPerBar) {
        if (playing) {
            var nextBeatAt = SystemClock.elapsedRealtime()
            val practiceStart = SystemClock.elapsedRealtime() - practiceMs
            var beat = 0
            while (playing) {
                val intervalMs = (60_000.0 / bpm).roundToInt().toLong()
                currentBeat = (beat % beatsPerBar) + 1
                if (currentBeat == 1) viewModel.feedbackAccent() else viewModel.feedbackClick()
                beat++
                nextBeatAt += intervalMs
                val sleepMs = (nextBeatAt - SystemClock.elapsedRealtime()).coerceAtLeast(10L)
                delay(sleepMs)
                practiceMs = SystemClock.elapsedRealtime() - practiceStart
            }
        } else {
            currentBeat = 0
        }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        MonoToolTopBar(
            title = "Metronome & Practice",
            subtitle = "$bpm BPM • $beatsPerBar/4",
            onBack = onBack,
            isFavorite = isFavorite,
            onToggleFavorite = onToggleFavorite,
        )
        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            MonoCard(elevated = true, contentPadding = PaddingValues(22.dp)) {
                Column(modifier = Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "$bpm",
                        style = TabularNumeralStyle.copy(fontSize = 68.sp, fontWeight = FontWeight.Bold),
                        color = palette.textPrimary,
                    )
                    Text("BPM", style = MaterialTheme.typography.labelLarge, color = palette.textMuted)
                    Spacer(Modifier.height(14.dp))

                    // Beat Visual Indicators
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        for (b in 1..beatsPerBar) {
                            val active = playing && currentBeat == b
                            Box(
                                modifier = Modifier
                                    .size(if (b == 1) 24.dp else 18.dp)
                                    .clip(CircleShape)
                                    .background(
                                        when {
                                            active && b == 1 -> palette.accent
                                            active -> palette.textSecondary
                                            else -> palette.surfaceSecondary
                                        }
                                    )
                                    .border(1.dp, palette.border, CircleShape)
                            )
                        }
                    }

                    Spacer(Modifier.height(14.dp))
                    Slider(
                        value = bpm.toFloat(),
                        onValueChange = { bpm = it.roundToInt().coerceIn(30, 260) },
                        valueRange = 30f..260f,
                        colors = SliderDefaults.colors(
                            thumbColor = palette.accent,
                            activeTrackColor = palette.accent,
                            inactiveTrackColor = palette.surfaceSecondary,
                        ),
                    )
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        MonoSecondaryButton("-1", { if (bpm > 30) bpm-- })
                        MonoSecondaryButton("Tap Tempo", {
                            val now = SystemClock.elapsedRealtime()
                            if (tapTimes.isNotEmpty() && now - tapTimes.last() > 2000L) tapTimes.clear()
                            tapTimes.add(now)
                            if (tapTimes.size > 5) tapTimes.removeAt(0)
                            if (tapTimes.size >= 2) {
                                val avgInterval = (tapTimes.last() - tapTimes.first()).toDouble() / (tapTimes.size - 1)
                                if (avgInterval > 0) bpm = (60_000.0 / avgInterval).roundToInt().coerceIn(30, 260)
                            }
                            viewModel.feedbackClick()
                        })
                        MonoSecondaryButton("+1", { if (bpm < 260) bpm++ })
                    }
                }
            }

            MonoCard(contentPadding = PaddingValues(14.dp)) {
                Text("Time Signature", style = MaterialTheme.typography.titleSmall, color = palette.textPrimary)
                Spacer(Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    listOf(2, 3, 4, 6).forEach { beats ->
                        MonoChip("$beats/4", beatsPerBar == beats, { beatsPerBar = beats }, modifier = Modifier.weight(1f))
                    }
                }
                Spacer(Modifier.height(10.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Text("Practice Time: ${TimeFormatter.formatMinSec(practiceMs)}", style = TabularNumeralStyle, color = palette.textSecondary)
                    TextButton(onClick = { practiceMs = 0L }) { Text("Reset", color = palette.textMuted) }
                }
            }

            Spacer(Modifier.weight(1f))
            MonoPrimaryButton(
                text = if (playing) "Stop Metronome" else "Start Metronome",
                icon = if (playing) Icons.Outlined.Pause else Icons.Outlined.PlayArrow,
                onClick = { playing = !playing },
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}
