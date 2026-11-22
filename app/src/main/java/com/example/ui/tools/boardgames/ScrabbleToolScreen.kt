package com.example.ui.tools.boardgames

import android.os.SystemClock
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.Pause
import androidx.compose.material.icons.outlined.PlayArrow
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.MainViewModel
import com.example.ui.components.MonoCard
import com.example.ui.components.MonoChip
import com.example.ui.components.MonoPrimaryButton
import com.example.ui.components.MonoToolTopBar
import com.example.ui.theme.MonoTheme
import com.example.ui.theme.TabularNumeralStyle
import com.example.util.TimeFormatter
import kotlinx.coroutines.delay

private val scrabblePoints = mapOf(
    'A' to 1, 'B' to 3, 'C' to 3, 'D' to 2, 'E' to 1, 'F' to 4, 'G' to 2, 'H' to 4,
    'I' to 1, 'J' to 8, 'K' to 5, 'L' to 1, 'M' to 3, 'N' to 1, 'O' to 1, 'P' to 3,
    'Q' to 10, 'R' to 1, 'S' to 1, 'T' to 1, 'U' to 1, 'V' to 4, 'W' to 4, 'X' to 8,
    'Y' to 4, 'Z' to 10, '?' to 0,
)

private val officialTileBag = mapOf(
    'A' to 9, 'B' to 2, 'C' to 2, 'D' to 4, 'E' to 12, 'F' to 2, 'G' to 3, 'H' to 2,
    'I' to 9, 'J' to 1, 'K' to 1, 'L' to 4, 'M' to 2, 'N' to 6, 'O' to 8, 'P' to 2,
    'Q' to 1, 'R' to 6, 'S' to 4, 'T' to 6, 'U' to 4, 'V' to 2, 'W' to 2, 'X' to 1,
    'Y' to 2, 'Z' to 1, '?' to 2,
)

private data class ScrabblePlayer(val name: String, val score: Int)
private data class ScrabbleWordLog(val player: String, val word: String, val points: Int)

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ScrabbleToolScreen(
    viewModel: MainViewModel,
    isFavorite: Boolean,
    onBack: () -> Unit,
    onToggleFavorite: () -> Unit,
) {
    val palette = MonoTheme.palette
    val players = remember { mutableStateListOf(ScrabblePlayer("Player 1", 0), ScrabblePlayer("Player 2", 0)) }
    var activePlayerIdx by remember { mutableIntStateOf(0) }
    val wordLog = remember { mutableStateListOf<ScrabbleWordLog>() }

    var wordInput by remember { mutableStateOf("") }
    var letterMultipliers by remember { mutableStateOf<List<Int>>(emptyList()) } // 1, 2, 3
    var wordMultiplier by remember { mutableIntStateOf(1) } // 1, 2, 3
    var bingoBonus by remember { mutableStateOf(false) }

    // Turn Timer (default 90s)
    var turnDurationSec by remember { mutableIntStateOf(90) }
    var timeLeftMs by remember { mutableLongStateOf(90_000L) }
    var timerRunning by remember { mutableStateOf(false) }
    var lastTickMs by remember { mutableLongStateOf(0L) }

    LaunchedEffect(timerRunning) {
        if (timerRunning) {
            lastTickMs = SystemClock.elapsedRealtime()
            while (timerRunning) {
                delay(100L)
                val now = SystemClock.elapsedRealtime()
                val delta = now - lastTickMs
                lastTickMs = now
                timeLeftMs = (timeLeftMs - delta).coerceAtLeast(0L)
                if (timeLeftMs == 0L) {
                    timerRunning = false
                    viewModel.feedbackAlert()
                }
            }
        }
    }

    val cleanWord = wordInput.uppercase().filter { it in 'A'..'Z' || it == '?' }
    if (letterMultipliers.size != cleanWord.length) {
        letterMultipliers = List(cleanWord.length) { idx -> letterMultipliers.getOrElse(idx) { 1 } }
    }

    val wordPoints = remember(cleanWord, letterMultipliers, wordMultiplier, bingoBonus) {
        val base = cleanWord.mapIndexed { idx, ch ->
            (scrabblePoints[ch] ?: 0) * letterMultipliers.getOrElse(idx) { 1 }
        }.sum()
        base * wordMultiplier + (if (bingoBonus) 50 else 0)
    }

    // Tile distribution checker across logged words
    val remainingBag = remember(wordLog.size) {
        val used = mutableMapOf<Char, Int>()
        wordLog.forEach { log ->
            log.word.forEach { ch -> used[ch] = (used[ch] ?: 0) + 1 }
        }
        officialTileBag.mapValues { (ch, total) -> (total - (used[ch] ?: 0)).coerceAtLeast(0) }
    }

    val wordCheckMessage = remember(cleanWord, remainingBag) {
        when {
            cleanWord.isEmpty() -> "Enter A–Z letters (? for blank tile)"
            cleanWord.length < 2 -> "Standard words require 2+ letters"
            else -> {
                val needed = cleanWord.groupingBy { it }.eachCount()
                val shortages = needed.filter { (ch, cnt) -> cnt > (remainingBag[ch] ?: 0) }
                if (shortages.isEmpty()) "Valid letter format • Tiles available in remaining bag"
                else "Exceeds remaining bag count for: ${shortages.keys.joinToString()}"
            }
        }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        MonoToolTopBar(
            title = "Scrabble Utility",
            subtitle = "Word calculator, timer & 100-tile bag",
            onBack = onBack,
            isFavorite = isFavorite,
            onToggleFavorite = onToggleFavorite,
            actions = {
                IconButton(
                    onClick = {
                        if (players.size < 4) players.add(ScrabblePlayer("Player ${players.size + 1}", 0))
                    },
                    enabled = players.size < 4,
                ) {
                    Icon(Icons.Outlined.Add, contentDescription = "Add player", tint = palette.textPrimary)
                }
            },
        )

        LazyColumn(
            modifier = Modifier.weight(1f).navigationBarsPadding(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            // Players + Turn Timer
            item {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    players.forEachIndexed { idx, p ->
                        val active = idx == activePlayerIdx
                        MonoCard(
                            modifier = Modifier.weight(1f),
                            elevated = active,
                            borderColor = if (active) palette.accent else palette.border,
                            onClick = { activePlayerIdx = idx },
                            contentPadding = PaddingValues(12.dp),
                        ) {
                            Text(p.name, style = MaterialTheme.typography.labelMedium, color = palette.textSecondary, maxLines = 1)
                            Text("${p.score}", style = TabularNumeralStyle.copy(fontSize = 26.sp, fontWeight = FontWeight.Bold), color = palette.textPrimary)
                        }
                    }
                }
            }

            item {
                MonoCard(contentPadding = PaddingValues(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Column {
                            Text("Turn Timer", style = MaterialTheme.typography.labelSmall, color = palette.textMuted)
                            Text(
                                text = TimeFormatter.formatMinSec(timeLeftMs),
                                style = TabularNumeralStyle.copy(fontSize = 22.sp, fontWeight = FontWeight.Bold),
                                color = if (timeLeftMs <= 10_000L && timerRunning) palette.warning else palette.textPrimary,
                            )
                        }
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
                            listOf(60, 90, 120).forEach { sec ->
                                MonoChip("${sec}s", turnDurationSec == sec, {
                                    turnDurationSec = sec
                                    timerRunning = false
                                    timeLeftMs = sec * 1000L
                                })
                            }
                            IconButton(onClick = { timerRunning = !timerRunning }) {
                                Icon(if (timerRunning) Icons.Outlined.Pause else Icons.Outlined.PlayArrow, contentDescription = "Start/Pause", tint = palette.textPrimary)
                            }
                            IconButton(onClick = { timerRunning = false; timeLeftMs = turnDurationSec * 1000L }) {
                                Icon(Icons.Outlined.Refresh, contentDescription = "Reset timer", tint = palette.textSecondary)
                            }
                        }
                    }
                }
            }

            // Word Score Calculator
            item {
                MonoCard(elevated = true, contentPadding = PaddingValues(16.dp)) {
                    Text("Word Score Calculator", style = MaterialTheme.typography.titleMedium, color = palette.textPrimary)
                    Spacer(Modifier.height(8.dp))
                    OutlinedTextField(
                        value = wordInput,
                        onValueChange = { wordInput = it.take(15) },
                        label = { Text("Enter Word (? = Blank)") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                    )
                    Spacer(Modifier.height(6.dp))
                    Text(wordCheckMessage, style = MaterialTheme.typography.labelMedium, color = palette.textSecondary)

                    if (cleanWord.isNotEmpty()) {
                        Spacer(Modifier.height(10.dp))
                        Text("Tap tiles to cycle 1x / 2L / 3L:", style = MaterialTheme.typography.labelSmall, color = palette.textMuted)
                        Spacer(Modifier.height(6.dp))
                        FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            cleanWord.forEachIndexed { idx, ch ->
                                val mult = letterMultipliers.getOrElse(idx) { 1 }
                                val bg = when (mult) {
                                    2 -> palette.teamBlue.copy(alpha = 0.25f)
                                    3 -> palette.teamRed.copy(alpha = 0.25f)
                                    else -> palette.surfaceSecondary
                                }
                                Box(
                                    modifier = Modifier
                                        .size(44.dp)
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(bg)
                                        .border(1.dp, palette.border, RoundedCornerShape(10.dp))
                                        .clickable {
                                            val next = letterMultipliers.toMutableList()
                                            next[idx] = when (mult) { 1 -> 2; 2 -> 3; else -> 1 }
                                            letterMultipliers = next
                                        },
                                    contentAlignment = Alignment.Center,
                                ) {
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Text("$ch", style = MaterialTheme.typography.titleSmall, color = palette.textPrimary, fontWeight = FontWeight.Bold)
                                        Text(
                                            text = if (mult > 1) "${mult}L" else "${scrabblePoints[ch] ?: 0}",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = palette.textSecondary,
                                        )
                                    }
                                }
                            }
                        }
                    }

                    Spacer(Modifier.height(12.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            MonoChip("1W", wordMultiplier == 1, { wordMultiplier = 1 })
                            MonoChip("2W", wordMultiplier == 2, { wordMultiplier = 2 })
                            MonoChip("3W", wordMultiplier == 3, { wordMultiplier = 3 })
                        }
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("+50", style = MaterialTheme.typography.labelMedium, color = palette.textSecondary)
                            Switch(checked = bingoBonus, onCheckedChange = { bingoBonus = it })
                        }
                    }

                    Spacer(Modifier.height(10.dp))
                    MonoPrimaryButton(
                        text = "Add $wordPoints pts to ${players[activePlayerIdx].name}",
                        enabled = cleanWord.length >= 2,
                        onClick = {
                            val p = players[activePlayerIdx]
                            players[activePlayerIdx] = p.copy(score = p.score + wordPoints)
                            wordLog.add(0, ScrabbleWordLog(p.name, cleanWord, wordPoints))
                            wordInput = ""
                            wordMultiplier = 1
                            bingoBonus = false
                            activePlayerIdx = (activePlayerIdx + 1) % players.size
                            timerRunning = false
                            timeLeftMs = turnDurationSec * 1000L
                            viewModel.feedbackAccent()
                        },
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
            }

            // Remaining Tile Bag Summary
            item {
                val totalRem = remainingBag.values.sum()
                MonoCard(contentPadding = PaddingValues(14.dp)) {
                    Text("Remaining Tile Bag ($totalRem / 100)", style = MaterialTheme.typography.titleSmall, color = palette.textPrimary)
                    Spacer(Modifier.height(8.dp))
                    FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        remainingBag.forEach { (ch, count) ->
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = palette.surfaceSecondary,
                                border = BorderStroke(1.dp, palette.border),
                            ) {
                                Text(
                                    text = "$ch:$count",
                                    style = TabularNumeralStyle.copy(fontSize = 11.sp),
                                    color = if (count == 0) palette.textMuted else palette.textPrimary,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp),
                                )
                            }
                        }
                    }
                }
            }

            if (wordLog.isNotEmpty()) {
                item {
                    Text("Word History", style = MaterialTheme.typography.titleSmall, color = palette.textPrimary)
                }
                items(wordLog) { entry ->
                    MonoCard(contentPadding = PaddingValues(horizontal = 14.dp, vertical = 10.dp)) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("${entry.player}: ${entry.word}", style = MaterialTheme.typography.bodyMedium, color = palette.textPrimary)
                            Text("+${entry.points}", style = TabularNumeralStyle.copy(fontWeight = FontWeight.Bold), color = palette.accent)
                        }
                    }
                }
            }
        }
    }
}
