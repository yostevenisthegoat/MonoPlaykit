package com.example.ui.tools.timers

import android.os.SystemClock
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.DeleteOutline
import androidx.compose.material.icons.outlined.Flag
import androidx.compose.material.icons.outlined.Pause
import androidx.compose.material.icons.outlined.PlayArrow
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material.icons.outlined.Remove
import androidx.compose.material.icons.outlined.Save
import androidx.compose.material.icons.outlined.SkipNext
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.UtilityIds
import com.example.ui.MainViewModel
import com.example.ui.components.KeepScreenOnEffect
import com.example.ui.components.MonoCard
import com.example.ui.components.MonoChip
import com.example.ui.components.MonoPrimaryButton
import com.example.ui.components.MonoSecondaryButton
import com.example.ui.components.MonoToolTopBar
import com.example.ui.theme.MonoTheme
import com.example.ui.theme.TabularNumeralStyle
import com.example.util.TimeFormatter
import kotlinx.coroutines.delay

// -----------------------------------------------------------------------------
// Stopwatch
// -----------------------------------------------------------------------------

private data class LapEntry(val number: Int, val splitMs: Long, val totalMs: Long)

@Composable
fun StopwatchScreen(
    viewModel: MainViewModel,
    isFavorite: Boolean,
    keepScreenAwake: Boolean,
    onBack: () -> Unit,
    onToggleFavorite: () -> Unit,
) {
    val palette = MonoTheme.palette
    val context = LocalContext.current
    KeepScreenOnEffect(keepScreenAwake)

    var running by remember { mutableStateOf(false) }
    var elapsedMs by remember { mutableLongStateOf(0L) }
    var startNanos by remember { mutableLongStateOf(0L) }
    var lastLapTotalMs by remember { mutableLongStateOf(0L) }
    val laps = remember { mutableStateListOf<LapEntry>() }

    LaunchedEffect(running) {
        if (running) {
            startNanos = SystemClock.elapsedRealtimeNanos() - elapsedMs * 1_000_000L
            while (running) {
                elapsedMs = (SystemClock.elapsedRealtimeNanos() - startNanos) / 1_000_000L
                delay(16L)
            }
        }
    }

    val bestSplit = laps.minOfOrNull { it.splitMs }
    val worstSplit = laps.maxOfOrNull { it.splitMs }

    Column(modifier = Modifier.fillMaxSize()) {
        MonoToolTopBar(
            title = "Stopwatch",
            subtitle = "${laps.size} laps recorded",
            onBack = onBack,
            isFavorite = isFavorite,
            onToggleFavorite = onToggleFavorite,
            actions = {
                IconButton(
                    enabled = elapsedMs > 0L,
                    onClick = {
                        viewModel.logHistory(
                            toolId = UtilityIds.STOPWATCH,
                            title = "Stopwatch Run",
                            summary = "${laps.size} laps",
                            detailText = laps.joinToString("\n") { "Lap ${it.number}: ${TimeFormatter.formatStopwatch(it.splitMs)} (Total ${TimeFormatter.formatStopwatch(it.totalMs)})" },
                            primaryValue = TimeFormatter.formatStopwatch(elapsedMs),
                            numericValueMs = elapsedMs,
                        )
                        Toast.makeText(context, "Stopwatch saved to History", Toast.LENGTH_SHORT).show()
                    },
                ) {
                    Icon(Icons.Outlined.Save, contentDescription = "Save run", tint = if (elapsedMs > 0L) palette.textPrimary else palette.textMuted)
                }
            },
        )

        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            MonoCard(elevated = true, cornerRadius = 24.dp, contentPadding = PaddingValues(28.dp)) {
                Column(modifier = Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = TimeFormatter.formatStopwatch(elapsedMs),
                        style = TabularNumeralStyle.copy(fontSize = 54.sp, fontWeight = FontWeight.Bold),
                        color = palette.textPrimary,
                        modifier = Modifier.testTag("stopwatch_display"),
                    )
                    Spacer(Modifier.height(20.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        MonoPrimaryButton(
                            text = if (running) "Pause" else "Start",
                            icon = if (running) Icons.Outlined.Pause else Icons.Outlined.PlayArrow,
                            onClick = {
                                running = !running
                                viewModel.feedbackAccent()
                            },
                            modifier = Modifier.weight(1f).testTag("stopwatch_start_pause"),
                        )
                        if (running) {
                            MonoSecondaryButton(
                                text = "Lap",
                                icon = Icons.Outlined.Flag,
                                onClick = {
                                    val nowTotal = (SystemClock.elapsedRealtimeNanos() - startNanos) / 1_000_000L
                                    val split = nowTotal - lastLapTotalMs
                                    lastLapTotalMs = nowTotal
                                    laps.add(0, LapEntry(laps.size + 1, split, nowTotal))
                                    viewModel.feedbackClick()
                                },
                                modifier = Modifier.weight(1f),
                            )
                        } else {
                            MonoSecondaryButton(
                                text = "Reset",
                                icon = Icons.Outlined.Refresh,
                                enabled = elapsedMs > 0L,
                                onClick = {
                                    elapsedMs = 0L
                                    lastLapTotalMs = 0L
                                    laps.clear()
                                    viewModel.feedbackClick()
                                },
                                modifier = Modifier.weight(1f),
                            )
                        }
                    }
                }
            }

            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                items(laps, key = { it.number }) { lap ->
                    MonoCard(contentPadding = PaddingValues(horizontal = 14.dp, vertical = 10.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text("Lap ${lap.number}", style = MaterialTheme.typography.titleSmall, color = palette.textPrimary)
                                if (laps.size > 1 && lap.splitMs == bestSplit) {
                                    Spacer(Modifier.width(8.dp))
                                    Text("FASTEST", style = MaterialTheme.typography.labelSmall, color = palette.success, fontWeight = FontWeight.Bold)
                                } else if (laps.size > 1 && lap.splitMs == worstSplit) {
                                    Spacer(Modifier.width(8.dp))
                                    Text("SLOWEST", style = MaterialTheme.typography.labelSmall, color = palette.teamRed, fontWeight = FontWeight.Bold)
                                }
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                Text(TimeFormatter.formatStopwatch(lap.splitMs), style = TabularNumeralStyle.copy(fontWeight = FontWeight.Bold), color = palette.textPrimary)
                                Text(TimeFormatter.formatStopwatch(lap.totalMs), style = TabularNumeralStyle.copy(fontSize = 11.sp), color = palette.textMuted)
                            }
                        }
                    }
                }
            }
        }
    }
}

// -----------------------------------------------------------------------------
// Countdown Timer
// -----------------------------------------------------------------------------

@Composable
fun CountdownTimerScreen(
    viewModel: MainViewModel,
    isFavorite: Boolean,
    keepScreenAwake: Boolean,
    onBack: () -> Unit,
    onToggleFavorite: () -> Unit,
) {
    val palette = MonoTheme.palette
    KeepScreenOnEffect(keepScreenAwake)

    var totalMs by remember { mutableLongStateOf(5 * 60_000L) }
    var remainingMs by remember { mutableLongStateOf(5 * 60_000L) }
    var running by remember { mutableStateOf(false) }
    var endRealtimeMs by remember { mutableLongStateOf(0L) }
    var showCustomDialog by remember { mutableStateOf(false) }

    LaunchedEffect(running) {
        if (running) {
            endRealtimeMs = SystemClock.elapsedRealtime() + remainingMs
            while (running) {
                val left = (endRealtimeMs - SystemClock.elapsedRealtime()).coerceAtLeast(0L)
                remainingMs = left
                if (left == 0L) {
                    running = false
                    viewModel.feedbackAlert()
                    viewModel.logHistory(
                        toolId = UtilityIds.COUNTDOWN_TIMER,
                        title = "Countdown Completed",
                        summary = "Duration: ${TimeFormatter.formatMinSec(totalMs)}",
                        primaryValue = TimeFormatter.formatMinSec(totalMs),
                        numericValueMs = totalMs,
                    )
                }
                delay(50L)
            }
        }
    }

    val progress = if (totalMs > 0L) remainingMs.toFloat() / totalMs.toFloat() else 0f

    Column(modifier = Modifier.fillMaxSize()) {
        MonoToolTopBar(
            title = "Countdown Timer",
            subtitle = "Preset & custom round countdown",
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
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(listOf(1, 3, 5, 10, 15, 30)) { mins ->
                    MonoChip("${mins}m", totalMs == mins * 60_000L && !running, {
                        running = false
                        totalMs = mins * 60_000L
                        remainingMs = totalMs
                    })
                }
                item {
                    MonoChip("Custom", false, { showCustomDialog = true })
                }
            }

            Spacer(Modifier.weight(1f))
            Box(contentAlignment = Alignment.Center, modifier = Modifier.size(250.dp)) {
                CircularProgressIndicator(
                    progress = { progress },
                    modifier = Modifier.fillMaxSize(),
                    color = if (remainingMs == 0L) palette.success else palette.accent,
                    trackColor = palette.surfaceSecondary,
                    strokeWidth = 10.dp,
                )
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = TimeFormatter.formatMinSec(remainingMs),
                        style = TabularNumeralStyle.copy(fontSize = 48.sp, fontWeight = FontWeight.Bold),
                        color = palette.textPrimary,
                    )
                    Text(
                        text = if (remainingMs == 0L) "FINISHED" else if (running) "RUNNING" else "READY",
                        style = MaterialTheme.typography.labelMedium,
                        color = palette.textSecondary,
                    )
                }
            }
            Spacer(Modifier.weight(1f))

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                MonoPrimaryButton(
                    text = if (running) "Pause" else "Start",
                    icon = if (running) Icons.Outlined.Pause else Icons.Outlined.PlayArrow,
                    enabled = remainingMs > 0L,
                    onClick = { running = !running; viewModel.feedbackAccent() },
                    modifier = Modifier.weight(1f),
                )
                MonoSecondaryButton(
                    text = "Reset",
                    icon = Icons.Outlined.Refresh,
                    onClick = { running = false; remainingMs = totalMs; viewModel.feedbackClick() },
                    modifier = Modifier.weight(1f),
                )
            }
        }
    }

    if (showCustomDialog) {
        var minsText by remember { mutableStateOf((totalMs / 60_000L).toString()) }
        var secsText by remember { mutableStateOf(((totalMs % 60_000L) / 1000L).toString()) }
        AlertDialog(
            onDismissRequest = { showCustomDialog = false },
            containerColor = palette.cardElevated,
            titleContentColor = palette.textPrimary,
            title = { Text("Custom Countdown") },
            text = {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = minsText,
                        onValueChange = { minsText = it.filter { c -> c.isDigit() }.take(3) },
                        label = { Text("Minutes") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier.weight(1f),
                    )
                    OutlinedTextField(
                        value = secsText,
                        onValueChange = { secsText = it.filter { c -> c.isDigit() }.take(2) },
                        label = { Text("Seconds") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier.weight(1f),
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    val m = minsText.toLongOrNull() ?: 0L
                    val s = (secsText.toLongOrNull() ?: 0L).coerceIn(0L, 59L)
                    val ms = (m * 60_000L + s * 1000L).coerceAtLeast(1000L)
                    running = false
                    totalMs = ms
                    remainingMs = ms
                    showCustomDialog = false
                }) { Text("Set", color = palette.accent) }
            },
        )
    }
}

// -----------------------------------------------------------------------------
// Interval Timer (Saveable Presets in Room)
// -----------------------------------------------------------------------------

@Composable
fun IntervalTimerScreen(
    viewModel: MainViewModel,
    isFavorite: Boolean,
    keepScreenAwake: Boolean,
    onBack: () -> Unit,
    onToggleFavorite: () -> Unit,
) {
    val palette = MonoTheme.palette
    val context = LocalContext.current
    KeepScreenOnEffect(keepScreenAwake)

    val savedConfigs by viewModel.observeProjects("INTERVAL_PRESET").collectAsStateWithLifecycle(initialValue = emptyList())

    var workSec by remember { mutableIntStateOf(40) }
    var restSec by remember { mutableIntStateOf(20) }
    var totalRounds by remember { mutableIntStateOf(6) }

    var running by remember { mutableStateOf(false) }
    var isWorkPhase by remember { mutableStateOf(true) }
    var currentRound by remember { mutableIntStateOf(1) }
    var remainingMs by remember { mutableLongStateOf(40_000L) }
    var showSaveDialog by remember { mutableStateOf(false) }

    LaunchedEffect(running) {
        if (running) {
            var lastTick = SystemClock.elapsedRealtime()
            while (running) {
                delay(50L)
                val now = SystemClock.elapsedRealtime()
                val delta = now - lastTick
                lastTick = now
                remainingMs = (remainingMs - delta).coerceAtLeast(0L)
                if (remainingMs == 0L) {
                    if (isWorkPhase && restSec > 0) {
                        isWorkPhase = false
                        remainingMs = restSec * 1000L
                        viewModel.feedbackBeep()
                    } else if (currentRound < totalRounds) {
                        currentRound++
                        isWorkPhase = true
                        remainingMs = workSec * 1000L
                        viewModel.feedbackAccent()
                    } else {
                        running = false
                        viewModel.feedbackAlert()
                        viewModel.logHistory(
                            toolId = UtilityIds.INTERVAL_TIMER,
                            title = "Interval Workout ($totalRounds rounds)",
                            summary = "${workSec}s work / ${restSec}s rest",
                            primaryValue = "$totalRounds rounds",
                        )
                    }
                }
            }
        }
    }

    fun reset() {
        running = false
        isWorkPhase = true
        currentRound = 1
        remainingMs = workSec * 1000L
    }

    Column(modifier = Modifier.fillMaxSize()) {
        MonoToolTopBar(
            title = "Interval Timer",
            subtitle = "Round $currentRound / $totalRounds",
            onBack = onBack,
            isFavorite = isFavorite,
            onToggleFavorite = onToggleFavorite,
            actions = {
                IconButton(onClick = { showSaveDialog = true }) {
                    Icon(Icons.Outlined.Save, contentDescription = "Save preset", tint = palette.textPrimary)
                }
            },
        )
        LazyColumn(
            modifier = Modifier.weight(1f).navigationBarsPadding(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            item {
                MonoCard(
                    elevated = true,
                    borderColor = if (!running) palette.border else if (isWorkPhase) palette.success else palette.warning,
                    contentPadding = PaddingValues(24.dp),
                ) {
                    Column(modifier = Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = if (isWorkPhase) "WORK • ROUND $currentRound/$totalRounds" else "REST • ROUND $currentRound/$totalRounds",
                            style = MaterialTheme.typography.labelLarge,
                            color = if (isWorkPhase) palette.success else palette.warning,
                            fontWeight = FontWeight.Bold,
                        )
                        Spacer(Modifier.height(8.dp))
                        Text(
                            text = TimeFormatter.formatMinSec(remainingMs),
                            style = TabularNumeralStyle.copy(fontSize = 60.sp, fontWeight = FontWeight.Bold),
                            color = palette.textPrimary,
                        )
                        Spacer(Modifier.height(14.dp))
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            MonoPrimaryButton(
                                text = if (running) "Pause" else "Start",
                                icon = if (running) Icons.Outlined.Pause else Icons.Outlined.PlayArrow,
                                onClick = { running = !running; viewModel.feedbackAccent() },
                                modifier = Modifier.weight(1f),
                            )
                            MonoSecondaryButton("Reset", { reset() }, icon = Icons.Outlined.Refresh, modifier = Modifier.weight(1f))
                        }
                    }
                }
            }

            item {
                MonoCard(contentPadding = PaddingValues(14.dp)) {
                    StepperRow("Work (sec)", workSec, 5, 600, 5) {
                        workSec = it; if (!running) reset()
                    }
                    StepperRow("Rest (sec)", restSec, 0, 300, 5) {
                        restSec = it; if (!running) reset()
                    }
                    StepperRow("Rounds", totalRounds, 1, 50, 1) {
                        totalRounds = it; if (!running) reset()
                    }
                }
            }

            if (savedConfigs.isNotEmpty()) {
                item {
                    Text("Saved Presets", style = MaterialTheme.typography.titleSmall, color = palette.textPrimary)
                }
                items(savedConfigs, key = { it.id }) { cfg ->
                    MonoCard(
                        onClick = {
                            val parts = cfg.subtitle.split(",").mapNotNull { it.toIntOrNull() }
                            if (parts.size == 3) {
                                workSec = parts[0]; restSec = parts[1]; totalRounds = parts[2]
                                reset()
                            }
                        },
                        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 10.dp),
                    ) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                            Column {
                                Text(cfg.name, style = MaterialTheme.typography.titleSmall, color = palette.textPrimary)
                                Text(cfg.notes, style = MaterialTheme.typography.bodyMedium, color = palette.textSecondary)
                            }
                            IconButton(onClick = { viewModel.deleteProject(cfg.id) }) {
                                Icon(Icons.Outlined.DeleteOutline, contentDescription = "Delete preset", tint = palette.textMuted)
                            }
                        }
                    }
                }
            }
        }
    }

    if (showSaveDialog) {
        var name by remember { mutableStateOf("") }
        AlertDialog(
            onDismissRequest = { showSaveDialog = false },
            containerColor = palette.cardElevated,
            titleContentColor = palette.textPrimary,
            title = { Text("Save Interval Preset") },
            text = {
                OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Preset Name") }, singleLine = true)
            },
            confirmButton = {
                TextButton(onClick = {
                    val label = name.trim().ifBlank { "${workSec}s/${restSec}s x$totalRounds" }
                    viewModel.createProject("INTERVAL_PRESET", label, "$workSec,$restSec,$totalRounds", "${workSec}s work • ${restSec}s rest • $totalRounds rounds")
                    Toast.makeText(context, "Preset saved", Toast.LENGTH_SHORT).show()
                    showSaveDialog = false
                }) { Text("Save", color = palette.accent) }
            },
        )
    }
}

@Composable
private fun StepperRow(label: String, value: Int, min: Int, max: Int, step: Int, onChange: (Int) -> Unit) {
    val palette = MonoTheme.palette
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(label, style = MaterialTheme.typography.titleSmall, color = palette.textPrimary)
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = { onChange((value - step).coerceAtLeast(min)) }) {
                Icon(Icons.Outlined.Remove, contentDescription = "Decrease $label", tint = palette.textSecondary)
            }
            Text("$value", style = TabularNumeralStyle.copy(fontSize = 18.sp, fontWeight = FontWeight.Bold), color = palette.textPrimary)
            IconButton(onClick = { onChange((value + step).coerceAtMost(max)) }) {
                Icon(Icons.Outlined.Add, contentDescription = "Increase $label", tint = palette.textPrimary)
            }
        }
    }
}

// -----------------------------------------------------------------------------
// Multi-Counter & Player Turn Tracker
// -----------------------------------------------------------------------------

private data class CounterRow(val id: Int, val name: String, val value: Int, val step: Int = 1)

@Composable
fun MultiCounterScreen(
    viewModel: MainViewModel,
    isFavorite: Boolean,
    onBack: () -> Unit,
    onToggleFavorite: () -> Unit,
) {
    val palette = MonoTheme.palette
    val context = LocalContext.current

    val counters = remember {
        mutableStateListOf(
            CounterRow(1, "Player 1", 0, 1),
            CounterRow(2, "Player 2", 0, 1),
        )
    }
    var turnTrackerEnabled by remember { mutableStateOf(true) }
    var activeTurnIdx by remember { mutableIntStateOf(0) }
    var roundNum by remember { mutableIntStateOf(1) }
    var showAddDialog by remember { mutableStateOf(false) }

    Column(modifier = Modifier.fillMaxSize()) {
        MonoToolTopBar(
            title = "Multi-Counter & Turns",
            subtitle = if (turnTrackerEnabled) "Round $roundNum • ${counters.getOrNull(activeTurnIdx)?.name.orEmpty()}" else "${counters.size} named counters",
            onBack = onBack,
            isFavorite = isFavorite,
            onToggleFavorite = onToggleFavorite,
            actions = {
                IconButton(onClick = { showAddDialog = true }) {
                    Icon(Icons.Outlined.Add, contentDescription = "Add counter", tint = palette.textPrimary)
                }
                IconButton(onClick = {
                    viewModel.logHistory(
                        toolId = UtilityIds.MULTI_COUNTER,
                        title = "Counter Session (Round $roundNum)",
                        summary = counters.joinToString(" | ") { "${it.name}: ${it.value}" },
                        primaryValue = "${counters.maxOfOrNull { it.value } ?: 0}",
                    )
                    Toast.makeText(context, "Saved to History", Toast.LENGTH_SHORT).show()
                }) {
                    Icon(Icons.Outlined.Save, contentDescription = "Save", tint = palette.textPrimary)
                }
            },
        )
        LazyColumn(
            modifier = Modifier.weight(1f).navigationBarsPadding(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            item {
                MonoCard(contentPadding = PaddingValues(horizontal = 14.dp, vertical = 10.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Column {
                            Text("Turn Tracker Mode", style = MaterialTheme.typography.titleSmall, color = palette.textPrimary)
                            Text("Round $roundNum", style = MaterialTheme.typography.labelMedium, color = palette.textSecondary)
                        }
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Switch(checked = turnTrackerEnabled, onCheckedChange = { turnTrackerEnabled = it })
                            if (turnTrackerEnabled && counters.isNotEmpty()) {
                                MonoPrimaryButton(
                                    text = "Next Turn",
                                    icon = Icons.Outlined.SkipNext,
                                    onClick = {
                                        val next = activeTurnIdx + 1
                                        if (next >= counters.size) {
                                            activeTurnIdx = 0
                                            roundNum++
                                        } else {
                                            activeTurnIdx = next
                                        }
                                        viewModel.feedbackAccent()
                                    },
                                )
                            }
                        }
                    }
                }
            }

            items(counters.indices.toList()) { idx ->
                val c = counters[idx]
                val isTurn = turnTrackerEnabled && idx == activeTurnIdx
                MonoCard(
                    elevated = isTurn,
                    borderColor = if (isTurn) palette.accent else palette.border,
                    contentPadding = PaddingValues(14.dp),
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(c.name, style = MaterialTheme.typography.titleMedium, color = palette.textPrimary)
                            Text("Step ±${c.step}", style = MaterialTheme.typography.labelSmall, color = palette.textMuted)
                        }
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            Surface(
                                onClick = { counters[idx] = c.copy(value = c.value - c.step); viewModel.feedbackClick() },
                                shape = RoundedCornerShape(10.dp),
                                color = palette.surfaceSecondary,
                                border = BorderStroke(1.dp, palette.border),
                                modifier = Modifier.minimumInteractiveComponentSize(),
                            ) {
                                Box(Modifier.padding(10.dp), contentAlignment = Alignment.Center) {
                                    Icon(Icons.Outlined.Remove, contentDescription = "Subtract", tint = palette.textPrimary, modifier = Modifier.size(18.dp))
                                }
                            }
                            Text(
                                text = "${c.value}",
                                style = TabularNumeralStyle.copy(fontSize = 26.sp, fontWeight = FontWeight.Bold),
                                color = palette.textPrimary,
                                modifier = Modifier.padding(horizontal = 8.dp),
                            )
                            Surface(
                                onClick = { counters[idx] = c.copy(value = c.value + c.step); viewModel.feedbackClick() },
                                shape = RoundedCornerShape(10.dp),
                                color = palette.accent,
                                modifier = Modifier.minimumInteractiveComponentSize(),
                            ) {
                                Box(Modifier.padding(10.dp), contentAlignment = Alignment.Center) {
                                    Icon(Icons.Outlined.Add, contentDescription = "Add", tint = palette.onAccent, modifier = Modifier.size(18.dp))
                                }
                            }
                            if (counters.size > 1) {
                                IconButton(onClick = {
                                    counters.removeAt(idx)
                                    if (activeTurnIdx >= counters.size) activeTurnIdx = 0
                                }) {
                                    Icon(Icons.Outlined.DeleteOutline, contentDescription = "Remove", tint = palette.textMuted, modifier = Modifier.size(18.dp))
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (showAddDialog) {
        var name by remember { mutableStateOf("") }
        var stepStr by remember { mutableStateOf("1") }
        AlertDialog(
            onDismissRequest = { showAddDialog = false },
            containerColor = palette.cardElevated,
            titleContentColor = palette.textPrimary,
            title = { Text("Add Named Counter") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Counter / Player Name") }, singleLine = true)
                    OutlinedTextField(
                        value = stepStr,
                        onValueChange = { stepStr = it.filter { c -> c.isDigit() }.take(3) },
                        label = { Text("Step Size (1, 5, 10...)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    val label = name.trim().ifBlank { "Counter ${counters.size + 1}" }
                    val step = (stepStr.toIntOrNull() ?: 1).coerceAtLeast(1)
                    val nextId = (counters.maxOfOrNull { it.id } ?: 0) + 1
                    counters.add(CounterRow(nextId, label, 0, step))
                    showAddDialog = false
                }) { Text("Add", color = palette.accent) }
            },
        )
    }
}
