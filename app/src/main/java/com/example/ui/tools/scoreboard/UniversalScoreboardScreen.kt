package com.example.ui.tools.scoreboard

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
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.Undo
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.Fullscreen
import androidx.compose.material.icons.outlined.FullscreenExit
import androidx.compose.material.icons.outlined.Notes
import androidx.compose.material.icons.outlined.Pause
import androidx.compose.material.icons.outlined.PlayArrow
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material.icons.outlined.Remove
import androidx.compose.material.icons.outlined.Save
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.UtilityIds
import com.example.ui.MainViewModel
import com.example.ui.components.KeepScreenOnEffect
import com.example.ui.components.MonoCard
import com.example.ui.components.MonoChip
import com.example.ui.components.MonoToolTopBar
import com.example.ui.theme.MonoTeamBlue
import com.example.ui.theme.MonoTeamRed
import com.example.ui.theme.MonoTheme
import com.example.ui.theme.TabularNumeralStyle
import com.example.util.TimeFormatter
import kotlinx.coroutines.delay
import kotlin.math.abs

enum class ScoreboardPreset(
    val key: String,
    val label: String,
    val periodLabel: String,
    val maxPeriods: Int,
    val defaultTimerMs: Long,
    val countDownTimer: Boolean,
    val defaultSteps: List<Int>,
    val startScore: Int = 0,
) {
    CUSTOM("CUSTOM", "Custom", "Period", 4, 0L, false, listOf(1)),
    BASKETBALL("BASKETBALL", "Basketball", "Quarter", 4, 10 * 60_000L, true, listOf(1, 2, 3)),
    VOLLEYBALL("VOLLEYBALL", "Volleyball", "Set", 5, 0L, false, listOf(1)),
    BADMINTON("BADMINTON", "Badminton", "Set", 3, 0L, false, listOf(1)),
    TABLE_TENNIS("TABLE_TENNIS", "Table Tennis", "Set", 5, 0L, false, listOf(1)),
    TENNIS("TENNIS", "Tennis", "Set", 3, 0L, false, listOf(1)),
    FOOTBALL("FOOTBALL", "Football", "Half", 2, 45 * 60_000L, true, listOf(1)),
    BOXING("BOXING", "Boxing", "Round", 12, 3 * 60_000L, true, listOf(1)),
    DARTS_301("DARTS_301", "Darts 301", "Leg", 5, 0L, false, listOf(1), startScore = 301),
    DARTS_501("DARTS_501", "Darts 501", "Leg", 5, 0L, false, listOf(1), startScore = 501),
}

private data class ScoreSnapshot(
    val redScore: Int,
    val blueScore: Int,
    val redSets: Int,
    val blueSets: Int,
    val redGames: Int,
    val blueGames: Int,
    val period: Int,
)

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun UniversalScoreboardScreen(
    viewModel: MainViewModel,
    isFavorite: Boolean,
    defaultPresetKey: String,
    defaultRedName: String,
    defaultBlueName: String,
    keepScreenAwake: Boolean,
    presetHint: String?,
    onBack: () -> Unit,
    onToggleFavorite: () -> Unit,
) {
    val palette = MonoTheme.palette
    val context = LocalContext.current
    KeepScreenOnEffect(keepScreenAwake)

    val initialPreset = remember(presetHint, defaultPresetKey) {
        val hint = presetHint?.lowercase().orEmpty()
        when {
            hint.contains("basket") -> ScoreboardPreset.BASKETBALL
            hint.contains("volley") -> ScoreboardPreset.VOLLEYBALL
            hint.contains("badminton") -> ScoreboardPreset.BADMINTON
            hint.contains("table") || hint.contains("ping") -> ScoreboardPreset.TABLE_TENNIS
            hint.contains("tennis") -> ScoreboardPreset.TENNIS
            hint.contains("foot") || hint.contains("soccer") -> ScoreboardPreset.FOOTBALL
            hint.contains("box") -> ScoreboardPreset.BOXING
            hint.contains("301") -> ScoreboardPreset.DARTS_301
            hint.contains("501") || hint.contains("dart") -> ScoreboardPreset.DARTS_501
            else -> ScoreboardPreset.entries.find { it.key == defaultPresetKey } ?: ScoreboardPreset.CUSTOM
        }
    }

    var preset by remember { mutableStateOf(initialPreset) }
    var redName by remember { mutableStateOf(defaultRedName.ifBlank { "Red Team" }) }
    var blueName by remember { mutableStateOf(defaultBlueName.ifBlank { "Blue Team" }) }
    var redColor by remember { mutableStateOf(MonoTeamRed) }
    var blueColor by remember { mutableStateOf(MonoTeamBlue) }

    var redScore by remember { mutableIntStateOf(initialPreset.startScore) }
    var blueScore by remember { mutableIntStateOf(initialPreset.startScore) }
    var redSets by remember { mutableIntStateOf(0) }
    var blueSets by remember { mutableIntStateOf(0) }
    var redGames by remember { mutableIntStateOf(0) }
    var blueGames by remember { mutableIntStateOf(0) }
    var currentPeriod by remember { mutableIntStateOf(1) }
    var matchNotes by remember { mutableStateOf("") }
    var fullscreen by remember { mutableStateOf(false) }

    // Monotonic timer state
    var timerRunning by remember { mutableStateOf(false) }
    var elapsedMs by remember { mutableLongStateOf(0L) }
    var lastTickStartMs by remember { mutableLongStateOf(0L) }
    var targetTimerMs by remember { mutableLongStateOf(initialPreset.defaultTimerMs) }

    // Darts throw buffer
    var dartsInput by remember { mutableStateOf("") }
    var dartsActiveSide by remember { mutableIntStateOf(0) } // 0 = red, 1 = blue

    // Custom match config
    var customStep by remember { mutableIntStateOf(1) }
    var customPeriodLabel by remember { mutableStateOf("Period") }

    val undoStack = remember { mutableStateListOf<ScoreSnapshot>() }

    var showEditTeamsDialog by remember { mutableStateOf(false) }
    var showNotesDialog by remember { mutableStateOf(false) }
    var manualScoreTarget by remember { mutableStateOf<Int?>(null) } // 0 = red, 1 = blue
    var showCustomConfigDialog by remember { mutableStateOf(false) }

    LaunchedEffect(timerRunning) {
        if (timerRunning) {
            lastTickStartMs = SystemClock.elapsedRealtime()
            while (timerRunning) {
                delay(100L)
                val now = SystemClock.elapsedRealtime()
                val delta = now - lastTickStartMs
                lastTickStartMs = now
                elapsedMs += delta
                if (preset.countDownTimer && targetTimerMs > 0L && elapsedMs >= targetTimerMs) {
                    elapsedMs = targetTimerMs
                    timerRunning = false
                    viewModel.feedbackAlert()
                }
            }
        }
    }

    fun pushUndo() {
        undoStack.add(
            ScoreSnapshot(
                redScore = redScore,
                blueScore = blueScore,
                redSets = redSets,
                blueSets = blueSets,
                redGames = redGames,
                blueGames = blueGames,
                period = currentPeriod,
            )
        )
        if (undoStack.size > 40) undoStack.removeAt(0)
    }

    fun applyPreset(newPreset: ScoreboardPreset) {
        preset = newPreset
        redScore = newPreset.startScore
        blueScore = newPreset.startScore
        redSets = 0
        blueSets = 0
        redGames = 0
        blueGames = 0
        currentPeriod = 1
        timerRunning = false
        elapsedMs = 0L
        targetTimerMs = newPreset.defaultTimerMs
        undoStack.clear()
        viewModel.feedbackClick()
    }

    fun adjustScore(isRed: Boolean, delta: Int) {
        pushUndo()
        viewModel.feedbackClick()
        if (preset == ScoreboardPreset.TENNIS && delta > 0) {
            // 0=0, 1=15, 2=30, 3=40, 4=AD
            if (isRed) {
                when {
                    redScore < 3 -> redScore++
                    redScore == 3 && blueScore < 3 -> {
                        // Red wins game
                        redScore = 0; blueScore = 0; redGames++
                        if (redGames >= 6 && redGames - blueGames >= 2) {
                            redSets++; redGames = 0; blueGames = 0; currentPeriod++
                        }
                    }
                    redScore == 3 && blueScore == 3 -> redScore = 4 // AD
                    redScore == 3 && blueScore == 4 -> blueScore = 3 // Deuce
                    redScore == 4 -> {
                        redScore = 0; blueScore = 0; redGames++
                        if (redGames >= 6 && redGames - blueGames >= 2) {
                            redSets++; redGames = 0; blueGames = 0; currentPeriod++
                        }
                    }
                }
            } else {
                when {
                    blueScore < 3 -> blueScore++
                    blueScore == 3 && redScore < 3 -> {
                        redScore = 0; blueScore = 0; blueGames++
                        if (blueGames >= 6 && blueGames - redGames >= 2) {
                            blueSets++; redGames = 0; blueGames = 0; currentPeriod++
                        }
                    }
                    blueScore == 3 && redScore == 3 -> blueScore = 4
                    blueScore == 3 && redScore == 4 -> redScore = 3
                    blueScore == 4 -> {
                        redScore = 0; blueScore = 0; blueGames++
                        if (blueGames >= 6 && blueGames - redGames >= 2) {
                            blueSets++; redGames = 0; blueGames = 0; currentPeriod++
                        }
                    }
                }
            }
            return
        }

        if (isRed) {
            redScore = (redScore + delta).coerceAtLeast(0)
        } else {
            blueScore = (blueScore + delta).coerceAtLeast(0)
        }
    }

    fun undo() {
        if (undoStack.isEmpty()) return
        val last = undoStack.removeAt(undoStack.lastIndex)
        redScore = last.redScore
        blueScore = last.blueScore
        redSets = last.redSets
        blueSets = last.blueSets
        redGames = last.redGames
        blueGames = last.blueGames
        currentPeriod = last.period
        viewModel.feedbackClick()
    }

    fun displayScoreText(score: Int): String {
        if (preset == ScoreboardPreset.TENNIS) {
            return when (score) {
                0 -> "0"
                1 -> "15"
                2 -> "30"
                3 -> "40"
                else -> "AD"
            }
        }
        return score.toString()
    }

    val displayTimerMs = if (preset.countDownTimer && targetTimerMs > 0L) {
        (targetTimerMs - elapsedMs).coerceAtLeast(0L)
    } else {
        elapsedMs
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .then(if (fullscreen) Modifier.statusBarsPadding().navigationBarsPadding() else Modifier)
    ) {
        if (!fullscreen) {
            MonoToolTopBar(
                title = "Universal Scoreboard",
                subtitle = preset.label,
                onBack = onBack,
                isFavorite = isFavorite,
                onToggleFavorite = onToggleFavorite,
                actions = {
                    IconButton(
                        onClick = { showEditTeamsDialog = true },
                        modifier = Modifier.minimumInteractiveComponentSize(),
                    ) {
                        Icon(Icons.Outlined.Edit, contentDescription = "Rename teams", tint = palette.textPrimary)
                    }
                    IconButton(
                        onClick = { fullscreen = true },
                        modifier = Modifier.minimumInteractiveComponentSize().testTag("scoreboard_fullscreen_btn"),
                    ) {
                        Icon(Icons.Outlined.Fullscreen, contentDescription = "Fullscreen mode", tint = palette.textPrimary)
                    }
                },
            )

            // Preset selector
            LazyRow(
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                items(ScoreboardPreset.entries) { item ->
                    MonoChip(
                        label = item.label,
                        selected = preset == item,
                        onClick = { applyPreset(item) },
                    )
                }
            }
        }

        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            // Match Info Bar (Timer + Period/Set + Score Diff)
            MonoCard(
                modifier = Modifier.fillMaxWidth(),
                contentPadding = PaddingValues(horizontal = 14.dp, vertical = 10.dp),
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    // Period / Quarter / Set
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        val pLabel = if (preset == ScoreboardPreset.CUSTOM) customPeriodLabel else preset.periodLabel
                        IconButton(
                            onClick = { if (currentPeriod > 1) currentPeriod-- },
                            modifier = Modifier.size(34.dp),
                        ) {
                            Icon(Icons.Outlined.Remove, contentDescription = "Previous period", tint = palette.textSecondary, modifier = Modifier.size(16.dp))
                        }
                        Text(
                            text = "$pLabel $currentPeriod",
                            style = MaterialTheme.typography.labelLarge,
                            color = palette.textPrimary,
                            fontWeight = FontWeight.SemiBold,
                        )
                        IconButton(
                            onClick = { currentPeriod++ },
                            modifier = Modifier.size(34.dp),
                        ) {
                            Icon(Icons.Outlined.Add, contentDescription = "Next period", tint = palette.textSecondary, modifier = Modifier.size(16.dp))
                        }
                    }

                    // Match Timer
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                    ) {
                        Text(
                            text = TimeFormatter.formatMinSec(displayTimerMs),
                            style = TabularNumeralStyle.copy(
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold,
                            ),
                            color = if (preset.countDownTimer && displayTimerMs <= 10_000L && timerRunning) {
                                palette.warning
                            } else {
                                palette.textPrimary
                            },
                        )
                        IconButton(
                            onClick = {
                                timerRunning = !timerRunning
                                viewModel.feedbackClick()
                            },
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(palette.surfaceSecondary),
                        ) {
                            Icon(
                                imageVector = if (timerRunning) Icons.Outlined.Pause else Icons.Outlined.PlayArrow,
                                contentDescription = if (timerRunning) "Pause timer" else "Start timer",
                                tint = palette.textPrimary,
                                modifier = Modifier.size(18.dp),
                            )
                        }
                    }

                    // Diff / Fullscreen Exit
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        val diff = abs(redScore - blueScore)
                        Text(
                            text = if (preset == ScoreboardPreset.TENNIS) {
                                "Games $redGames-$blueGames"
                            } else {
                                "Diff $diff"
                            },
                            style = MaterialTheme.typography.labelMedium,
                            color = palette.textSecondary,
                        )
                        if (fullscreen) {
                            Spacer(Modifier.width(6.dp))
                            IconButton(
                                onClick = { fullscreen = false },
                                modifier = Modifier.size(36.dp),
                            ) {
                                Icon(Icons.Outlined.FullscreenExit, contentDescription = "Exit fullscreen", tint = palette.textPrimary)
                            }
                        }
                    }
                }

                // Set / Serve extras for Racquet & Volleyball
                if (preset in listOf(ScoreboardPreset.VOLLEYBALL, ScoreboardPreset.BADMINTON, ScoreboardPreset.TABLE_TENNIS, ScoreboardPreset.TENNIS)) {
                    Spacer(Modifier.height(6.dp))
                    FlowRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalArrangement = Arrangement.spacedBy(4.dp),
                    ) {
                        Text(
                            text = "Sets: $redSets – $blueSets",
                            style = MaterialTheme.typography.labelLarge,
                            color = palette.accent,
                        )
                        if (preset == ScoreboardPreset.TABLE_TENNIS) {
                            val totalPts = redScore + blueScore
                            val redServes = (totalPts / 2) % 2 == 0
                            Text(
                                text = "Serve: ${if (redServes) redName else blueName}",
                                style = MaterialTheme.typography.labelMedium,
                                color = palette.textSecondary,
                            )
                        }
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text(
                                text = "+Set $redName",
                                style = MaterialTheme.typography.labelMedium,
                                color = redColor,
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .clickable {
                                        pushUndo()
                                        redSets++
                                        redScore = 0
                                        blueScore = 0
                                        currentPeriod++
                                        viewModel.feedbackAccent()
                                    }
                                    .padding(horizontal = 6.dp, vertical = 4.dp),
                            )
                            Text(
                                text = "+Set $blueName",
                                style = MaterialTheme.typography.labelMedium,
                                color = blueColor,
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .clickable {
                                        pushUndo()
                                        blueSets++
                                        redScore = 0
                                        blueScore = 0
                                        currentPeriod++
                                        viewModel.feedbackAccent()
                                    }
                                    .padding(horizontal = 6.dp, vertical = 4.dp),
                            )
                        }
                    }
                }
            }

            // Opposing Red & Blue Score Panels
            Row(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                TeamScorePanel(
                    teamName = redName,
                    scoreText = displayScoreText(redScore),
                    teamColor = redColor,
                    isDartsActive = (preset == ScoreboardPreset.DARTS_301 || preset == ScoreboardPreset.DARTS_501) && dartsActiveSide == 0,
                    steps = if (preset == ScoreboardPreset.CUSTOM) listOf(customStep) else preset.defaultSteps,
                    onPlus = { step ->
                        if (preset == ScoreboardPreset.DARTS_301 || preset == ScoreboardPreset.DARTS_501) {
                            dartsActiveSide = 0
                        } else {
                            adjustScore(true, step)
                        }
                    },
                    onMinus = { adjustScore(true, -1) },
                    onManualEdit = { manualScoreTarget = 0 },
                    onPanelTap = {
                        if (preset == ScoreboardPreset.DARTS_301 || preset == ScoreboardPreset.DARTS_501) {
                            dartsActiveSide = 0
                        } else {
                            adjustScore(true, if (preset == ScoreboardPreset.CUSTOM) customStep else 1)
                        }
                    },
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .testTag("scoreboard_panel_red"),
                )

                TeamScorePanel(
                    teamName = blueName,
                    scoreText = displayScoreText(blueScore),
                    teamColor = blueColor,
                    isDartsActive = (preset == ScoreboardPreset.DARTS_301 || preset == ScoreboardPreset.DARTS_501) && dartsActiveSide == 1,
                    steps = if (preset == ScoreboardPreset.CUSTOM) listOf(customStep) else preset.defaultSteps,
                    onPlus = { step ->
                        if (preset == ScoreboardPreset.DARTS_301 || preset == ScoreboardPreset.DARTS_501) {
                            dartsActiveSide = 1
                        } else {
                            adjustScore(false, step)
                        }
                    },
                    onMinus = { adjustScore(false, -1) },
                    onManualEdit = { manualScoreTarget = 1 },
                    onPanelTap = {
                        if (preset == ScoreboardPreset.DARTS_301 || preset == ScoreboardPreset.DARTS_501) {
                            dartsActiveSide = 1
                        } else {
                            adjustScore(false, if (preset == ScoreboardPreset.CUSTOM) customStep else 1)
                        }
                    },
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .testTag("scoreboard_panel_blue"),
                )
            }

            // Darts Turn Throw Calculator
            if (preset == ScoreboardPreset.DARTS_301 || preset == ScoreboardPreset.DARTS_501) {
                MonoCard(contentPadding = PaddingValues(12.dp)) {
                    val activeRemaining = if (dartsActiveSide == 0) redScore else blueScore
                    val activeTeam = if (dartsActiveSide == 0) redName else blueName
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            text = "Throw for $activeTeam (Rem: $activeRemaining)",
                            style = MaterialTheme.typography.labelLarge,
                            color = palette.textPrimary,
                        )
                        if (activeRemaining in 2..170 && activeRemaining !in listOf(169, 168, 166, 165, 163, 162, 159)) {
                            Text(
                                text = "Checkout window!",
                                style = MaterialTheme.typography.labelMedium,
                                color = palette.success,
                            )
                        }
                    }
                    Spacer(Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        OutlinedTextField(
                            value = dartsInput,
                            onValueChange = { dartsInput = it.filter { ch -> ch.isDigit() }.take(3) },
                            label = { Text("Turn Score (0-180)") },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.weight(1f),
                        )
                        Surface(
                            onClick = {
                                val pts = dartsInput.toIntOrNull() ?: return@Surface
                                if (pts > 180) {
                                    Toast.makeText(context, "Max 3-dart score is 180", Toast.LENGTH_SHORT).show()
                                    return@Surface
                                }
                                pushUndo()
                                val current = if (dartsActiveSide == 0) redScore else blueScore
                                val next = current - pts
                                if (next < 0 || next == 1) {
                                    Toast.makeText(context, "Bust! Score stays $current", Toast.LENGTH_SHORT).show()
                                    viewModel.feedbackAlert()
                                } else {
                                    if (dartsActiveSide == 0) redScore = next else blueScore = next
                                    if (next == 0) {
                                        Toast.makeText(context, "$activeTeam Checkout!", Toast.LENGTH_SHORT).show()
                                        viewModel.feedbackAccent()
                                    } else {
                                        viewModel.feedbackClick()
                                    }
                                }
                                dartsInput = ""
                                dartsActiveSide = 1 - dartsActiveSide
                            },
                            shape = RoundedCornerShape(12.dp),
                            color = palette.accent,
                            modifier = Modifier.minimumInteractiveComponentSize(),
                        ) {
                            Text(
                                text = "Apply",
                                color = palette.onAccent,
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.SemiBold,
                                modifier = Modifier.padding(horizontal = 18.dp, vertical = 14.dp),
                            )
                        }
                    }
                }
            }

            // Bottom Action Bar
            MonoCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .then(if (!fullscreen) Modifier.navigationBarsPadding() else Modifier),
                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    IconButton(
                        onClick = { undo() },
                        enabled = undoStack.isNotEmpty(),
                        modifier = Modifier.minimumInteractiveComponentSize().testTag("scoreboard_undo_btn"),
                    ) {
                        Icon(
                            Icons.AutoMirrored.Outlined.Undo,
                            contentDescription = "Undo",
                            tint = if (undoStack.isNotEmpty()) palette.textPrimary else palette.textMuted,
                        )
                    }
                    IconButton(
                        onClick = { showNotesDialog = true },
                        modifier = Modifier.minimumInteractiveComponentSize(),
                    ) {
                        Icon(Icons.Outlined.Notes, contentDescription = "Match notes", tint = palette.textPrimary)
                    }
                    if (preset == ScoreboardPreset.CUSTOM) {
                        IconButton(
                            onClick = { showCustomConfigDialog = true },
                            modifier = Modifier.minimumInteractiveComponentSize(),
                        ) {
                            Icon(Icons.Outlined.Edit, contentDescription = "Custom rules", tint = palette.textPrimary)
                        }
                    }
                    IconButton(
                        onClick = {
                            viewModel.logHistory(
                                toolId = UtilityIds.SCOREBOARD,
                                title = "${preset.label}: $redName vs $blueName",
                                summary = "$redName $redScore – $blueScore $blueName (${preset.periodLabel} $currentPeriod)",
                                detailText = matchNotes.ifBlank { "Match duration: ${TimeFormatter.formatMinSec(elapsedMs)}" },
                                primaryValue = "$redScore : $blueScore",
                                numericValueMs = elapsedMs,
                            )
                            viewModel.feedbackAccent()
                            Toast.makeText(context, "Match saved to History", Toast.LENGTH_SHORT).show()
                        },
                        modifier = Modifier.minimumInteractiveComponentSize().testTag("scoreboard_save_btn"),
                    ) {
                        Icon(Icons.Outlined.Save, contentDescription = "Save match", tint = palette.textPrimary)
                    }
                    IconButton(
                        onClick = { applyPreset(preset) },
                        modifier = Modifier.minimumInteractiveComponentSize().testTag("scoreboard_reset_btn"),
                    ) {
                        Icon(Icons.Outlined.Refresh, contentDescription = "Reset match", tint = palette.textPrimary)
                    }
                }
            }
        }
    }

    // Rename & Color Dialog
    if (showEditTeamsDialog) {
        var rName by remember { mutableStateOf(redName) }
        var bName by remember { mutableStateOf(blueName) }
        AlertDialog(
            onDismissRequest = { showEditTeamsDialog = false },
            containerColor = palette.cardElevated,
            titleContentColor = palette.textPrimary,
            title = { Text("Customize Teams") },
            text = {
                Column(
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.verticalScroll(rememberScrollState()),
                ) {
                    OutlinedTextField(
                        value = rName,
                        onValueChange = { rName = it },
                        label = { Text("Left Team Name") },
                        singleLine = true,
                    )
                    OutlinedTextField(
                        value = bName,
                        onValueChange = { bName = it },
                        label = { Text("Right Team Name") },
                        singleLine = true,
                    )
                    Text("Left Accent", style = MaterialTheme.typography.labelMedium, color = palette.textSecondary)
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        listOf(MonoTeamRed, Color(0xFFB33951), Color(0xFFD97724), Color(0xFF34A853)).forEach { c ->
                            Box(
                                modifier = Modifier
                                    .size(32.dp)
                                    .clip(CircleShape)
                                    .background(c)
                                    .border(if (redColor == c) 2.dp else 0.dp, Color.White, CircleShape)
                                    .clickable { redColor = c }
                            )
                        }
                    }
                    Text("Right Accent", style = MaterialTheme.typography.labelMedium, color = palette.textSecondary)
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        listOf(MonoTeamBlue, Color(0xFF1D8A99), Color(0xFF5C6BC0), Color(0xFF707070)).forEach { c ->
                            Box(
                                modifier = Modifier
                                    .size(32.dp)
                                    .clip(CircleShape)
                                    .background(c)
                                    .border(if (blueColor == c) 2.dp else 0.dp, Color.White, CircleShape)
                                    .clickable { blueColor = c }
                            )
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    redName = rName.ifBlank { "Red Team" }
                    blueName = bName.ifBlank { "Blue Team" }
                    showEditTeamsDialog = false
                }) { Text("Save", color = palette.accent) }
            },
            dismissButton = {
                TextButton(onClick = { showEditTeamsDialog = false }) { Text("Cancel", color = palette.textSecondary) }
            },
        )
    }

    // Manual Score Entry Dialog
    manualScoreTarget?.let { target ->
        var input by remember(target) {
            mutableStateOf((if (target == 0) redScore else blueScore).toString())
        }
        AlertDialog(
            onDismissRequest = { manualScoreTarget = null },
            containerColor = palette.cardElevated,
            titleContentColor = palette.textPrimary,
            title = { Text("Set Score — ${if (target == 0) redName else blueName}") },
            text = {
                OutlinedTextField(
                    value = input,
                    onValueChange = { input = it.filter { c -> c.isDigit() }.take(5) },
                    label = { Text("Score") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    val value = input.toIntOrNull()
                    if (value != null) {
                        pushUndo()
                        if (target == 0) redScore = value else blueScore = value
                    }
                    manualScoreTarget = null
                }) { Text("Apply", color = palette.accent) }
            },
            dismissButton = {
                TextButton(onClick = { manualScoreTarget = null }) { Text("Cancel", color = palette.textSecondary) }
            },
        )
    }

    // Match Notes Dialog
    if (showNotesDialog) {
        var noteDraft by remember { mutableStateOf(matchNotes) }
        AlertDialog(
            onDismissRequest = { showNotesDialog = false },
            containerColor = palette.cardElevated,
            titleContentColor = palette.textPrimary,
            title = { Text("Match Notes") },
            text = {
                OutlinedTextField(
                    value = noteDraft,
                    onValueChange = { noteDraft = it },
                    label = { Text("Venue, fouls, timeouts, highlights...") },
                    minLines = 3,
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    matchNotes = noteDraft
                    showNotesDialog = false
                }) { Text("Done", color = palette.accent) }
            },
        )
    }

    // Custom Match Rules Dialog
    if (showCustomConfigDialog) {
        var stepStr by remember { mutableStateOf(customStep.toString()) }
        var pLabel by remember { mutableStateOf(customPeriodLabel) }
        var minsStr by remember { mutableStateOf((targetTimerMs / 60_000L).toString()) }
        AlertDialog(
            onDismissRequest = { showCustomConfigDialog = false },
            containerColor = palette.cardElevated,
            titleContentColor = palette.textPrimary,
            title = { Text("Custom Match Rules") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = stepStr,
                        onValueChange = { stepStr = it.filter { c -> c.isDigit() }.take(3) },
                        label = { Text("Score Step (+N per tap)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                    )
                    OutlinedTextField(
                        value = pLabel,
                        onValueChange = { pLabel = it },
                        label = { Text("Period Label (e.g. Round, Inning)") },
                        singleLine = true,
                    )
                    OutlinedTextField(
                        value = minsStr,
                        onValueChange = { minsStr = it.filter { c -> c.isDigit() }.take(3) },
                        label = { Text("Countdown Minutes (0 = count up)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    customStep = (stepStr.toIntOrNull() ?: 1).coerceAtLeast(1)
                    customPeriodLabel = pLabel.ifBlank { "Period" }
                    val mins = (minsStr.toLongOrNull() ?: 0L).coerceAtLeast(0L)
                    targetTimerMs = mins * 60_000L
                    elapsedMs = 0L
                    timerRunning = false
                    showCustomConfigDialog = false
                }) { Text("Apply", color = palette.accent) }
            },
            dismissButton = {
                TextButton(onClick = { showCustomConfigDialog = false }) { Text("Cancel", color = palette.textSecondary) }
            },
        )
    }
}

@Composable
private fun TeamScorePanel(
    teamName: String,
    scoreText: String,
    teamColor: Color,
    isDartsActive: Boolean,
    steps: List<Int>,
    onPlus: (Int) -> Unit,
    onMinus: () -> Unit,
    onManualEdit: () -> Unit,
    onPanelTap: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val palette = MonoTheme.palette
    val borderColor = if (isDartsActive) Color.White else teamColor.copy(alpha = 0.7f)
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(22.dp))
            .background(
                Brush.verticalGradient(
                    listOf(
                        teamColor.copy(alpha = 0.28f),
                        teamColor.copy(alpha = 0.14f),
                        palette.cardPrimary,
                    )
                )
            )
            .border(if (isDartsActive) 2.dp else 1.5.dp, borderColor, RoundedCornerShape(22.dp))
            .clickable(onClick = onPanelTap)
            .padding(14.dp),
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween,
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Box(
                    modifier = Modifier
                        .size(10.dp)
                        .clip(CircleShape)
                        .background(teamColor)
                )
                Text(
                    text = teamName,
                    style = MaterialTheme.typography.titleMedium,
                    color = palette.textPrimary,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.weight(1f).padding(horizontal = 6.dp),
                )
                IconButton(
                    onClick = onManualEdit,
                    modifier = Modifier.size(32.dp),
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Edit,
                        contentDescription = "Edit $teamName score",
                        tint = palette.textSecondary,
                        modifier = Modifier.size(16.dp),
                    )
                }
            }

            val scoreFontSize = when {
                scoreText.length >= 4 -> 40.sp
                scoreText.length == 3 -> 48.sp
                else -> 64.sp
            }
            Text(
                text = scoreText,
                style = TabularNumeralStyle.copy(
                    fontSize = scoreFontSize,
                    fontWeight = FontWeight.Bold,
                ),
                color = palette.textPrimary,
                maxLines = 1,
            )

            if (steps.size > 1) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                    ) {
                        steps.forEach { step ->
                            Surface(
                                onClick = { onPlus(step) },
                                shape = RoundedCornerShape(12.dp),
                                color = teamColor.copy(alpha = 0.88f),
                                modifier = Modifier
                                    .weight(1f)
                                    .minimumInteractiveComponentSize(),
                            ) {
                                Box(
                                    modifier = Modifier.padding(vertical = 12.dp),
                                    contentAlignment = Alignment.Center,
                                ) {
                                    Text(
                                        text = "+$step",
                                        style = MaterialTheme.typography.titleSmall,
                                        color = Color.White,
                                        fontWeight = FontWeight.Bold,
                                        maxLines = 1,
                                    )
                                }
                            }
                        }
                    }
                    Surface(
                        onClick = onMinus,
                        shape = RoundedCornerShape(12.dp),
                        color = palette.surfaceSecondary.copy(alpha = 0.85f),
                        border = BorderStroke(1.dp, palette.border),
                        modifier = Modifier
                            .fillMaxWidth()
                            .minimumInteractiveComponentSize(),
                    ) {
                        Row(
                            modifier = Modifier.padding(vertical = 10.dp),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Icon(Icons.Outlined.Remove, contentDescription = "Decrease $teamName", tint = palette.textPrimary, modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(4.dp))
                            Text(
                                text = "1",
                                style = MaterialTheme.typography.labelLarge,
                                color = palette.textPrimary,
                                fontWeight = FontWeight.SemiBold,
                            )
                        }
                    }
                }
            } else {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Surface(
                        onClick = onMinus,
                        shape = RoundedCornerShape(12.dp),
                        color = palette.surfaceSecondary.copy(alpha = 0.85f),
                        border = BorderStroke(1.dp, palette.border),
                        modifier = Modifier
                            .weight(1f)
                            .minimumInteractiveComponentSize(),
                    ) {
                        Box(
                            modifier = Modifier.padding(vertical = 12.dp),
                            contentAlignment = Alignment.Center,
                        ) {
                            Icon(Icons.Outlined.Remove, contentDescription = "Decrease $teamName", tint = palette.textPrimary, modifier = Modifier.size(18.dp))
                        }
                    }

                    steps.forEach { step ->
                        Surface(
                            onClick = { onPlus(step) },
                            shape = RoundedCornerShape(12.dp),
                            color = teamColor.copy(alpha = 0.88f),
                            modifier = Modifier
                                .weight(1.4f)
                                .minimumInteractiveComponentSize(),
                        ) {
                            Box(
                                modifier = Modifier.padding(vertical = 12.dp),
                                contentAlignment = Alignment.Center,
                            ) {
                                Text(
                                    text = "+$step",
                                    style = MaterialTheme.typography.titleSmall,
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold,
                                    maxLines = 1,
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
