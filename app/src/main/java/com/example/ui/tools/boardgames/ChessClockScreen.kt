package com.example.ui.tools.boardgames

import android.os.SystemClock
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Casino
import androidx.compose.material.icons.outlined.EditNote
import androidx.compose.material.icons.outlined.Fullscreen
import androidx.compose.material.icons.outlined.FullscreenExit
import androidx.compose.material.icons.outlined.Pause
import androidx.compose.material.icons.outlined.PlayArrow
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material.icons.outlined.Save
import androidx.compose.material.icons.outlined.Tune
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.UtilityIds
import com.example.ui.MainViewModel
import com.example.ui.components.KeepScreenOnEffect
import com.example.ui.components.MonoCard
import com.example.ui.components.MonoChip
import com.example.ui.components.MonoToolTopBar
import com.example.ui.theme.MonoTheme
import com.example.ui.theme.TabularNumeralStyle
import com.example.util.TimeFormatter
import kotlinx.coroutines.delay
import kotlin.random.Random

enum class TimingMode(val label: String) {
    INCREMENT("Fischer Increment"),
    DELAY("Simple Delay"),
}

data class ChessPreset(
    val id: String,
    val label: String,
    val category: String,
    val baseMs: Long,
    val bonusMs: Long,
    val mode: TimingMode = TimingMode.INCREMENT,
)

private val chessPresets = listOf(
    ChessPreset("bullet_1_0", "1+0 Bullet", "Bullet", 60_000L, 0L),
    ChessPreset("bullet_1_1", "1+1 Bullet", "Bullet", 60_000L, 1_000L),
    ChessPreset("bullet_2_1", "2+1 Bullet", "Bullet", 120_000L, 1_000L),
    ChessPreset("blitz_3_0", "3+0 Blitz", "Blitz", 180_000L, 0L),
    ChessPreset("blitz_3_2", "3+2 Blitz", "Blitz", 180_000L, 2_000L),
    ChessPreset("blitz_5_0", "5+0 Blitz", "Blitz", 300_000L, 0L),
    ChessPreset("rapid_10_0", "10+0 Rapid", "Rapid", 600_000L, 0L),
    ChessPreset("rapid_15_10", "15+10 Rapid", "Rapid", 900_000L, 10_000L),
    ChessPreset("classical_30_0", "30+0 Classical", "Classical", 1_800_000L, 0L),
    ChessPreset("classical_90_30", "90+30 Classical", "Classical", 5_400_000L, 30_000L),
)

@Composable
fun ChessClockScreen(
    viewModel: MainViewModel,
    isFavorite: Boolean,
    keepScreenAwake: Boolean,
    onBack: () -> Unit,
    onToggleFavorite: () -> Unit,
) {
    val palette = MonoTheme.palette
    val context = LocalContext.current
    KeepScreenOnEffect(keepScreenAwake)

    var currentPreset by remember { mutableStateOf(chessPresets[4]) } // 3+2 Blitz default
    var baseMs by remember { mutableLongStateOf(currentPreset.baseMs) }
    var bonusMs by remember { mutableLongStateOf(currentPreset.bonusMs) }
    var timingMode by remember { mutableStateOf(currentPreset.mode) }

    var topTimeMs by remember { mutableLongStateOf(baseMs) }
    var bottomTimeMs by remember { mutableLongStateOf(baseMs) }
    var topMoves by remember { mutableIntStateOf(0) }
    var bottomMoves by remember { mutableIntStateOf(0) }

    // 0 = idle, 1 = top running, 2 = bottom running
    var activePlayer by remember { mutableIntStateOf(0) }
    var paused by remember { mutableStateOf(false) }
    var flaggedPlayer by remember { mutableIntStateOf(0) } // 0 = none, 1 = top, 2 = bottom
    var remainingDelayMs by remember { mutableLongStateOf(0L) }
    var lastFrameMs by remember { mutableLongStateOf(0L) }
    var fullscreen by remember { mutableStateOf(false) }

    var showCustomDialog by remember { mutableStateOf(false) }
    var showNotationDialog by remember { mutableStateOf(false) }
    var show960Dialog by remember { mutableStateOf(false) }
    var chess960Id by remember { mutableIntStateOf(Random.nextInt(960)) }
    val recordedMoves = remember { mutableStateListOf<String>() }
    var moveDraft by remember { mutableStateOf("") }

    LaunchedEffect(activePlayer, paused, flaggedPlayer) {
        if (activePlayer != 0 && !paused && flaggedPlayer == 0) {
            lastFrameMs = SystemClock.elapsedRealtime()
            while (activePlayer != 0 && !paused && flaggedPlayer == 0) {
                delay(50L)
                val now = SystemClock.elapsedRealtime()
                var delta = now - lastFrameMs
                lastFrameMs = now

                if (timingMode == TimingMode.DELAY && remainingDelayMs > 0L) {
                    val consumed = minOf(delta, remainingDelayMs)
                    remainingDelayMs -= consumed
                    delta -= consumed
                }

                if (delta > 0L) {
                    if (activePlayer == 1) {
                        topTimeMs = (topTimeMs - delta).coerceAtLeast(0L)
                        if (topTimeMs == 0L) {
                            flaggedPlayer = 1
                            viewModel.feedbackAlert()
                        }
                    } else if (activePlayer == 2) {
                        bottomTimeMs = (bottomTimeMs - delta).coerceAtLeast(0L)
                        if (bottomTimeMs == 0L) {
                            flaggedPlayer = 2
                            viewModel.feedbackAlert()
                        }
                    }
                }
            }
        }
    }

    fun resetClock(newBase: Long = baseMs, newBonus: Long = bonusMs, newMode: TimingMode = timingMode) {
        baseMs = newBase
        bonusMs = newBonus
        timingMode = newMode
        topTimeMs = newBase
        bottomTimeMs = newBase
        topMoves = 0
        bottomMoves = 0
        activePlayer = 0
        paused = false
        flaggedPlayer = 0
        remainingDelayMs = 0L
        viewModel.feedbackClick()
    }

    fun onPlayerTap(player: Int) {
        if (flaggedPlayer != 0) return
        if (activePlayer == 0) {
            // First tap starts opponent's clock
            activePlayer = if (player == 1) 2 else 1
            paused = false
            remainingDelayMs = if (timingMode == TimingMode.DELAY) bonusMs else 0L
            viewModel.feedbackAccent()
            return
        }
        if (paused) {
            paused = false
            return
        }
        if (activePlayer != player) return

        // Current active player tapped -> add increment & switch
        if (timingMode == TimingMode.INCREMENT && bonusMs > 0L) {
            if (player == 1) topTimeMs += bonusMs else bottomTimeMs += bonusMs
        }
        if (player == 1) topMoves++ else bottomMoves++
        activePlayer = if (player == 1) 2 else 1
        remainingDelayMs = if (timingMode == TimingMode.DELAY) bonusMs else 0L
        viewModel.feedbackClick()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .then(if (fullscreen) Modifier.statusBarsPadding().navigationBarsPadding() else Modifier)
    ) {
        if (!fullscreen) {
            MonoToolTopBar(
                title = "Chess Clock",
                subtitle = "${currentPreset.label} • ${timingMode.label}",
                onBack = onBack,
                isFavorite = isFavorite,
                onToggleFavorite = onToggleFavorite,
                actions = {
                    IconButton(
                        onClick = { show960Dialog = true },
                        modifier = Modifier.minimumInteractiveComponentSize(),
                    ) {
                        Icon(Icons.Outlined.Casino, contentDescription = "Chess960 generator", tint = palette.textPrimary)
                    }
                    IconButton(
                        onClick = { showNotationDialog = true },
                        modifier = Modifier.minimumInteractiveComponentSize(),
                    ) {
                        Icon(Icons.Outlined.EditNote, contentDescription = "Move notation", tint = palette.textPrimary)
                    }
                    IconButton(
                        onClick = { fullscreen = true },
                        modifier = Modifier.minimumInteractiveComponentSize(),
                    ) {
                        Icon(Icons.Outlined.Fullscreen, contentDescription = "Fullscreen", tint = palette.textPrimary)
                    }
                },
            )

            LazyRow(
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                items(chessPresets) { preset ->
                    MonoChip(
                        label = preset.label,
                        selected = currentPreset.id == preset.id,
                        onClick = {
                            currentPreset = preset
                            resetClock(preset.baseMs, preset.bonusMs, preset.mode)
                        },
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
            // Top Player Panel (Rotated 180° for tabletop opponent)
            ChessClockPanel(
                label = "Black",
                timeMs = topTimeMs,
                moves = topMoves,
                isActive = activePlayer == 1 && !paused && flaggedPlayer == 0,
                isFlagged = flaggedPlayer == 1,
                delayMs = if (activePlayer == 1 && timingMode == TimingMode.DELAY) remainingDelayMs else 0L,
                rotated = true,
                onClick = { onPlayerTap(1) },
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .testTag("chess_clock_top"),
            )

            // Center Control Strip
            MonoCard(
                modifier = Modifier.fillMaxWidth(),
                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    IconButton(
                        onClick = { showCustomDialog = true },
                        modifier = Modifier.minimumInteractiveComponentSize(),
                    ) {
                        Icon(Icons.Outlined.Tune, contentDescription = "Custom time control", tint = palette.textPrimary)
                    }
                    IconButton(
                        onClick = {
                            if (activePlayer != 0) paused = !paused
                            viewModel.feedbackClick()
                        },
                        enabled = activePlayer != 0 && flaggedPlayer == 0,
                        modifier = Modifier.minimumInteractiveComponentSize().testTag("chess_pause_btn"),
                    ) {
                        Icon(
                            imageVector = if (paused) Icons.Outlined.PlayArrow else Icons.Outlined.Pause,
                            contentDescription = if (paused) "Resume" else "Pause",
                            tint = if (activePlayer != 0) palette.textPrimary else palette.textMuted,
                        )
                    }
                    IconButton(
                        onClick = { resetClock() },
                        modifier = Modifier.minimumInteractiveComponentSize().testTag("chess_reset_btn"),
                    ) {
                        Icon(Icons.Outlined.Refresh, contentDescription = "Reset clock", tint = palette.textPrimary)
                    }
                    IconButton(
                        onClick = {
                            viewModel.logHistory(
                                toolId = UtilityIds.CHESS_CLOCK,
                                title = "Chess (${currentPreset.label})",
                                summary = "Moves: White $bottomMoves / Black $topMoves • White ${TimeFormatter.formatClock(bottomTimeMs)} vs Black ${TimeFormatter.formatClock(topTimeMs)}",
                                detailText = if (recordedMoves.isNotEmpty()) recordedMoves.joinToString(" ") else "No notation recorded",
                                primaryValue = "${topMoves + bottomMoves} moves",
                            )
                            Toast.makeText(context, "Session saved to History", Toast.LENGTH_SHORT).show()
                        },
                        modifier = Modifier.minimumInteractiveComponentSize(),
                    ) {
                        Icon(Icons.Outlined.Save, contentDescription = "Save session", tint = palette.textPrimary)
                    }
                    if (fullscreen) {
                        IconButton(
                            onClick = { fullscreen = false },
                            modifier = Modifier.minimumInteractiveComponentSize(),
                        ) {
                            Icon(Icons.Outlined.FullscreenExit, contentDescription = "Exit fullscreen", tint = palette.textPrimary)
                        }
                    }
                }
            }

            // Bottom Player Panel
            ChessClockPanel(
                label = "White",
                timeMs = bottomTimeMs,
                moves = bottomMoves,
                isActive = activePlayer == 2 && !paused && flaggedPlayer == 0,
                isFlagged = flaggedPlayer == 2,
                delayMs = if (activePlayer == 2 && timingMode == TimingMode.DELAY) remainingDelayMs else 0L,
                rotated = false,
                onClick = { onPlayerTap(2) },
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .testTag("chess_clock_bottom"),
            )

            if (!fullscreen) {
                Spacer(Modifier.navigationBarsPadding())
            }
        }
    }

    // Custom Time Control Dialog
    if (showCustomDialog) {
        var mins by remember { mutableStateOf((baseMs / 60_000L).toString()) }
        var secs by remember { mutableStateOf(((baseMs % 60_000L) / 1000L).toString()) }
        var inc by remember { mutableStateOf((bonusMs / 1000L).toString()) }
        var mode by remember { mutableStateOf(timingMode) }

        AlertDialog(
            onDismissRequest = { showCustomDialog = false },
            containerColor = palette.cardElevated,
            titleContentColor = palette.textPrimary,
            title = { Text("Custom Time Control") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = mins,
                            onValueChange = { mins = it.filter { c -> c.isDigit() }.take(3) },
                            label = { Text("Minutes") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true,
                            modifier = Modifier.weight(1f),
                        )
                        OutlinedTextField(
                            value = secs,
                            onValueChange = { secs = it.filter { c -> c.isDigit() }.take(2) },
                            label = { Text("Seconds") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true,
                            modifier = Modifier.weight(1f),
                        )
                    }
                    OutlinedTextField(
                        value = inc,
                        onValueChange = { inc = it.filter { c -> c.isDigit() }.take(3) },
                        label = { Text("Bonus Seconds") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        TimingMode.entries.forEach { m ->
                            MonoChip(
                                label = if (m == TimingMode.INCREMENT) "Increment" else "Delay",
                                selected = mode == m,
                                onClick = { mode = m },
                            )
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    val m = (mins.toLongOrNull() ?: 5L).coerceAtLeast(0L)
                    val s = (secs.toLongOrNull() ?: 0L).coerceIn(0L, 59L)
                    val total = (m * 60_000L + s * 1_000L).coerceAtLeast(10_000L)
                    val bonus = (inc.toLongOrNull() ?: 0L) * 1_000L
                    currentPreset = ChessPreset("custom", "${m}m+${bonus / 1000}s", "Custom", total, bonus, mode)
                    resetClock(total, bonus, mode)
                    showCustomDialog = false
                }) { Text("Apply", color = palette.accent) }
            },
            dismissButton = {
                TextButton(onClick = { showCustomDialog = false }) { Text("Cancel", color = palette.textSecondary) }
            },
        )
    }

    // Move Notation Recorder Dialog
    if (showNotationDialog) {
        AlertDialog(
            onDismissRequest = { showNotationDialog = false },
            containerColor = palette.cardElevated,
            titleContentColor = palette.textPrimary,
            title = { Text("Move Notation Recorder") },
            text = {
                Column(
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.verticalScroll(rememberScrollState()),
                ) {
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        listOf("N", "B", "R", "Q", "K", "O-O").forEach { sym ->
                            Surface(
                                onClick = { moveDraft += sym },
                                shape = RoundedCornerShape(8.dp),
                                color = palette.surfaceSecondary,
                            ) {
                                Text(
                                    text = sym,
                                    color = palette.textPrimary,
                                    style = MaterialTheme.typography.labelLarge,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
                                )
                            }
                        }
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                        OutlinedTextField(
                            value = moveDraft,
                            onValueChange = { moveDraft = it },
                            label = { Text("Move (e.g. e4, Nf3)") },
                            singleLine = true,
                            modifier = Modifier.weight(1f),
                        )
                        TextButton(onClick = {
                            val clean = moveDraft.trim()
                            if (clean.isNotEmpty()) {
                                val ply = recordedMoves.size
                                val moveNum = (ply / 2) + 1
                                val formatted = if (ply % 2 == 0) "$moveNum. $clean" else clean
                                recordedMoves.add(formatted)
                                moveDraft = ""
                            }
                        }) { Text("Add", color = palette.accent) }
                    }
                    if (recordedMoves.isEmpty()) {
                        Text("No moves logged yet.", color = palette.textMuted, style = MaterialTheme.typography.bodyMedium)
                    } else {
                        Text(
                            text = recordedMoves.joinToString("  "),
                            color = palette.textPrimary,
                            style = TabularNumeralStyle.copy(fontSize = 14.sp),
                        )
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showNotationDialog = false }) { Text("Done", color = palette.accent) }
            },
            dismissButton = {
                if (recordedMoves.isNotEmpty()) {
                    TextButton(onClick = { recordedMoves.clear() }) { Text("Clear", color = palette.teamRed) }
                }
            },
        )
    }

    // Chess960 Position Generator Dialog
    if (show960Dialog) {
        val rank = remember(chess960Id) { Chess960Engine.generatePosition(chess960Id) }
        AlertDialog(
            onDismissRequest = { show960Dialog = false },
            containerColor = palette.cardElevated,
            titleContentColor = palette.textPrimary,
            title = { Text("Chess960 Position #$chess960Id") },
            text = {
                Column(
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Text(
                        text = "Official Fischer Random starting back rank (White a1–h1; Black mirrors on a8–h8).",
                        color = palette.textSecondary,
                        style = MaterialTheme.typography.bodyMedium,
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                    ) {
                        rank.forEachIndexed { idx, piece ->
                            val isLight = idx % 2 == 1
                            Box(
                                modifier = Modifier
                                    .size(34.dp)
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(if (isLight) palette.surfaceSecondary else palette.bgMain)
                                    .border(1.dp, palette.border, RoundedCornerShape(6.dp)),
                                contentAlignment = Alignment.Center,
                            ) {
                                Text(
                                    text = Chess960Engine.pieceSymbol(piece),
                                    fontSize = 20.sp,
                                    color = palette.textPrimary,
                                )
                            }
                        }
                    }
                    Text(
                        text = rank.toCharArray().joinToString(" "),
                        style = TabularNumeralStyle.copy(fontSize = 15.sp, fontWeight = FontWeight.Bold),
                        color = palette.accent,
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = { chess960Id = Random.nextInt(960) }) {
                    Text("New Random", color = palette.accent)
                }
            },
            dismissButton = {
                TextButton(onClick = { show960Dialog = false }) {
                    Text("Close", color = palette.textSecondary)
                }
            },
        )
    }
}

@Composable
private fun ChessClockPanel(
    label: String,
    timeMs: Long,
    moves: Int,
    isActive: Boolean,
    isFlagged: Boolean,
    delayMs: Long,
    rotated: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val palette = MonoTheme.palette
    val bg = when {
        isFlagged -> palette.teamRed.copy(alpha = 0.35f)
        isActive -> palette.accent
        else -> palette.cardPrimary
    }
    val fg = when {
        isFlagged -> Color.White
        isActive -> palette.onAccent
        else -> palette.textPrimary
    }
    val subFg = when {
        isFlagged -> Color.White.copy(alpha = 0.85f)
        isActive -> palette.onAccent.copy(alpha = 0.72f)
        else -> palette.textSecondary
    }

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(24.dp))
            .background(bg)
            .border(
                width = if (isActive) 2.dp else 1.dp,
                color = if (isFlagged) palette.teamRed else if (isActive) palette.accent else palette.border,
                shape = RoundedCornerShape(24.dp),
            )
            .clickable(onClick = onClick)
            .graphicsLayer { rotationZ = if (rotated) 180f else 0f }
            .padding(22.dp),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween,
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Text(
                    text = label.uppercase(),
                    style = MaterialTheme.typography.labelLarge,
                    color = subFg,
                    fontWeight = FontWeight.SemiBold,
                )
                Text(
                    text = "Moves: $moves",
                    style = MaterialTheme.typography.labelLarge,
                    color = subFg,
                )
            }

            Text(
                text = if (isFlagged) "TIME" else TimeFormatter.formatClock(timeMs),
                style = TabularNumeralStyle.copy(
                    fontSize = 62.sp,
                    fontWeight = FontWeight.Bold,
                ),
                color = fg,
                textAlign = TextAlign.Center,
            )

            Text(
                text = when {
                    isFlagged -> "Flag Fallen"
                    delayMs > 0L -> "Delay ${(delayMs + 999L) / 1000L}s"
                    isActive -> "Tap to end turn"
                    else -> "Waiting"
                },
                style = MaterialTheme.typography.labelMedium,
                color = subFg,
            )
        }
    }
}
