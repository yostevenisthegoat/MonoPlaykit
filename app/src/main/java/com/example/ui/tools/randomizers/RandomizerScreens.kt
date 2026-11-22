package com.example.ui.tools.randomizers

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.material.icons.outlined.Casino
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.DeleteOutline
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material.icons.outlined.Remove
import androidx.compose.material.icons.outlined.Shuffle
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.UtilityIds
import com.example.ui.MainViewModel
import com.example.ui.components.MonoCard
import com.example.ui.components.MonoChip
import com.example.ui.components.MonoPrimaryButton
import com.example.ui.components.MonoToolTopBar
import com.example.ui.theme.MonoTheme
import com.example.ui.theme.TabularNumeralStyle
import kotlinx.coroutines.launch
import kotlin.random.Random

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun DiceRollerScreen(
    viewModel: MainViewModel,
    isFavorite: Boolean,
    saveToHistory: Boolean,
    onBack: () -> Unit,
    onToggleFavorite: () -> Unit,
) {
    val palette = MonoTheme.palette
    val scope = rememberCoroutineScope()

    val dieTypes = remember { listOf(4, 6, 8, 10, 12, 20, 100) }
    var selectedSides by remember { mutableIntStateOf(6) }
    var useCustomSides by remember { mutableStateOf(false) }
    var customSidesText by remember { mutableStateOf("30") }
    var diceCount by remember { mutableIntStateOf(2) }
    var modifierValue by remember { mutableIntStateOf(0) }

    var currentRolls by remember { mutableStateOf(listOf(4, 6)) }
    val sessionHistory = remember { mutableStateListOf<String>() }
    val spinAnim = remember { Animatable(0f) }

    val activeSides = if (useCustomSides) (customSidesText.toIntOrNull() ?: 6).coerceIn(2, 1000) else selectedSides

    fun roll() {
        val results = List(diceCount) { Random.nextInt(1, activeSides + 1) }
        currentRolls = results
        val sum = results.sum() + modifierValue
        val modText = when {
            modifierValue > 0 -> "+$modifierValue"
            modifierValue < 0 -> "$modifierValue"
            else -> ""
        }
        val label = "${diceCount}d${activeSides}$modText"
        val entry = "$label → $sum  [${results.joinToString(", ")}]"
        sessionHistory.add(0, entry)
        if (sessionHistory.size > 30) sessionHistory.removeAt(sessionHistory.lastIndex)

        viewModel.feedbackAccent()
        if (saveToHistory) {
            viewModel.logHistory(
                toolId = UtilityIds.DICE_ROLLER,
                title = "Dice Roll ($label)",
                summary = "Results: ${results.joinToString(", ")}$modText",
                primaryValue = "$sum",
            )
        }
        scope.launch {
            spinAnim.snapTo(0f)
            spinAnim.animateTo(360f, tween(280, easing = FastOutSlowInEasing))
        }
    }

    val total = currentRolls.sum() + modifierValue

    Column(modifier = Modifier.fillMaxSize()) {
        MonoToolTopBar(
            title = "Dice Roller",
            subtitle = "${diceCount}d${activeSides}",
            onBack = onBack,
            isFavorite = isFavorite,
            onToggleFavorite = onToggleFavorite,
        )

        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .navigationBarsPadding(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            item {
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(dieTypes) { sides ->
                        MonoChip(
                            label = "D$sides",
                            selected = !useCustomSides && selectedSides == sides,
                            onClick = {
                                useCustomSides = false
                                selectedSides = sides
                                viewModel.feedbackClick()
                            },
                        )
                    }
                    item {
                        MonoChip(
                            label = "Custom",
                            selected = useCustomSides,
                            onClick = { useCustomSides = true },
                        )
                    }
                }
            }

            if (useCustomSides) {
                item {
                    OutlinedTextField(
                        value = customSidesText,
                        onValueChange = { customSidesText = it.filter { c -> c.isDigit() }.take(4) },
                        label = { Text("Custom Sides (2–1000)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
            }

            // Central Dice Display
            item {
                MonoCard(
                    elevated = true,
                    cornerRadius = 24.dp,
                    contentPadding = PaddingValues(22.dp),
                ) {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        Text(
                            text = "TOTAL",
                            style = MaterialTheme.typography.labelMedium,
                            color = palette.textMuted,
                        )
                        Text(
                            text = "$total",
                            style = TabularNumeralStyle.copy(fontSize = 68.sp, fontWeight = FontWeight.Bold),
                            color = palette.textPrimary,
                            modifier = Modifier.graphicsLayer { rotationZ = spinAnim.value * 0.05f },
                        )
                        Spacer(Modifier.height(14.dp))
                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp),
                            modifier = Modifier.fillMaxWidth(),
                        ) {
                            currentRolls.forEach { dieVal ->
                                Box(
                                    modifier = Modifier
                                        .size(54.dp)
                                        .graphicsLayer { rotationZ = spinAnim.value }
                                        .clip(RoundedCornerShape(14.dp))
                                        .background(palette.surfaceSecondary)
                                        .border(1.dp, palette.border, RoundedCornerShape(14.dp)),
                                    contentAlignment = Alignment.Center,
                                ) {
                                    Text(
                                        text = "$dieVal",
                                        style = TabularNumeralStyle.copy(fontSize = 20.sp, fontWeight = FontWeight.Bold),
                                        color = palette.textPrimary,
                                    )
                                }
                            }
                        }
                        Spacer(Modifier.height(18.dp))
                        MonoPrimaryButton(
                            text = "Roll ${diceCount}d${activeSides}",
                            onClick = { roll() },
                            icon = Icons.Outlined.Casino,
                            modifier = Modifier.fillMaxWidth().testTag("dice_roll_button"),
                        )
                    }
                }
            }

            // Quantity & Modifier
            item {
                MonoCard(contentPadding = PaddingValues(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Column {
                            Text("Dice Quantity", style = MaterialTheme.typography.titleSmall, color = palette.textPrimary)
                            Text("1 to 12 dice", style = MaterialTheme.typography.labelSmall, color = palette.textMuted)
                        }
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            IconButton(
                                onClick = { if (diceCount > 1) diceCount-- },
                                modifier = Modifier.minimumInteractiveComponentSize(),
                            ) { Icon(Icons.Outlined.Remove, contentDescription = "Fewer dice", tint = palette.textPrimary) }
                            Text(
                                text = "$diceCount",
                                style = TabularNumeralStyle.copy(fontSize = 18.sp, fontWeight = FontWeight.Bold),
                                color = palette.textPrimary,
                            )
                            IconButton(
                                onClick = { if (diceCount < 12) diceCount++ },
                                modifier = Modifier.minimumInteractiveComponentSize(),
                            ) { Icon(Icons.Outlined.Add, contentDescription = "More dice", tint = palette.textPrimary) }
                        }
                    }
                    Spacer(Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Column {
                            Text("Modifier", style = MaterialTheme.typography.titleSmall, color = palette.textPrimary)
                            Text("Added to combined total", style = MaterialTheme.typography.labelSmall, color = palette.textMuted)
                        }
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            IconButton(
                                onClick = { modifierValue-- },
                                modifier = Modifier.minimumInteractiveComponentSize(),
                            ) { Icon(Icons.Outlined.Remove, contentDescription = "Decrease modifier", tint = palette.textPrimary) }
                            Text(
                                text = if (modifierValue >= 0) "+$modifierValue" else "$modifierValue",
                                style = TabularNumeralStyle.copy(fontSize = 18.sp, fontWeight = FontWeight.Bold),
                                color = palette.textPrimary,
                            )
                            IconButton(
                                onClick = { modifierValue++ },
                                modifier = Modifier.minimumInteractiveComponentSize(),
                            ) { Icon(Icons.Outlined.Add, contentDescription = "Increase modifier", tint = palette.textPrimary) }
                        }
                    }
                }
            }

            if (sessionHistory.isNotEmpty()) {
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text("Roll History", style = MaterialTheme.typography.titleSmall, color = palette.textPrimary)
                        IconButton(onClick = { sessionHistory.clear() }) {
                            Icon(Icons.Outlined.DeleteOutline, contentDescription = "Clear roll history", tint = palette.textMuted)
                        }
                    }
                }
                items(sessionHistory) { entry ->
                    MonoCard(contentPadding = PaddingValues(horizontal = 14.dp, vertical = 10.dp)) {
                        Text(entry, style = TabularNumeralStyle.copy(fontSize = 13.sp), color = palette.textSecondary)
                    }
                }
            }
        }
    }
}

@Composable
fun CoinFlipScreen(
    viewModel: MainViewModel,
    isFavorite: Boolean,
    onBack: () -> Unit,
    onToggleFavorite: () -> Unit,
) {
    val palette = MonoTheme.palette
    val scope = rememberCoroutineScope()
    var result by remember { mutableStateOf("HEADS") }
    var headsCount by remember { mutableIntStateOf(0) }
    var tailsCount by remember { mutableIntStateOf(0) }
    var streak by remember { mutableIntStateOf(0) }
    var lastSide by remember { mutableStateOf("") }
    val flipRotation = remember { Animatable(0f) }

    fun flip() {
        val next = if (Random.nextBoolean()) "HEADS" else "TAILS"
        result = next
        if (next == "HEADS") headsCount++ else tailsCount++
        streak = if (lastSide == next) streak + 1 else 1
        lastSide = next
        viewModel.feedbackAccent()
        scope.launch {
            flipRotation.snapTo(0f)
            flipRotation.animateTo(720f, tween(450, easing = FastOutSlowInEasing))
        }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        MonoToolTopBar(
            title = "Coin Flip",
            subtitle = "Unbiased 50/50 randomizer",
            onBack = onBack,
            isFavorite = isFavorite,
            onToggleFavorite = onToggleFavorite,
        )
        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween,
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                MonoCard(modifier = Modifier.weight(1f), contentPadding = PaddingValues(14.dp)) {
                    Text("HEADS", style = MaterialTheme.typography.labelSmall, color = palette.textMuted)
                    Text("$headsCount", style = TabularNumeralStyle.copy(fontSize = 26.sp, fontWeight = FontWeight.Bold), color = palette.textPrimary)
                }
                MonoCard(modifier = Modifier.weight(1f), contentPadding = PaddingValues(14.dp)) {
                    Text("TAILS", style = MaterialTheme.typography.labelSmall, color = palette.textMuted)
                    Text("$tailsCount", style = TabularNumeralStyle.copy(fontSize = 26.sp, fontWeight = FontWeight.Bold), color = palette.textPrimary)
                }
                MonoCard(modifier = Modifier.weight(1f), contentPadding = PaddingValues(14.dp)) {
                    Text("STREAK", style = MaterialTheme.typography.labelSmall, color = palette.textMuted)
                    Text("${streak}x", style = TabularNumeralStyle.copy(fontSize = 26.sp, fontWeight = FontWeight.Bold), color = palette.accent)
                }
            }

            Box(
                modifier = Modifier
                    .size(210.dp)
                    .graphicsLayer {
                        rotationX = flipRotation.value
                        cameraDistance = 12f * density
                    }
                    .clip(CircleShape)
                    .background(if (result == "HEADS") palette.accent else palette.cardElevated)
                    .border(3.dp, palette.border, CircleShape),
                contentAlignment = Alignment.Center,
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = if (result == "HEADS") "H" else "T",
                        style = TabularNumeralStyle.copy(fontSize = 64.sp, fontWeight = FontWeight.Bold),
                        color = if (result == "HEADS") palette.onAccent else palette.textPrimary,
                    )
                    Text(
                        text = result,
                        style = MaterialTheme.typography.labelLarge,
                        color = if (result == "HEADS") palette.onAccent else palette.textSecondary,
                        fontWeight = FontWeight.SemiBold,
                    )
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                Surface(
                    onClick = {
                        headsCount = 0; tailsCount = 0; streak = 0; lastSide = ""
                    },
                    shape = RoundedCornerShape(14.dp),
                    color = palette.cardPrimary,
                    border = BorderStroke(1.dp, palette.border),
                    modifier = Modifier.minimumInteractiveComponentSize(),
                ) {
                    Box(modifier = Modifier.padding(14.dp), contentAlignment = Alignment.Center) {
                        Icon(Icons.Outlined.Refresh, contentDescription = "Reset stats", tint = palette.textPrimary)
                    }
                }
                MonoPrimaryButton(
                    text = "Flip Coin",
                    onClick = { flip() },
                    modifier = Modifier.weight(1f).testTag("coin_flip_button"),
                )
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun RngScreen(
    viewModel: MainViewModel,
    isFavorite: Boolean,
    onBack: () -> Unit,
    onToggleFavorite: () -> Unit,
) {
    val palette = MonoTheme.palette
    var minText by remember { mutableStateOf("1") }
    var maxText by remember { mutableStateOf("100") }
    var count by remember { mutableIntStateOf(1) }
    var uniqueOnly by remember { mutableStateOf(true) }
    var results by remember { mutableStateOf(listOf(42)) }
    var errorMsg by remember { mutableStateOf<String?>(null) }

    fun generate() {
        val min = minText.toIntOrNull()
        val max = maxText.toIntOrNull()
        if (min == null || max == null || min > max) {
            errorMsg = "Enter a valid range where Min ≤ Max."
            return
        }
        val span = max.toLong() - min.toLong() + 1L
        if (uniqueOnly && count.toLong() > span) {
            errorMsg = "Range only has $span unique numbers."
            return
        }
        errorMsg = null
        results = if (uniqueOnly) {
            val picked = mutableSetOf<Int>()
            while (picked.size < count) {
                picked.add(Random.nextInt(min, max + 1))
            }
            picked.toList()
        } else {
            List(count) { Random.nextInt(min, max + 1) }
        }
        viewModel.feedbackAccent()
    }

    Column(modifier = Modifier.fillMaxSize()) {
        MonoToolTopBar(
            title = "Random Number",
            subtitle = "Custom range generator",
            onBack = onBack,
            isFavorite = isFavorite,
            onToggleFavorite = onToggleFavorite,
        )
        LazyColumn(
            modifier = Modifier.weight(1f).navigationBarsPadding(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            item {
                MonoCard(elevated = true, cornerRadius = 22.dp, contentPadding = PaddingValues(24.dp)) {
                    Column(modifier = Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("GENERATED", style = MaterialTheme.typography.labelMedium, color = palette.textMuted)
                        Spacer(Modifier.height(10.dp))
                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp),
                        ) {
                            results.forEach { n ->
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(14.dp))
                                        .background(palette.surfaceSecondary)
                                        .border(1.dp, palette.border, RoundedCornerShape(14.dp))
                                        .padding(horizontal = 18.dp, vertical = 12.dp),
                                ) {
                                    Text(
                                        text = "$n",
                                        style = TabularNumeralStyle.copy(fontSize = 32.sp, fontWeight = FontWeight.Bold),
                                        color = palette.textPrimary,
                                    )
                                }
                            }
                        }
                        Spacer(Modifier.height(18.dp))
                        MonoPrimaryButton(
                            text = "Generate",
                            onClick = { generate() },
                            modifier = Modifier.fillMaxWidth().testTag("rng_generate_button"),
                        )
                    }
                }
            }

            item {
                MonoCard(contentPadding = PaddingValues(16.dp)) {
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        OutlinedTextField(
                            value = minText,
                            onValueChange = { minText = it.filter { c -> c.isDigit() || c == '-' }.take(7) },
                            label = { Text("Min") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true,
                            modifier = Modifier.weight(1f),
                        )
                        OutlinedTextField(
                            value = maxText,
                            onValueChange = { maxText = it.filter { c -> c.isDigit() || c == '-' }.take(7) },
                            label = { Text("Max") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true,
                            modifier = Modifier.weight(1f),
                        )
                    }
                    Spacer(Modifier.height(12.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text("Quantity: $count", style = MaterialTheme.typography.titleSmall, color = palette.textPrimary)
                        Row {
                            IconButton(onClick = { if (count > 1) count-- }) { Icon(Icons.Outlined.Remove, contentDescription = "Less", tint = palette.textPrimary) }
                            IconButton(onClick = { if (count < 20) count++ }) { Icon(Icons.Outlined.Add, contentDescription = "More", tint = palette.textPrimary) }
                        }
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text("No duplicates", style = MaterialTheme.typography.titleSmall, color = palette.textPrimary)
                        Switch(checked = uniqueOnly, onCheckedChange = { uniqueOnly = it })
                    }
                    errorMsg?.let {
                        Spacer(Modifier.height(6.dp))
                        Text(it, color = palette.teamRed, style = MaterialTheme.typography.bodyMedium)
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun PlayerTeamRandomizerScreen(
    viewModel: MainViewModel,
    isFavorite: Boolean,
    onBack: () -> Unit,
    onToggleFavorite: () -> Unit,
) {
    val palette = MonoTheme.palette
    val players = remember { mutableStateListOf("Alex", "Jordan", "Sam", "Taylor") }
    var newPlayer by remember { mutableStateOf("") }
    var mode by remember { mutableIntStateOf(0) } // 0 = Pick 1, 1 = Turn Order, 2 = Teams
    var teamCount by remember { mutableIntStateOf(2) }
    var pickedPlayer by remember { mutableStateOf<String?>(null) }
    var turnOrder by remember { mutableStateOf<List<String>>(emptyList()) }
    var teams by remember { mutableStateOf<List<List<String>>>(emptyList()) }

    fun runRandomizer() {
        if (players.isEmpty()) return
        viewModel.feedbackAccent()
        when (mode) {
            0 -> pickedPlayer = players.random()
            1 -> turnOrder = players.shuffled()
            2 -> {
                val shuffled = players.shuffled()
                val buckets = List(teamCount) { mutableListOf<String>() }
                shuffled.forEachIndexed { idx, name -> buckets[idx % teamCount].add(name) }
                teams = buckets
            }
        }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        MonoToolTopBar(
            title = "Player, Turn & Team Picker",
            subtitle = "${players.size} players in roster",
            onBack = onBack,
            isFavorite = isFavorite,
            onToggleFavorite = onToggleFavorite,
        )

        LazyColumn(
            modifier = Modifier.weight(1f).navigationBarsPadding(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            item {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    MonoChip("Pick Player", mode == 0, { mode = 0 })
                    MonoChip("Turn Order", mode == 1, { mode = 1 })
                    MonoChip("Teams", mode == 2, { mode = 2 })
                }
            }

            item {
                MonoCard(contentPadding = PaddingValues(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        OutlinedTextField(
                            value = newPlayer,
                            onValueChange = { newPlayer = it },
                            label = { Text("Add player name") },
                            singleLine = true,
                            modifier = Modifier.weight(1f),
                        )
                        MonoPrimaryButton(
                            text = "Add",
                            onClick = {
                                val clean = newPlayer.trim()
                                if (clean.isNotEmpty() && !players.contains(clean)) {
                                    players.add(clean)
                                    newPlayer = ""
                                    viewModel.feedbackClick()
                                }
                            },
                        )
                    }
                    Spacer(Modifier.height(12.dp))
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        players.forEach { name ->
                            Surface(
                                shape = RoundedCornerShape(50),
                                color = palette.surfaceSecondary,
                                border = BorderStroke(1.dp, palette.border),
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.padding(start = 12.dp, end = 4.dp, top = 4.dp, bottom = 4.dp),
                                ) {
                                    Text(name, color = palette.textPrimary, style = MaterialTheme.typography.labelLarge)
                                    IconButton(
                                        onClick = { players.remove(name) },
                                        modifier = Modifier.size(28.dp),
                                    ) {
                                        Icon(Icons.Outlined.Close, contentDescription = "Remove $name", tint = palette.textMuted, modifier = Modifier.size(16.dp))
                                    }
                                }
                            }
                        }
                    }
                    if (mode == 2) {
                        Spacer(Modifier.height(10.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Text("Number of Teams: $teamCount", style = MaterialTheme.typography.titleSmall, color = palette.textPrimary)
                            Row {
                                IconButton(onClick = { if (teamCount > 2) teamCount-- }) { Icon(Icons.Outlined.Remove, contentDescription = "Less teams", tint = palette.textPrimary) }
                                IconButton(onClick = { if (teamCount < 6) teamCount++ }) { Icon(Icons.Outlined.Add, contentDescription = "More teams", tint = palette.textPrimary) }
                            }
                        }
                    }
                }
            }

            item {
                MonoPrimaryButton(
                    text = when (mode) {
                        0 -> "Pick Random Player"
                        1 -> "Shuffle Turn Order"
                        else -> "Generate $teamCount Teams"
                    },
                    onClick = { runRandomizer() },
                    icon = Icons.Outlined.Shuffle,
                    enabled = players.isNotEmpty(),
                    modifier = Modifier.fillMaxWidth(),
                )
            }

            if (mode == 0 && pickedPlayer != null) {
                item {
                    MonoCard(elevated = true, contentPadding = PaddingValues(24.dp)) {
                        Column(modifier = Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("SELECTED PLAYER", style = MaterialTheme.typography.labelMedium, color = palette.textMuted)
                            Spacer(Modifier.height(8.dp))
                            Text(pickedPlayer!!, style = MaterialTheme.typography.headlineLarge, color = palette.textPrimary, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            if (mode == 1 && turnOrder.isNotEmpty()) {
                item {
                    MonoCard(elevated = true, contentPadding = PaddingValues(16.dp)) {
                        Text("TURN ORDER", style = MaterialTheme.typography.labelMedium, color = palette.textMuted)
                        Spacer(Modifier.height(10.dp))
                        turnOrder.forEachIndexed { idx, name ->
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Text(
                                    text = "#${idx + 1}",
                                    style = TabularNumeralStyle.copy(fontWeight = FontWeight.Bold),
                                    color = palette.accent,
                                    modifier = Modifier.width(36.dp),
                                )
                                Text(name, style = MaterialTheme.typography.titleMedium, color = palette.textPrimary)
                            }
                        }
                    }
                }
            }

            if (mode == 2 && teams.isNotEmpty()) {
                items(teams.withIndex().toList()) { (idx, members) ->
                    MonoCard(elevated = true, contentPadding = PaddingValues(16.dp)) {
                        Text("Team ${idx + 1}", style = MaterialTheme.typography.titleMedium, color = palette.accent, fontWeight = FontWeight.Bold)
                        Spacer(Modifier.height(6.dp))
                        Text(
                            text = if (members.isEmpty()) "—" else members.joinToString(" • "),
                            style = MaterialTheme.typography.bodyLarge,
                            color = palette.textPrimary,
                        )
                    }
                }
            }
        }
    }
}
