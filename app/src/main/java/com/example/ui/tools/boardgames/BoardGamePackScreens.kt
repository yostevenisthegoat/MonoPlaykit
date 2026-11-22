package com.example.ui.tools.boardgames

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.Casino
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material.icons.outlined.Remove
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
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.MainViewModel
import com.example.ui.components.MonoCard
import com.example.ui.components.MonoChip
import com.example.ui.components.MonoPrimaryButton
import com.example.ui.components.MonoSecondaryButton
import com.example.ui.components.MonoToolTopBar
import com.example.ui.theme.MonoTheme
import com.example.ui.theme.TabularNumeralStyle
import kotlinx.coroutines.delay
import kotlin.random.Random

// -----------------------------------------------------------------------------
// Ludo Companion
// -----------------------------------------------------------------------------

@Composable
fun LudoScreen(
    viewModel: MainViewModel,
    isFavorite: Boolean,
    onBack: () -> Unit,
    onToggleFavorite: () -> Unit,
) {
    val palette = MonoTheme.palette
    val playerColors = listOf("Red", "Green", "Yellow", "Blue")
    var playerCount by remember { mutableIntStateOf(4) }
    var currentPlayer by remember { mutableIntStateOf(0) }
    var lastRoll by remember { mutableIntStateOf(6) }
    var consecutiveSixes by remember { mutableIntStateOf(0) }
    val homeTokens = remember { mutableStateListOf(0, 0, 0, 0) }
    val history = remember { mutableStateListOf<String>() }

    fun rollDice() {
        val roll = Random.nextInt(1, 7)
        lastRoll = roll
        val name = playerColors[currentPlayer]
        viewModel.feedbackAccent()
        if (roll == 6) {
            consecutiveSixes++
            if (consecutiveSixes >= 3) {
                history.add(0, "$name rolled 6 (3rd consecutive — turn forfeited!)")
                consecutiveSixes = 0
                currentPlayer = (currentPlayer + 1) % playerCount
            } else {
                history.add(0, "$name rolled 6 (Roll again!)")
            }
        } else {
            history.add(0, "$name rolled $roll")
            consecutiveSixes = 0
            currentPlayer = (currentPlayer + 1) % playerCount
        }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        MonoToolTopBar(
            title = "Ludo Companion",
            subtitle = "Turn tracker & 3-sixes rule",
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
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf(2, 3, 4).forEach { cnt ->
                        MonoChip("$cnt Players", playerCount == cnt, {
                            playerCount = cnt
                            currentPlayer = 0
                            consecutiveSixes = 0
                        })
                    }
                }
            }
            item {
                MonoCard(elevated = true, contentPadding = PaddingValues(22.dp)) {
                    Column(modifier = Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("CURRENT TURN: ${playerColors[currentPlayer].uppercase()}", style = MaterialTheme.typography.labelLarge, color = palette.accent)
                        Spacer(Modifier.height(10.dp))
                        Text("$lastRoll", style = TabularNumeralStyle.copy(fontSize = 64.sp, fontWeight = FontWeight.Bold), color = palette.textPrimary)
                        if (consecutiveSixes > 0) {
                            Text("6 rolled! Bonus roll active ($consecutiveSixes/3)", style = MaterialTheme.typography.labelMedium, color = palette.warning)
                        }
                        Spacer(Modifier.height(16.dp))
                        MonoPrimaryButton(
                            text = "Roll 1d6",
                            icon = Icons.Outlined.Casino,
                            onClick = { rollDice() },
                            modifier = Modifier.fillMaxWidth(),
                        )
                    }
                }
            }
            item {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    for (i in 0 until playerCount) {
                        val active = i == currentPlayer
                        MonoCard(
                            modifier = Modifier.weight(1f),
                            borderColor = if (active) palette.accent else palette.border,
                            onClick = { currentPlayer = i; consecutiveSixes = 0 },
                            contentPadding = PaddingValues(10.dp),
                        ) {
                            Text(playerColors[i], style = MaterialTheme.typography.labelMedium, color = palette.textSecondary)
                            Text("${homeTokens[i]}/4", style = TabularNumeralStyle.copy(fontSize = 20.sp, fontWeight = FontWeight.Bold), color = palette.textPrimary)
                            Row {
                                IconButton(onClick = { if (homeTokens[i] > 0) homeTokens[i]-- }, modifier = Modifier.size(28.dp)) {
                                    Icon(Icons.Outlined.Remove, contentDescription = "Less home", tint = palette.textMuted, modifier = Modifier.size(16.dp))
                                }
                                IconButton(onClick = { if (homeTokens[i] < 4) homeTokens[i]++ }, modifier = Modifier.size(28.dp)) {
                                    Icon(Icons.Outlined.Add, contentDescription = "Token home", tint = palette.textPrimary, modifier = Modifier.size(16.dp))
                                }
                            }
                        }
                    }
                }
            }
            items(history.take(20)) { entry ->
                MonoCard(contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp)) {
                    Text(entry, style = MaterialTheme.typography.bodyMedium, color = palette.textSecondary)
                }
            }
        }
    }
}

// -----------------------------------------------------------------------------
// Snakes & Ladders Companion
// -----------------------------------------------------------------------------

@Composable
fun SnakesLaddersScreen(
    viewModel: MainViewModel,
    isFavorite: Boolean,
    onBack: () -> Unit,
    onToggleFavorite: () -> Unit,
) {
    val palette = MonoTheme.palette
    var twoDice by remember { mutableStateOf(false) }
    var playerCount by remember { mutableIntStateOf(2) }
    var currentTurn by remember { mutableIntStateOf(0) }
    val positions = remember { mutableStateListOf(1, 1, 1, 1) }
    var lastDice by remember { mutableStateOf(listOf(4)) }
    val rollHistory = remember { mutableStateListOf<String>() }

    fun roll() {
        val rolls = if (twoDice) listOf(Random.nextInt(1, 7), Random.nextInt(1, 7)) else listOf(Random.nextInt(1, 7))
        lastDice = rolls
        val sum = rolls.sum()
        val prev = positions[currentTurn]
        val next = (prev + sum).coerceAtMost(100)
        positions[currentTurn] = next
        rollHistory.add(0, "P${currentTurn + 1} rolled ${rolls.joinToString("+")} ($sum) → Sq $next")
        currentTurn = (currentTurn + 1) % playerCount
        viewModel.feedbackAccent()
    }

    Column(modifier = Modifier.fillMaxSize()) {
        MonoToolTopBar(
            title = "Snakes & Ladders",
            subtitle = if (twoDice) "Two Dice Mode" else "One Dice Mode",
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
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        MonoChip("1 Die", !twoDice, { twoDice = false })
                        MonoChip("2 Dice", twoDice, { twoDice = true })
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        listOf(2, 3, 4).forEach { cnt ->
                            MonoChip("${cnt}P", playerCount == cnt, { playerCount = cnt; currentTurn = 0 })
                        }
                    }
                }
            }
            item {
                MonoCard(elevated = true, contentPadding = PaddingValues(22.dp)) {
                    Column(modifier = Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("PLAYER ${currentTurn + 1}'S ROLL", style = MaterialTheme.typography.labelLarge, color = palette.accent)
                        Spacer(Modifier.height(8.dp))
                        Text(
                            text = "${lastDice.sum()}",
                            style = TabularNumeralStyle.copy(fontSize = 62.sp, fontWeight = FontWeight.Bold),
                            color = palette.textPrimary,
                        )
                        if (twoDice && lastDice.size == 2) {
                            Text("[${lastDice[0]}] + [${lastDice[1]}]", style = TabularNumeralStyle, color = palette.textSecondary)
                        }
                        Spacer(Modifier.height(14.dp))
                        MonoPrimaryButton(
                            text = "Roll Dice",
                            icon = Icons.Outlined.Casino,
                            onClick = { roll() },
                            modifier = Modifier.fillMaxWidth(),
                        )
                    }
                }
            }
            item {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    for (i in 0 until playerCount) {
                        MonoCard(
                            modifier = Modifier.weight(1f),
                            borderColor = if (i == currentTurn) palette.accent else palette.border,
                            onClick = { currentTurn = i },
                            contentPadding = PaddingValues(10.dp),
                        ) {
                            Text("P${i + 1}", style = MaterialTheme.typography.labelMedium, color = palette.textSecondary)
                            Text("Sq ${positions[i]}", style = TabularNumeralStyle.copy(fontSize = 18.sp, fontWeight = FontWeight.Bold), color = palette.textPrimary)
                            Row {
                                IconButton(onClick = { positions[i] = (positions[i] - 1).coerceAtLeast(1) }, modifier = Modifier.size(28.dp)) {
                                    Icon(Icons.Outlined.Remove, contentDescription = "Back", tint = palette.textMuted, modifier = Modifier.size(16.dp))
                                }
                                IconButton(onClick = { positions[i] = (positions[i] + 1).coerceAtMost(100) }, modifier = Modifier.size(28.dp)) {
                                    Icon(Icons.Outlined.Add, contentDescription = "Forward", tint = palette.textPrimary, modifier = Modifier.size(16.dp))
                                }
                            }
                        }
                    }
                }
            }
            items(rollHistory.take(20)) { entry ->
                MonoCard(contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp)) {
                    Text(entry, style = MaterialTheme.typography.bodyMedium, color = palette.textSecondary)
                }
            }
        }
    }
}

// -----------------------------------------------------------------------------
// Dominoes Calculator
// -----------------------------------------------------------------------------

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun DominoesCalcScreen(
    viewModel: MainViewModel,
    isFavorite: Boolean,
    onBack: () -> Unit,
    onToggleFavorite: () -> Unit,
) {
    val palette = MonoTheme.palette
    val scores = remember { mutableStateListOf(0, 0) }
    var targetScore by remember { mutableIntStateOf(150) }
    var selectedPlayer by remember { mutableIntStateOf(0) }
    var pendingPips by remember { mutableIntStateOf(0) }
    val rounds = remember { mutableStateListOf<String>() }

    Column(modifier = Modifier.fillMaxSize()) {
        MonoToolTopBar(
            title = "Dominoes Calculator",
            subtitle = "Target: $targetScore pts",
            onBack = onBack,
            isFavorite = isFavorite,
            onToggleFavorite = onToggleFavorite,
            actions = {
                IconButton(onClick = { if (scores.size < 4) scores.add(0) }, enabled = scores.size < 4) {
                    Icon(Icons.Outlined.Add, contentDescription = "Add player", tint = palette.textPrimary)
                }
                IconButton(onClick = {
                    for (i in scores.indices) scores[i] = 0
                    pendingPips = 0
                    rounds.clear()
                }) {
                    Icon(Icons.Outlined.Refresh, contentDescription = "Reset", tint = palette.textPrimary)
                }
            },
        )
        LazyColumn(
            modifier = Modifier.weight(1f).navigationBarsPadding(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            item {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf(100, 150, 200).forEach { target ->
                        MonoChip("To $target", targetScore == target, { targetScore = target })
                    }
                }
            }
            item {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    scores.forEachIndexed { idx, s ->
                        val active = idx == selectedPlayer
                        MonoCard(
                            modifier = Modifier.weight(1f),
                            elevated = active,
                            borderColor = if (s >= targetScore) palette.success else if (active) palette.accent else palette.border,
                            onClick = { selectedPlayer = idx },
                            contentPadding = PaddingValues(12.dp),
                        ) {
                            Text("Player ${idx + 1}", style = MaterialTheme.typography.labelMedium, color = palette.textSecondary)
                            Text("$s", style = TabularNumeralStyle.copy(fontSize = 28.sp, fontWeight = FontWeight.Bold), color = palette.textPrimary)
                        }
                    }
                }
            }
            item {
                MonoCard(elevated = true, contentPadding = PaddingValues(16.dp)) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Text("Pip Sum Pad", style = MaterialTheme.typography.titleMedium, color = palette.textPrimary)
                        Text("+$pendingPips", style = TabularNumeralStyle.copy(fontSize = 24.sp, fontWeight = FontWeight.Bold), color = palette.accent)
                    }
                    Spacer(Modifier.height(10.dp))
                    FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        listOf(1, 2, 3, 4, 5, 6, 10, 12, 15, 20).forEach { pip ->
                            Surface(
                                onClick = { pendingPips += pip; viewModel.feedbackClick() },
                                shape = RoundedCornerShape(10.dp),
                                color = palette.surfaceSecondary,
                                border = BorderStroke(1.dp, palette.border),
                            ) {
                                Text("+$pip", style = TabularNumeralStyle.copy(fontWeight = FontWeight.Bold), color = palette.textPrimary, modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp))
                            }
                        }
                    }
                    Spacer(Modifier.height(12.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        MonoSecondaryButton("Clear", { pendingPips = 0 }, modifier = Modifier.weight(1f))
                        MonoPrimaryButton(
                            text = "Award to P${selectedPlayer + 1}",
                            enabled = pendingPips > 0,
                            onClick = {
                                scores[selectedPlayer] += pendingPips
                                rounds.add(0, "P${selectedPlayer + 1} +$pendingPips → ${scores[selectedPlayer]}")
                                pendingPips = 0
                                viewModel.feedbackAccent()
                            },
                            modifier = Modifier.weight(2f),
                        )
                    }
                }
            }
            items(rounds) { entry ->
                MonoCard(contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp)) {
                    Text(entry, style = MaterialTheme.typography.bodyMedium, color = palette.textSecondary)
                }
            }
        }
    }
}

// -----------------------------------------------------------------------------
// Bingo Caller
// -----------------------------------------------------------------------------

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun BingoCallerScreen(
    viewModel: MainViewModel,
    isFavorite: Boolean,
    onBack: () -> Unit,
    onToggleFavorite: () -> Unit,
) {
    val palette = MonoTheme.palette
    var maxBalls by remember { mutableIntStateOf(75) }
    val remainingPool = remember(maxBalls) { mutableStateListOf<Int>().apply { addAll((1..maxBalls).toList()) } }
    val calledBalls = remember { mutableStateListOf<Int>() }
    var autoCallSec by remember { mutableIntStateOf(0) } // 0 = manual, 5, 10

    fun formatBall(num: Int): String {
        if (maxBalls == 90) return "$num"
        val letter = when (num) {
            in 1..15 -> "B"
            in 16..30 -> "I"
            in 31..45 -> "N"
            in 46..60 -> "G"
            else -> "O"
        }
        return "$letter-$num"
    }

    fun callNext() {
        if (remainingPool.isEmpty()) {
            autoCallSec = 0
            return
        }
        val idx = Random.nextInt(remainingPool.size)
        val ball = remainingPool.removeAt(idx)
        calledBalls.add(0, ball)
        viewModel.feedbackAccent()
    }

    fun reset(newMax: Int = maxBalls) {
        autoCallSec = 0
        maxBalls = newMax
        remainingPool.clear()
        remainingPool.addAll(1..newMax)
        calledBalls.clear()
    }

    LaunchedEffect(autoCallSec, remainingPool.size) {
        if (autoCallSec > 0 && remainingPool.isNotEmpty()) {
            delay(autoCallSec * 1000L)
            callNext()
        }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        MonoToolTopBar(
            title = "Bingo Caller",
            subtitle = "${calledBalls.size} / $maxBalls called",
            onBack = onBack,
            isFavorite = isFavorite,
            onToggleFavorite = onToggleFavorite,
            actions = {
                IconButton(onClick = { reset() }) {
                    Icon(Icons.Outlined.Refresh, contentDescription = "Reset game", tint = palette.textPrimary)
                }
            },
        )
        LazyColumn(
            modifier = Modifier.weight(1f).navigationBarsPadding(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        MonoChip("75-Ball", maxBalls == 75, { reset(75) })
                        MonoChip("90-Ball", maxBalls == 90, { reset(90) })
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        MonoChip("Manual", autoCallSec == 0, { autoCallSec = 0 })
                        MonoChip("5s", autoCallSec == 5, { autoCallSec = 5 })
                        MonoChip("10s", autoCallSec == 10, { autoCallSec = 10 })
                    }
                }
            }
            item {
                MonoCard(elevated = true, contentPadding = PaddingValues(24.dp)) {
                    Column(modifier = Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("CURRENT CALL", style = MaterialTheme.typography.labelMedium, color = palette.textMuted)
                        Spacer(Modifier.height(8.dp))
                        Text(
                            text = calledBalls.firstOrNull()?.let(::formatBall) ?: "READY",
                            style = TabularNumeralStyle.copy(fontSize = 56.sp, fontWeight = FontWeight.Bold),
                            color = palette.textPrimary,
                        )
                        Spacer(Modifier.height(14.dp))
                        MonoPrimaryButton(
                            text = if (remainingPool.isEmpty()) "All Balls Called" else "Call Next Ball",
                            enabled = remainingPool.isNotEmpty(),
                            onClick = { callNext() },
                            modifier = Modifier.fillMaxWidth(),
                        )
                    }
                }
            }
            item {
                MonoCard(contentPadding = PaddingValues(12.dp)) {
                    Text("Called Board", style = MaterialTheme.typography.titleSmall, color = palette.textPrimary)
                    Spacer(Modifier.height(8.dp))
                    val calledSet = calledBalls.toSet()
                    FlowRow(horizontalArrangement = Arrangement.spacedBy(4.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        for (n in 1..maxBalls) {
                            val hit = calledSet.contains(n)
                            Box(
                                modifier = Modifier
                                    .size(30.dp)
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(if (hit) palette.accent else palette.bgSecondary)
                                    .border(0.5.dp, palette.border, RoundedCornerShape(6.dp)),
                                contentAlignment = Alignment.Center,
                            ) {
                                Text(
                                    text = "$n",
                                    style = TabularNumeralStyle.copy(fontSize = 11.sp, fontWeight = if (hit) FontWeight.Bold else FontWeight.Normal),
                                    color = if (hit) palette.onAccent else palette.textMuted,
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

// -----------------------------------------------------------------------------
// Jenga Random Block Selector
// -----------------------------------------------------------------------------

@Composable
fun JengaSelectorScreen(
    viewModel: MainViewModel,
    isFavorite: Boolean,
    onBack: () -> Unit,
    onToggleFavorite: () -> Unit,
) {
    val palette = MonoTheme.palette
    var maxText by remember { mutableStateOf("54") }
    var noRepeat by remember { mutableStateOf(false) }
    var currentBlock by remember { mutableStateOf<Int?>(null) }
    val picked = remember { mutableStateListOf<Int>() }

    val maxVal = (maxText.toIntOrNull() ?: 54).coerceIn(3, 500)

    fun drawBlock() {
        val pool = if (noRepeat) (1..maxVal).filterNot { picked.contains(it) } else (1..maxVal).toList()
        if (pool.isEmpty()) {
            picked.clear()
            return
        }
        val b = pool.random()
        currentBlock = b
        picked.add(0, b)
        viewModel.feedbackAccent()
    }

    Column(modifier = Modifier.fillMaxSize()) {
        MonoToolTopBar(
            title = "Jenga Block Selector",
            subtitle = "Random block & tower position",
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
                MonoCard(elevated = true, contentPadding = PaddingValues(24.dp)) {
                    Column(modifier = Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("PULL BLOCK", style = MaterialTheme.typography.labelMedium, color = palette.textMuted)
                        Spacer(Modifier.height(8.dp))
                        Text(
                            text = currentBlock?.let { "#$it" } ?: "—",
                            style = TabularNumeralStyle.copy(fontSize = 64.sp, fontWeight = FontWeight.Bold),
                            color = palette.textPrimary,
                        )
                        currentBlock?.let { num ->
                            val layer = ((num - 1) / 3) + 1
                            val slot = when ((num - 1) % 3) {
                                0 -> "Left"
                                1 -> "Center"
                                else -> "Right"
                            }
                            Text("Layer $layer • $slot", style = MaterialTheme.typography.titleMedium, color = palette.accent)
                        }
                        Spacer(Modifier.height(16.dp))
                        MonoPrimaryButton("Select Random Block", { drawBlock() }, modifier = Modifier.fillMaxWidth())
                    }
                }
            }
            item {
                MonoCard(contentPadding = PaddingValues(14.dp)) {
                    OutlinedTextField(
                        value = maxText,
                        onValueChange = { maxText = it.filter { c -> c.isDigit() }.take(3) },
                        label = { Text("Total Blocks (Default 54)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                    )
                    Spacer(Modifier.height(10.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text("No Repeats Until Reset", style = MaterialTheme.typography.titleSmall, color = palette.textPrimary)
                        Switch(checked = noRepeat, onCheckedChange = { noRepeat = it; picked.clear() })
                    }
                }
            }
        }
    }
}

// -----------------------------------------------------------------------------
// Tangram Shape Reference & Solution Guide
// -----------------------------------------------------------------------------

private data class TangramShape(
    val name: String,
    val difficulty: String,
    val polygons: List<List<Pair<Float, Float>>>, // normalized 0..1 coordinates
)

private val tangramShapes = listOf(
    TangramShape(
        name = "Classic Square",
        difficulty = "Starter",
        polygons = listOf(
            listOf(0f to 0f, 1f to 0f, 0.5f to 0.5f),             // Large Tri 1
            listOf(0f to 0f, 0.5f to 0.5f, 0f to 1f),             // Large Tri 2
            listOf(0.5f to 0.5f, 0.75f to 0.25f, 0.75f to 0.75f), // Small Tri 1
            listOf(1f to 0f, 1f to 0.5f, 0.75f to 0.25f),         // Wait -> Med Tri at bottom right:
            listOf(0.5f to 1f, 1f to 0.5f, 1f to 1f),             // Medium Tri
            listOf(0.5f to 0.5f, 0.75f to 0.25f, 1f to 0.5f, 0.75f to 0.75f), // Square
            listOf(0f to 1f, 0.25f to 0.75f, 0.75f to 0.75f, 0.5f to 1f),     // Parallelogram
        ),
    ),
    TangramShape(
        name = "House",
        difficulty = "Easy",
        polygons = listOf(
            listOf(0.1f to 0.45f, 0.5f to 0.1f, 0.9f to 0.45f),   // Roof
            listOf(0.2f to 0.45f, 0.8f to 0.45f, 0.5f to 0.72f),  // Upper wall
            listOf(0.2f to 0.45f, 0.5f to 0.72f, 0.2f to 0.92f),  // Left wall
            listOf(0.8f to 0.45f, 0.8f to 0.92f, 0.5f to 0.72f),  // Right wall
            listOf(0.2f to 0.92f, 0.5f to 0.72f, 0.8f to 0.92f),  // Base
            listOf(0.68f to 0.14f, 0.78f to 0.14f, 0.78f to 0.34f, 0.68f to 0.26f), // Chimney
        ),
    ),
    TangramShape(
        name = "Sailboat",
        difficulty = "Medium",
        polygons = listOf(
            listOf(0.5f to 0.08f, 0.5f to 0.62f, 0.18f to 0.62f), // Main sail left
            listOf(0.53f to 0.18f, 0.84f to 0.62f, 0.53f to 0.62f), // Jib sail right
            listOf(0.12f to 0.68f, 0.88f to 0.68f, 0.72f to 0.88f, 0.28f to 0.88f), // Hull
            listOf(0.32f to 0.68f, 0.5f to 0.68f, 0.5f to 0.88f), // Hull split
        ),
    ),
    TangramShape(
        name = "Swan",
        difficulty = "Medium",
        polygons = listOf(
            listOf(0.16f to 0.22f, 0.28f to 0.14f, 0.28f to 0.28f), // Head
            listOf(0.22f to 0.24f, 0.30f to 0.24f, 0.36f to 0.58f, 0.28f to 0.58f), // Neck
            listOf(0.28f to 0.58f, 0.64f to 0.58f, 0.46f to 0.84f), // Body 1
            listOf(0.46f to 0.84f, 0.64f to 0.58f, 0.78f to 0.84f), // Body 2
            listOf(0.64f to 0.58f, 0.86f to 0.38f, 0.78f to 0.84f), // Wing/Tail
        ),
    ),
    TangramShape(
        name = "Cat",
        difficulty = "Challenge",
        polygons = listOf(
            listOf(0.28f to 0.14f, 0.28f to 0.26f, 0.36f to 0.22f), // Left ear
            listOf(0.46f to 0.14f, 0.46f to 0.26f, 0.38f to 0.22f), // Right ear
            listOf(0.28f to 0.26f, 0.46f to 0.26f, 0.46f to 0.42f, 0.28f to 0.42f), // Head
            listOf(0.35f to 0.42f, 0.58f to 0.64f, 0.28f to 0.64f), // Chest
            listOf(0.28f to 0.64f, 0.58f to 0.64f, 0.58f to 0.88f), // Body 1
            listOf(0.28f to 0.64f, 0.58f to 0.88f, 0.28f to 0.88f), // Body 2
            listOf(0.58f to 0.88f, 0.74f to 0.74f, 0.84f to 0.78f, 0.68f to 0.90f), // Tail
        ),
    ),
)

@Composable
fun TangramGuideScreen(
    isFavorite: Boolean,
    onBack: () -> Unit,
    onToggleFavorite: () -> Unit,
) {
    val palette = MonoTheme.palette
    var selectedIdx by remember { mutableIntStateOf(0) }
    var showSolutionLines by remember { mutableStateOf(false) }
    val shape = tangramShapes[selectedIdx]

    Column(modifier = Modifier.fillMaxSize()) {
        MonoToolTopBar(
            title = "Tangram Guide",
            subtitle = "Silhouette & piece placement guide",
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
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(tangramShapes.indices.toList()) { idx ->
                        MonoChip(tangramShapes[idx].name, selectedIdx == idx, { selectedIdx = idx })
                    }
                }
            }
            item {
                MonoCard(elevated = true, contentPadding = PaddingValues(20.dp)) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Column {
                            Text(shape.name, style = MaterialTheme.typography.titleLarge, color = palette.textPrimary)
                            Text("Difficulty: ${shape.difficulty}", style = MaterialTheme.typography.labelMedium, color = palette.textSecondary)
                        }
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("Hints", style = MaterialTheme.typography.labelMedium, color = palette.textSecondary)
                            Switch(checked = showSolutionLines, onCheckedChange = { showSolutionLines = it })
                        }
                    }
                    Spacer(Modifier.height(14.dp))
                    Canvas(
                        modifier = Modifier
                            .fillMaxWidth()
                            .aspectRatio(1f)
                            .clip(RoundedCornerShape(16.dp))
                            .background(palette.bgSecondary)
                            .padding(16.dp)
                    ) {
                        val w = size.width
                        val h = size.height
                        shape.polygons.forEachIndexed { idx, pts ->
                            val path = Path().apply {
                                pts.forEachIndexed { i, (nx, ny) ->
                                    if (i == 0) moveTo(nx * w, ny * h) else lineTo(nx * w, ny * h)
                                }
                                close()
                            }
                            val fill = if (showSolutionLines) {
                                palette.accent.copy(alpha = 0.25f + (idx % 4) * 0.16f)
                            } else {
                                palette.accent
                            }
                            drawPath(path, color = fill)
                            if (showSolutionLines) {
                                drawPath(path, color = palette.bgMain, style = Stroke(width = 4f))
                            }
                        }
                    }
                }
            }
        }
    }
}
