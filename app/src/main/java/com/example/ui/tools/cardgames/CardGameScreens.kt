package com.example.ui.tools.cardgames

import android.widget.Toast
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.Casino
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material.icons.outlined.Remove
import androidx.compose.material.icons.outlined.Save
import androidx.compose.material.icons.outlined.Shuffle
import androidx.compose.material.icons.outlined.Style
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.UtilityIds
import com.example.ui.MainViewModel
import com.example.ui.components.MonoCard
import com.example.ui.components.MonoChip
import com.example.ui.components.MonoPrimaryButton
import com.example.ui.components.MonoSecondaryButton
import com.example.ui.components.MonoToolTopBar
import com.example.ui.theme.MonoTheme
import com.example.ui.theme.TabularNumeralStyle

private data class TcgPlayer(
    val name: String,
    val life: Int,
    val damage: Int = 0,
    val energy: Int = 0,
    val poison: Int = 0,
    val statuses: Set<String> = emptySet(),
)

private val availableStatuses = listOf("Poisoned", "Burned", "Stunned", "Shielded", "Hexproof")

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun TcgCompanionScreen(
    viewModel: MainViewModel,
    isFavorite: Boolean,
    onBack: () -> Unit,
    onToggleFavorite: () -> Unit,
) {
    val palette = MonoTheme.palette
    val context = LocalContext.current

    var startingLife by remember { mutableIntStateOf(20) }
    var turnNumber by remember { mutableIntStateOf(1) }
    val players = remember {
        mutableStateListOf(
            TcgPlayer("Player 1", 20),
            TcgPlayer("Player 2", 20),
        )
    }

    fun resetGame(life: Int = startingLife) {
        startingLife = life
        turnNumber = 1
        for (i in players.indices) {
            players[i] = TcgPlayer(players[i].name, life)
        }
        viewModel.feedbackClick()
    }

    Column(modifier = Modifier.fillMaxSize()) {
        MonoToolTopBar(
            title = "TCG Life & Counters",
            subtitle = "Turn $turnNumber • Start $startingLife LP",
            onBack = onBack,
            isFavorite = isFavorite,
            onToggleFavorite = onToggleFavorite,
            actions = {
                IconButton(
                    onClick = {
                        viewModel.logHistory(
                            toolId = UtilityIds.TCG_COMPANION,
                            title = "TCG Match (Turn $turnNumber)",
                            summary = players.joinToString(" | ") { "${it.name}: ${it.life} LP" },
                            primaryValue = "Turn $turnNumber",
                        )
                        Toast.makeText(context, "Session saved to History", Toast.LENGTH_SHORT).show()
                    }
                ) {
                    Icon(Icons.Outlined.Save, contentDescription = "Save TCG match", tint = palette.textPrimary)
                }
                IconButton(onClick = { resetGame() }) {
                    Icon(Icons.Outlined.Refresh, contentDescription = "Reset match", tint = palette.textPrimary)
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
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    LazyRow(
                        modifier = Modifier.weight(1f),
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                    ) {
                        items(listOf(20 to "20 MTG", 40 to "40 EDH", 8000 to "8000 YGO", 10 to "10 Prize")) { (lp, label) ->
                            MonoChip(label, startingLife == lp, { resetGame(lp) })
                        }
                    }
                    Spacer(Modifier.width(8.dp))
                    Surface(
                        onClick = { turnNumber++; viewModel.feedbackAccent() },
                        shape = RoundedCornerShape(12.dp),
                        color = palette.accent,
                    ) {
                        Text(
                            text = "Turn $turnNumber +",
                            style = MaterialTheme.typography.labelLarge,
                            color = palette.onAccent,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                        )
                    }
                }
            }

            item {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    MonoSecondaryButton(
                        text = "Add Player",
                        icon = Icons.Outlined.Add,
                        enabled = players.size < 4,
                        onClick = { players.add(TcgPlayer("Player ${players.size + 1}", startingLife)) },
                        modifier = Modifier.weight(1f),
                    )
                    MonoSecondaryButton(
                        text = "Remove",
                        icon = Icons.Outlined.Remove,
                        enabled = players.size > 1,
                        onClick = { players.removeAt(players.lastIndex) },
                        modifier = Modifier.weight(1f),
                    )
                }
            }

            items(players.indices.toList()) { idx ->
                val p = players[idx]
                val stepBig = if (startingLife >= 1000) 500 else 5
                val stepSmall = if (startingLife >= 1000) 100 else 1

                MonoCard(elevated = true, contentPadding = PaddingValues(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(p.name, style = MaterialTheme.typography.titleMedium, color = palette.textPrimary)
                        Text(
                            text = "${p.life}",
                            style = TabularNumeralStyle.copy(fontSize = 40.sp, fontWeight = FontWeight.Bold),
                            color = if (p.life <= 0) palette.teamRed else palette.textPrimary,
                        )
                    }
                    Spacer(Modifier.height(10.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        listOf(-stepBig, -stepSmall, stepSmall, stepBig).forEach { delta ->
                            Surface(
                                onClick = {
                                    players[idx] = p.copy(life = p.life + delta)
                                    viewModel.feedbackClick()
                                },
                                shape = RoundedCornerShape(10.dp),
                                color = palette.surfaceSecondary,
                                border = BorderStroke(1.dp, palette.border),
                                modifier = Modifier.weight(1f).minimumInteractiveComponentSize(),
                            ) {
                                Box(contentAlignment = Alignment.Center, modifier = Modifier.padding(vertical = 10.dp)) {
                                    Text(
                                        text = if (delta > 0) "+$delta" else "$delta",
                                        style = TabularNumeralStyle.copy(fontWeight = FontWeight.Bold),
                                        color = if (delta > 0) palette.success else palette.teamRed,
                                    )
                                }
                            }
                        }
                    }

                    Spacer(Modifier.height(12.dp))
                    // Sub-counters: Damage, Energy, Poison
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        MiniCounterPill("Dmg", p.damage, Modifier.weight(1f)) { d ->
                            players[idx] = p.copy(damage = (p.damage + d).coerceAtLeast(0))
                        }
                        MiniCounterPill("Energy", p.energy, Modifier.weight(1f)) { d ->
                            players[idx] = p.copy(energy = (p.energy + d).coerceAtLeast(0))
                        }
                        MiniCounterPill("Poison", p.poison, Modifier.weight(1f)) { d ->
                            players[idx] = p.copy(poison = (p.poison + d).coerceAtLeast(0))
                        }
                    }

                    Spacer(Modifier.height(10.dp))
                    FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        availableStatuses.forEach { status ->
                            val has = p.statuses.contains(status)
                            MonoChip(status, has, {
                                val next = p.statuses.toMutableSet()
                                if (has) next.remove(status) else next.add(status)
                                players[idx] = p.copy(statuses = next)
                            })
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun MiniCounterPill(
    label: String,
    value: Int,
    modifier: Modifier = Modifier,
    onDelta: (Int) -> Unit,
) {
    val palette = MonoTheme.palette
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(10.dp))
            .background(palette.bgSecondary)
            .border(1.dp, palette.border, RoundedCornerShape(10.dp))
            .padding(horizontal = 6.dp, vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        IconButton(onClick = { onDelta(-1) }, modifier = Modifier.size(26.dp)) {
            Icon(Icons.Outlined.Remove, contentDescription = "Decrease $label", tint = palette.textMuted, modifier = Modifier.size(14.dp))
        }
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(label, style = MaterialTheme.typography.labelSmall, color = palette.textMuted)
            Text("$value", style = TabularNumeralStyle.copy(fontWeight = FontWeight.Bold), color = palette.textPrimary)
        }
        IconButton(onClick = { onDelta(1) }, modifier = Modifier.size(26.dp)) {
            Icon(Icons.Outlined.Add, contentDescription = "Increase $label", tint = palette.textPrimary, modifier = Modifier.size(14.dp))
        }
    }
}

// -----------------------------------------------------------------------------
// Playing Card Picker & Shuffler
// -----------------------------------------------------------------------------

private enum class DeckConfig(val label: String) {
    STANDARD_52("52 Cards"),
    WITH_JOKERS_54("54 (+Jokers)"),
    EUCHRE_24("24 (9–A)"),
    TWO_DECKS("2 Decks (104)"),
}

private data class PlayingCard(val rank: String, val suit: String, val isRedSuit: Boolean)

private fun buildDeck(config: DeckConfig): List<PlayingCard> {
    val suits = listOf("♠" to false, "♥" to true, "♦" to true, "♣" to false)
    val allRanks = listOf("A", "2", "3", "4", "5", "6", "7", "8", "9", "10", "J", "Q", "K")
    val euchreRanks = listOf("9", "10", "J", "Q", "K", "A")

    val single = when (config) {
        DeckConfig.EUCHRE_24 -> suits.flatMap { (s, red) -> euchreRanks.map { r -> PlayingCard(r, s, red) } }
        DeckConfig.STANDARD_52, DeckConfig.TWO_DECKS -> suits.flatMap { (s, red) -> allRanks.map { r -> PlayingCard(r, s, red) } }
        DeckConfig.WITH_JOKERS_54 -> suits.flatMap { (s, red) -> allRanks.map { r -> PlayingCard(r, s, red) } } +
            listOf(PlayingCard("JKR", "★", false), PlayingCard("JKR", "☆", true))
    }
    return if (config == DeckConfig.TWO_DECKS) (single + single).shuffled() else single.shuffled()
}

@Composable
fun PlayingCardPickerScreen(
    viewModel: MainViewModel,
    isFavorite: Boolean,
    onBack: () -> Unit,
    onToggleFavorite: () -> Unit,
) {
    val palette = MonoTheme.palette
    var config by remember { mutableStateOf(DeckConfig.STANDARD_52) }
    val deck = remember { mutableStateListOf<PlayingCard>().apply { addAll(buildDeck(config)) } }
    val drawnHistory = remember { mutableStateListOf<PlayingCard>() }
    var lastDraw by remember { mutableStateOf<List<PlayingCard>>(emptyList()) }

    fun reshuffle(newConfig: DeckConfig = config) {
        config = newConfig
        deck.clear()
        deck.addAll(buildDeck(newConfig))
        drawnHistory.clear()
        lastDraw = emptyList()
        viewModel.feedbackClick()
    }

    fun drawCards(n: Int) {
        if (deck.isEmpty()) return
        val count = minOf(n, deck.size)
        val picked = List(count) { deck.removeAt(0) }
        lastDraw = picked
        drawnHistory.addAll(0, picked.reversed())
        viewModel.feedbackAccent()
    }

    Column(modifier = Modifier.fillMaxSize()) {
        MonoToolTopBar(
            title = "Playing Card Picker",
            subtitle = "${deck.size} remaining in shoe",
            onBack = onBack,
            isFavorite = isFavorite,
            onToggleFavorite = onToggleFavorite,
            actions = {
                IconButton(onClick = { reshuffle() }) {
                    Icon(Icons.Outlined.Shuffle, contentDescription = "Reshuffle deck", tint = palette.textPrimary)
                }
            },
        )
        LazyColumn(
            modifier = Modifier.weight(1f).navigationBarsPadding(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            item {
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(DeckConfig.entries) { cfg ->
                        MonoChip(cfg.label, config == cfg, { reshuffle(cfg) })
                    }
                }
            }

            item {
                MonoCard(elevated = true, contentPadding = PaddingValues(20.dp)) {
                    Column(modifier = Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
                        if (lastDraw.isEmpty()) {
                            Icon(Icons.Outlined.Style, contentDescription = null, tint = palette.textMuted, modifier = Modifier.size(44.dp))
                            Spacer(Modifier.height(8.dp))
                            Text("Deck shuffled (${deck.size} cards)", style = MaterialTheme.typography.titleMedium, color = palette.textSecondary)
                        } else {
                            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                lastDraw.forEach { card ->
                                    Box(
                                        modifier = Modifier
                                            .size(width = 72.dp, height = 104.dp)
                                            .clip(RoundedCornerShape(14.dp))
                                            .background(palette.accent)
                                            .border(1.dp, palette.border, RoundedCornerShape(14.dp))
                                            .padding(8.dp),
                                        contentAlignment = Alignment.Center,
                                    ) {
                                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                            Text(
                                                text = card.rank,
                                                style = TabularNumeralStyle.copy(fontSize = 24.sp, fontWeight = FontWeight.Bold),
                                                color = if (card.isRedSuit) palette.teamRed else palette.onAccent,
                                            )
                                            Text(
                                                text = card.suit,
                                                fontSize = 26.sp,
                                                color = if (card.isRedSuit) palette.teamRed else palette.onAccent,
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        Spacer(Modifier.height(16.dp))
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            MonoPrimaryButton("Draw 1", { drawCards(1) }, enabled = deck.isNotEmpty(), modifier = Modifier.weight(1f))
                            MonoSecondaryButton("Draw 3", { drawCards(3) }, enabled = deck.isNotEmpty(), modifier = Modifier.weight(1f))
                            MonoSecondaryButton("Draw 5", { drawCards(5) }, enabled = deck.isNotEmpty(), modifier = Modifier.weight(1f))
                        }
                    }
                }
            }

            if (drawnHistory.isNotEmpty()) {
                item {
                    Text("Draw History (${drawnHistory.size})", style = MaterialTheme.typography.titleSmall, color = palette.textPrimary)
                }
                item {
                    MonoCard(contentPadding = PaddingValues(12.dp)) {
                        Text(
                            text = drawnHistory.joinToString("  ") { "${it.rank}${it.suit}" },
                            style = TabularNumeralStyle.copy(fontSize = 15.sp, lineHeight = 24.sp),
                            color = palette.textSecondary,
                        )
                    }
                }
            }
        }
    }
}
