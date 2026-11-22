package com.example.ui.tools.puzzles

import android.os.SystemClock
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
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
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.NavigateBefore
import androidx.compose.material.icons.automirrored.outlined.NavigateNext
import androidx.compose.material.icons.outlined.Casino
import androidx.compose.material.icons.outlined.DeleteOutline
import androidx.compose.material.icons.outlined.Pause
import androidx.compose.material.icons.outlined.PlayArrow
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.local.HistoryRecordEntity
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
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

enum class CubePuzzleType(val label: String) {
    CUBE_3X3("3x3"),
    CUBE_2X2("2x2"),
    CUBE_4X4("4x4"),
    PYRAMINX("Pyraminx"),
    SKEWB("Skewb"),
}

private enum class CubeTimerPhase {
    IDLE,
    INSPECTING,
    READY,
    RUNNING,
}

private fun generateScrambleFor(type: CubePuzzleType): String = when (type) {
    CubePuzzleType.CUBE_3X3 -> ScrambleGenerator.generate3x3().joinToString(" ")
    CubePuzzleType.CUBE_2X2 -> ScrambleGenerator.generate2x2().joinToString(" ")
    CubePuzzleType.CUBE_4X4 -> ScrambleGenerator.generate4x4().joinToString(" ")
    CubePuzzleType.PYRAMINX -> ScrambleGenerator.generatePyraminx().joinToString(" ")
    CubePuzzleType.SKEWB -> ScrambleGenerator.generateSkewb().joinToString(" ")
}

private fun effectiveSolveMs(record: HistoryRecordEntity): Long? = when (record.penaltyFlag) {
    "DNF" -> null
    "+2" -> record.numericValueMs + 2000L
    else -> record.numericValueMs
}

private fun computeWcaAverage(records: List<HistoryRecordEntity>, size: Int): String {
    if (records.size < size) return "—"
    val window = records.take(size).map(::effectiveSolveMs)
    val dnfCount = window.count { it == null }
    if (dnfCount >= 2) return "DNF"
    val sortedValues = window.map { it ?: Long.MAX_VALUE }.sorted()
    val trimmed = sortedValues.subList(1, size - 1)
    val avgMs = trimmed.sum() / trimmed.size
    return TimeFormatter.formatCubeTime(avgMs)
}

@Composable
fun CubeTimerScreen(
    viewModel: MainViewModel,
    isFavorite: Boolean,
    defaultInspection: Boolean,
    keepScreenAwake: Boolean,
    onBack: () -> Unit,
    onToggleFavorite: () -> Unit,
) {
    val palette = MonoTheme.palette
    KeepScreenOnEffect(keepScreenAwake)

    val allSolves by viewModel.observeHistoryForTool(UtilityIds.CUBE_TIMER)
        .collectAsStateWithLifecycle(initialValue = emptyList())

    var puzzleType by remember { mutableStateOf(CubePuzzleType.CUBE_3X3) }
    var scramble by remember { mutableStateOf(generateScrambleFor(CubePuzzleType.CUBE_3X3)) }
    var inspectionEnabled by remember(defaultInspection) { mutableStateOf(defaultInspection) }

    val solves = remember(allSolves, puzzleType) {
        allSolves.filter { it.title == puzzleType.label }
    }

    var phase by remember { mutableStateOf(CubeTimerPhase.IDLE) }
    var elapsedMs by remember { mutableLongStateOf(0L) }
    var startNanos by remember { mutableLongStateOf(0L) }
    var inspectionStartMs by remember { mutableLongStateOf(0L) }
    var inspectionSecondsLeft by remember { mutableIntStateOf(15) }
    var inspectionPenalty by remember { mutableStateOf("") }
    var cue8Fired by remember { mutableStateOf(false) }
    var cue12Fired by remember { mutableStateOf(false) }

    LaunchedEffect(phase) {
        when (phase) {
            CubeTimerPhase.INSPECTING -> {
                inspectionStartMs = SystemClock.elapsedRealtime()
                cue8Fired = false
                cue12Fired = false
                while (phase == CubeTimerPhase.INSPECTING) {
                    val elapsedSec = ((SystemClock.elapsedRealtime() - inspectionStartMs) / 1000L).toInt()
                    inspectionSecondsLeft = 15 - elapsedSec
                    if (elapsedSec >= 8 && !cue8Fired) {
                        cue8Fired = true
                        viewModel.feedbackBeep()
                    }
                    if (elapsedSec >= 12 && !cue12Fired) {
                        cue12Fired = true
                        viewModel.feedbackBeep()
                    }
                    inspectionPenalty = when {
                        elapsedSec >= 17 -> "DNF"
                        elapsedSec >= 15 -> "+2"
                        else -> ""
                    }
                    delay(100L)
                }
            }
            CubeTimerPhase.RUNNING -> {
                startNanos = SystemClock.elapsedRealtimeNanos()
                while (phase == CubeTimerPhase.RUNNING) {
                    elapsedMs = (SystemClock.elapsedRealtimeNanos() - startNanos) / 1_000_000L
                    delay(16L)
                }
            }
            else -> Unit
        }
    }

    val pbText = remember(solves) {
        val valid = solves.mapNotNull(::effectiveSolveMs)
        if (valid.isEmpty()) "—" else TimeFormatter.formatCubeTime(valid.min())
    }
    val ao5Text = remember(solves) { computeWcaAverage(solves, 5) }
    val ao12Text = remember(solves) { computeWcaAverage(solves, 12) }
    val meanText = remember(solves) {
        val valid = solves.mapNotNull(::effectiveSolveMs)
        if (valid.isEmpty()) "—" else TimeFormatter.formatCubeTime(valid.sum() / valid.size)
    }

    Column(modifier = Modifier.fillMaxSize()) {
        MonoToolTopBar(
            title = "Rubik's Cube Timer",
            subtitle = "${puzzleType.label} • ${solves.size} solves",
            onBack = onBack,
            isFavorite = isFavorite,
            onToggleFavorite = onToggleFavorite,
        )

        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(horizontal = 16.dp, vertical = 6.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            if (phase != CubeTimerPhase.RUNNING) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    LazyRow(
                        modifier = Modifier.weight(1f),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        items(CubePuzzleType.entries) { p ->
                            MonoChip(
                                label = p.label,
                                selected = puzzleType == p,
                                onClick = {
                                    puzzleType = p
                                    scramble = generateScrambleFor(p)
                                    phase = CubeTimerPhase.IDLE
                                    elapsedMs = 0L
                                },
                            )
                        }
                    }
                    Spacer(Modifier.width(8.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("15s", style = MaterialTheme.typography.labelMedium, color = palette.textSecondary)
                        Spacer(Modifier.width(4.dp))
                        Switch(checked = inspectionEnabled, onCheckedChange = { inspectionEnabled = it })
                    }
                }

                MonoCard(contentPadding = PaddingValues(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            text = scramble,
                            style = TabularNumeralStyle.copy(fontSize = 14.sp, lineHeight = 20.sp),
                            color = palette.textPrimary,
                            modifier = Modifier.weight(1f),
                        )
                        IconButton(
                            onClick = { scramble = generateScrambleFor(puzzleType) },
                            modifier = Modifier.minimumInteractiveComponentSize(),
                        ) {
                            Icon(Icons.Outlined.Refresh, contentDescription = "New scramble", tint = palette.textSecondary)
                        }
                    }
                }
            }

            // Main Touch Pad
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(24.dp))
                    .background(
                        when (phase) {
                            CubeTimerPhase.READY -> palette.success.copy(alpha = 0.2f)
                            CubeTimerPhase.INSPECTING -> palette.warning.copy(alpha = 0.14f)
                            else -> palette.cardPrimary
                        }
                    )
                    .border(
                        width = 1.5.dp,
                        color = when (phase) {
                            CubeTimerPhase.READY -> palette.success
                            CubeTimerPhase.INSPECTING -> palette.warning
                            CubeTimerPhase.RUNNING -> palette.accent
                            CubeTimerPhase.IDLE -> palette.border
                        },
                        shape = RoundedCornerShape(24.dp),
                    )
                    .pointerInput(puzzleType, inspectionEnabled) {
                        detectTapGestures(
                            onPress = {
                                when (phase) {
                                    CubeTimerPhase.RUNNING -> {
                                        val finalMs = (SystemClock.elapsedRealtimeNanos() - startNanos) / 1_000_000L
                                        elapsedMs = finalMs
                                        phase = CubeTimerPhase.IDLE
                                        viewModel.feedbackAccent()
                                        val formatted = TimeFormatter.formatCubeTime(finalMs, inspectionPenalty)
                                        viewModel.logHistory(
                                            toolId = UtilityIds.CUBE_TIMER,
                                            title = puzzleType.label,
                                            summary = "Scramble: $scramble",
                                            detailText = "Puzzle: ${puzzleType.label}\nScramble: $scramble",
                                            primaryValue = formatted,
                                            numericValueMs = finalMs,
                                            penaltyFlag = inspectionPenalty,
                                        )
                                        inspectionPenalty = ""
                                        scramble = generateScrambleFor(puzzleType)
                                    }
                                    CubeTimerPhase.IDLE -> {
                                        if (inspectionEnabled) {
                                            phase = CubeTimerPhase.INSPECTING
                                            viewModel.feedbackClick()
                                        } else {
                                            phase = CubeTimerPhase.READY
                                            val released = tryAwaitRelease()
                                            if (released) {
                                                phase = CubeTimerPhase.RUNNING
                                                viewModel.feedbackAccent()
                                            } else {
                                                phase = CubeTimerPhase.IDLE
                                            }
                                        }
                                    }
                                    CubeTimerPhase.INSPECTING -> {
                                        phase = CubeTimerPhase.READY
                                        val released = tryAwaitRelease()
                                        if (released) {
                                            phase = CubeTimerPhase.RUNNING
                                            viewModel.feedbackAccent()
                                        } else {
                                            phase = CubeTimerPhase.INSPECTING
                                        }
                                    }
                                    CubeTimerPhase.READY -> Unit
                                }
                            }
                        )
                    }
                    .testTag("cube_timer_pad"),
                contentAlignment = Alignment.Center,
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    val mainText = when (phase) {
                        CubeTimerPhase.INSPECTING -> when {
                            inspectionPenalty == "DNF" -> "DNF"
                            inspectionPenalty == "+2" -> "+2"
                            else -> "$inspectionSecondsLeft"
                        }
                        CubeTimerPhase.READY -> "0.00"
                        CubeTimerPhase.RUNNING, CubeTimerPhase.IDLE -> TimeFormatter.formatCubeTime(elapsedMs)
                    }
                    Text(
                        text = mainText,
                        style = TabularNumeralStyle.copy(fontSize = 64.sp, fontWeight = FontWeight.Bold),
                        color = when (phase) {
                            CubeTimerPhase.READY -> palette.success
                            CubeTimerPhase.INSPECTING -> palette.warning
                            else -> palette.textPrimary
                        },
                    )
                    Spacer(Modifier.height(10.dp))
                    Text(
                        text = when (phase) {
                            CubeTimerPhase.IDLE -> if (inspectionEnabled) "Tap to start 15s inspection" else "Hold & release to start"
                            CubeTimerPhase.INSPECTING -> "Hold & release to start solve"
                            CubeTimerPhase.READY -> "Release to start!"
                            CubeTimerPhase.RUNNING -> "Tap anywhere to stop"
                        },
                        style = MaterialTheme.typography.bodyMedium,
                        color = palette.textSecondary,
                    )
                }
            }

            if (phase != CubeTimerPhase.RUNNING) {
                // Session Stats Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    StatMiniCard("PB", pbText, Modifier.weight(1f))
                    StatMiniCard("Ao5", ao5Text, Modifier.weight(1f))
                    StatMiniCard("Ao12", ao12Text, Modifier.weight(1f))
                    StatMiniCard("Mean", meanText, Modifier.weight(1f))
                }

                // Recent Solves
                if (solves.isNotEmpty()) {
                    LazyColumn(
                        modifier = Modifier.height(150.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp),
                    ) {
                        items(solves.take(20), key = { it.id }) { rec ->
                            MonoCard(contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                ) {
                                    Text(
                                        text = TimeFormatter.formatCubeTime(rec.numericValueMs, rec.penaltyFlag),
                                        style = TabularNumeralStyle.copy(fontSize = 16.sp, fontWeight = FontWeight.Bold),
                                        color = if (rec.penaltyFlag == "DNF") palette.teamRed else palette.textPrimary,
                                    )
                                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
                                        PenaltyChip("+2", rec.penaltyFlag == "+2") {
                                            val nextFlag = if (rec.penaltyFlag == "+2") "" else "+2"
                                            viewModel.updateHistoryRecord(
                                                rec.copy(
                                                    penaltyFlag = nextFlag,
                                                    primaryValue = TimeFormatter.formatCubeTime(rec.numericValueMs, nextFlag),
                                                )
                                            )
                                        }
                                        PenaltyChip("DNF", rec.penaltyFlag == "DNF") {
                                            val nextFlag = if (rec.penaltyFlag == "DNF") "" else "DNF"
                                            viewModel.updateHistoryRecord(
                                                rec.copy(
                                                    penaltyFlag = nextFlag,
                                                    primaryValue = TimeFormatter.formatCubeTime(rec.numericValueMs, nextFlag),
                                                )
                                            )
                                        }
                                        IconButton(
                                            onClick = { viewModel.deleteHistoryRecord(rec.id) },
                                            modifier = Modifier.size(32.dp),
                                        ) {
                                            Icon(Icons.Outlined.DeleteOutline, contentDescription = "Delete solve", tint = palette.textMuted, modifier = Modifier.size(18.dp))
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun StatMiniCard(label: String, value: String, modifier: Modifier = Modifier) {
    val palette = MonoTheme.palette
    MonoCard(modifier = modifier, contentPadding = PaddingValues(horizontal = 10.dp, vertical = 8.dp)) {
        Text(label, style = MaterialTheme.typography.labelSmall, color = palette.textMuted)
        Text(
            text = value,
            style = TabularNumeralStyle.copy(fontSize = 15.sp, fontWeight = FontWeight.Bold),
            color = palette.textPrimary,
            maxLines = 1,
        )
    }
}

@Composable
private fun PenaltyChip(label: String, active: Boolean, onClick: () -> Unit) {
    val palette = MonoTheme.palette
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(8.dp),
        color = if (active) palette.accent else palette.surfaceSecondary,
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = if (active) palette.onAccent else palette.textSecondary,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
        )
    }
}

// -----------------------------------------------------------------------------
// 3x3 Rubik's Cube Solver
// -----------------------------------------------------------------------------

private val cubeStickerColors = listOf(
    Color(0xFFF5F5F5), // 0 = White (U)
    Color(0xFFD9383A), // 1 = Red (R)
    Color(0xFF2FA84F), // 2 = Green (F)
    Color(0xFFF2C94C), // 3 = Yellow (D)
    Color(0xFFE67E22), // 4 = Orange (L)
    Color(0xFF2E6FD8), // 5 = Blue (B)
)
private val cubeColorNames = listOf("W", "R", "G", "Y", "O", "B")

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun CubeSolverScreen(
    viewModel: MainViewModel,
    isFavorite: Boolean,
    onBack: () -> Unit,
    onToggleFavorite: () -> Unit,
) {
    val palette = MonoTheme.palette
    val scope = rememberCoroutineScope()

    var inputState by remember { mutableStateOf(CubeState.solved()) }
    var selectedColor by remember { mutableIntStateOf(0) }
    var solving by remember { mutableStateOf(false) }
    var errorMsg by remember { mutableStateOf<String?>(null) }

    var phases by remember { mutableStateOf<List<Cube3x3Engine.SolveStep>>(emptyList()) }
    var flatMoves by remember { mutableStateOf<List<String>>(emptyList()) }
    var stepIndex by remember { mutableIntStateOf(0) }
    var autoPlaying by remember { mutableStateOf(false) }

    val displayState = remember(inputState, flatMoves, stepIndex) {
        if (flatMoves.isEmpty() || stepIndex == 0) inputState
        else Cube3x3Engine.applyMoves(inputState, flatMoves.take(stepIndex))
    }

    LaunchedEffect(autoPlaying, stepIndex, flatMoves) {
        if (autoPlaying && stepIndex < flatMoves.size) {
            delay(650L)
            stepIndex++
            viewModel.feedbackClick()
        } else if (stepIndex >= flatMoves.size) {
            autoPlaying = false
        }
    }

    fun solveNow() {
        autoPlaying = false
        errorMsg = null
        solving = true
        val snapshot = inputState.copy()
        scope.launch {
            val res = withContext(Dispatchers.Default) { Cube3x3Engine.solve(snapshot) }
            solving = false
            res.onSuccess { solvedPhases ->
                phases = solvedPhases
                flatMoves = solvedPhases.flatMap { it.moves }
                stepIndex = 0
                if (flatMoves.isEmpty()) {
                    errorMsg = "Cube is already solved!"
                } else {
                    viewModel.feedbackAccent()
                }
            }.onFailure { err ->
                phases = emptyList()
                flatMoves = emptyList()
                errorMsg = err.message ?: "Invalid cube state"
                viewModel.feedbackAlert()
            }
        }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        MonoToolTopBar(
            title = "3x3 Cube Solver",
            subtitle = "CFOP multi-stage solver & visualizer",
            onBack = onBack,
            isFavorite = isFavorite,
            onToggleFavorite = onToggleFavorite,
        )

        LazyColumn(
            modifier = Modifier.weight(1f).navigationBarsPadding(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            // Color Brush Palette (stacked cleanly for narrow mobile widths)
            item {
                MonoCard(contentPadding = PaddingValues(14.dp)) {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text(
                            text = "Tap a color below, then tap any sticker on the net",
                            style = MaterialTheme.typography.labelLarge,
                            color = palette.textSecondary,
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            cubeStickerColors.forEachIndexed { idx, col ->
                                Box(
                                    modifier = Modifier
                                        .size(38.dp)
                                        .clip(CircleShape)
                                        .background(col)
                                        .border(
                                            width = if (selectedColor == idx) 3.dp else 1.dp,
                                            color = if (selectedColor == idx) palette.textPrimary else palette.border,
                                            shape = CircleShape,
                                        )
                                        .clickable { selectedColor = idx },
                                    contentAlignment = Alignment.Center,
                                ) {
                                    Text(
                                        cubeColorNames[idx],
                                        style = MaterialTheme.typography.labelSmall,
                                        color = if (idx == 0 || idx == 3) Color.Black else Color.White,
                                        fontWeight = FontWeight.Bold,
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Responsive Interactive 2D Unfolded Cube Net
            item {
                MonoCard(elevated = true, contentPadding = PaddingValues(12.dp)) {
                    BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
                        val faceSize = ((maxWidth - 20.dp) / 4f).coerceIn(54.dp, 80.dp)
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(4.dp),
                        ) {
                            // Row 1: U
                            Row {
                                Spacer(Modifier.size(faceSize + 4.dp))
                                CubeFaceGrid(displayState, 0, faceSize) { idx ->
                                    if (flatMoves.isEmpty()) {
                                        val arr = inputState.stickers.copyOf()
                                        arr[idx] = selectedColor
                                        inputState = CubeState(arr)
                                    }
                                }
                                Spacer(Modifier.size((faceSize + 4.dp) * 2))
                            }
                            // Row 2: L F R B
                            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                listOf(4, 2, 1, 5).forEach { face ->
                                    CubeFaceGrid(displayState, face, faceSize) { idx ->
                                        if (flatMoves.isEmpty()) {
                                            val arr = inputState.stickers.copyOf()
                                            arr[idx] = selectedColor
                                            inputState = CubeState(arr)
                                        }
                                    }
                                }
                            }
                            // Row 3: D
                            Row {
                                Spacer(Modifier.size(faceSize + 4.dp))
                                CubeFaceGrid(displayState, 3, faceSize) { idx ->
                                    if (flatMoves.isEmpty()) {
                                        val arr = inputState.stickers.copyOf()
                                        arr[idx] = selectedColor
                                        inputState = CubeState(arr)
                                    }
                                }
                                Spacer(Modifier.size((faceSize + 4.dp) * 2))
                            }
                        }
                    }
                }
            }

            // Action Controls
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    MonoSecondaryButton(
                        text = "Scramble",
                        icon = Icons.Outlined.Casino,
                        onClick = {
                            autoPlaying = false
                            phases = emptyList()
                            flatMoves = emptyList()
                            stepIndex = 0
                            errorMsg = null
                            inputState = Cube3x3Engine.applyMoves(CubeState.solved(), ScrambleGenerator.generate3x3(18))
                        },
                        modifier = Modifier.weight(1f),
                    )
                    MonoSecondaryButton(
                        text = "Reset",
                        icon = Icons.Outlined.Refresh,
                        onClick = {
                            autoPlaying = false
                            phases = emptyList()
                            flatMoves = emptyList()
                            stepIndex = 0
                            errorMsg = null
                            inputState = CubeState.solved()
                        },
                        modifier = Modifier.weight(1f),
                    )
                }
                Spacer(Modifier.height(8.dp))
                MonoPrimaryButton(
                    text = if (solving) "Solving..." else "Validate & Solve Cube",
                    onClick = { solveNow() },
                    enabled = !solving,
                    modifier = Modifier.fillMaxWidth().testTag("cube_solve_button"),
                )
            }

            if (solving) {
                item {
                    Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = palette.accent, modifier = Modifier.size(28.dp))
                    }
                }
            }

            errorMsg?.let { msg ->
                item {
                    MonoCard(borderColor = palette.teamRed, contentPadding = PaddingValues(14.dp)) {
                        Text(msg, color = palette.teamRed, style = MaterialTheme.typography.bodyMedium)
                    }
                }
            }

            if (flatMoves.isNotEmpty()) {
                item {
                    MonoCard(elevated = true, contentPadding = PaddingValues(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Text(
                                text = "Solution (${flatMoves.size} moves)",
                                style = MaterialTheme.typography.titleMedium,
                                color = palette.textPrimary,
                                fontWeight = FontWeight.Bold,
                            )
                            Text(
                                text = "Step $stepIndex / ${flatMoves.size}",
                                style = TabularNumeralStyle.copy(fontSize = 14.sp),
                                color = palette.accent,
                            )
                        }
                        Spacer(Modifier.height(10.dp))
                        // Playback buttons
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly,
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            IconButton(
                                onClick = { autoPlaying = false; stepIndex = 0 },
                                enabled = stepIndex > 0,
                            ) { Icon(Icons.Outlined.Refresh, contentDescription = "Restart playback", tint = palette.textPrimary) }
                            IconButton(
                                onClick = { autoPlaying = false; if (stepIndex > 0) stepIndex-- },
                                enabled = stepIndex > 0,
                            ) { Icon(Icons.AutoMirrored.Outlined.NavigateBefore, contentDescription = "Previous move", tint = palette.textPrimary) }
                            IconButton(
                                onClick = {
                                    if (stepIndex >= flatMoves.size) stepIndex = 0
                                    autoPlaying = !autoPlaying
                                },
                            ) {
                                Icon(
                                    if (autoPlaying) Icons.Outlined.Pause else Icons.Outlined.PlayArrow,
                                    contentDescription = "Play/Pause",
                                    tint = palette.accent,
                                )
                            }
                            IconButton(
                                onClick = { autoPlaying = false; if (stepIndex < flatMoves.size) stepIndex++ },
                                enabled = stepIndex < flatMoves.size,
                            ) { Icon(Icons.AutoMirrored.Outlined.NavigateNext, contentDescription = "Next move", tint = palette.textPrimary) }
                        }

                        Spacer(Modifier.height(10.dp))
                        var runningCounter = 0
                        phases.forEach { phase ->
                            Text(
                                text = phase.phase,
                                style = MaterialTheme.typography.labelLarge,
                                color = palette.textSecondary,
                                fontWeight = FontWeight.SemiBold,
                            )
                            Spacer(Modifier.height(4.dp))
                            FlowRow(
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                verticalArrangement = Arrangement.spacedBy(6.dp),
                            ) {
                                phase.moves.forEach { move ->
                                    val moveIdx = ++runningCounter
                                    val isCurrent = moveIdx == stepIndex
                                    val isDone = moveIdx < stepIndex
                                    Surface(
                                        onClick = { autoPlaying = false; stepIndex = moveIdx },
                                        shape = RoundedCornerShape(8.dp),
                                        color = when {
                                            isCurrent -> palette.accent
                                            isDone -> palette.surfaceSecondary
                                            else -> palette.bgSecondary
                                        },
                                        border = BorderStroke(1.dp, palette.border),
                                    ) {
                                        Text(
                                            text = move,
                                            style = TabularNumeralStyle.copy(fontSize = 14.sp, fontWeight = FontWeight.Bold),
                                            color = if (isCurrent) palette.onAccent else palette.textPrimary,
                                            modifier = Modifier.padding(horizontal = 9.dp, vertical = 5.dp),
                                        )
                                    }
                                }
                            }
                            Spacer(Modifier.height(10.dp))
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun CubeFaceGrid(
    state: CubeState,
    face: Int,
    sizeDp: androidx.compose.ui.unit.Dp,
    onStickerTap: (Int) -> Unit,
) {
    val palette = MonoTheme.palette
    val faceLabels = arrayOf("U", "R", "F", "D", "L", "B")
    Column(
        modifier = Modifier.size(sizeDp),
        verticalArrangement = Arrangement.spacedBy(2.dp),
    ) {
        for (r in 0 until 3) {
            Row(
                modifier = Modifier.weight(1f).fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(2.dp),
            ) {
                for (c in 0 until 3) {
                    val idx = face * 9 + r * 3 + c
                    val colorIdx = state.stickers[idx].coerceIn(0, 5)
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxSize()
                            .clip(RoundedCornerShape(4.dp))
                            .background(cubeStickerColors[colorIdx])
                            .border(0.5.dp, palette.bgMain, RoundedCornerShape(4.dp))
                            .clickable { onStickerTap(idx) },
                        contentAlignment = Alignment.Center,
                    ) {
                        if (r == 1 && c == 1) {
                            Text(
                                text = faceLabels[face],
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (colorIdx == 0 || colorIdx == 3) Color.Black else Color.White,
                                textAlign = TextAlign.Center,
                            )
                        }
                    }
                }
            }
        }
    }
}
