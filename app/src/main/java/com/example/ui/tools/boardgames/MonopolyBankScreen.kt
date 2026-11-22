package com.example.ui.tools.boardgames

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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.Undo
import androidx.compose.material.icons.outlined.AccountBalance
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.Calculate
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material.icons.outlined.Save
import androidx.compose.material.icons.outlined.SwapHoriz
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
import androidx.compose.ui.text.input.KeyboardType
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

private data class MonopolyPlayer(val id: Int, val name: String, val balance: Int)
private data class MonopolyTx(val description: String, val snapshot: List<MonopolyPlayer>)

private data class PropertyRentTier(val group: String, val rents: List<Int>)

private val rentGroups = listOf(
    PropertyRentTier("Brown (Mediterranean / Baltic)", listOf(4, 8, 20, 60, 180, 320, 450)),
    PropertyRentTier("Light Blue (Oriental / Conn.)", listOf(6, 12, 30, 90, 270, 400, 550)),
    PropertyRentTier("Pink (St. Charles / Virginia)", listOf(10, 20, 50, 150, 450, 625, 750)),
    PropertyRentTier("Orange (St. James / New York)", listOf(14, 28, 70, 200, 550, 750, 950)),
    PropertyRentTier("Red (Kentucky / Illinois)", listOf(18, 36, 90, 250, 700, 875, 1050)),
    PropertyRentTier("Yellow (Atlantic / Marvin)", listOf(22, 44, 110, 330, 800, 975, 1150)),
    PropertyRentTier("Green (Pacific / Penn.)", listOf(26, 52, 130, 390, 900, 1100, 1275)),
    PropertyRentTier("Dark Blue (Park Pl / Boardwalk)", listOf(50, 100, 200, 600, 1400, 1700, 2000)),
)

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun MonopolyBankScreen(
    viewModel: MainViewModel,
    isFavorite: Boolean,
    onBack: () -> Unit,
    onToggleFavorite: () -> Unit,
) {
    val palette = MonoTheme.palette
    val context = LocalContext.current

    val players = remember {
        mutableStateListOf(
            MonopolyPlayer(1, "Player 1", 1500),
            MonopolyPlayer(2, "Player 2", 1500),
            MonopolyPlayer(3, "Player 3", 1500),
            MonopolyPlayer(4, "Player 4", 1500),
        )
    }
    var freeParkingPot by remember { mutableIntStateOf(0) }
    val transactions = remember { mutableStateListOf<MonopolyTx>() }
    var selectedPlayerIdx by remember { mutableIntStateOf(0) }

    var showTransferDialog by remember { mutableStateOf(false) }
    var showRentDialog by remember { mutableStateOf(false) }
    var showRenameDialog by remember { mutableStateOf<Int?>(null) }
    var customDeltaText by remember { mutableStateOf("100") }

    fun recordAndApply(desc: String, updater: () -> Unit) {
        val snap = players.toList()
        updater()
        transactions.add(0, MonopolyTx(desc, snap))
        if (transactions.size > 40) transactions.removeAt(transactions.lastIndex)
        viewModel.feedbackClick()
    }

    fun undo() {
        if (transactions.isEmpty()) return
        val last = transactions.removeAt(0)
        players.clear()
        players.addAll(last.snapshot)
        viewModel.feedbackClick()
    }

    Column(modifier = Modifier.fillMaxSize()) {
        MonoToolTopBar(
            title = "Monopoly Bank",
            subtitle = "${players.size} players • Free Parking $$freeParkingPot",
            onBack = onBack,
            isFavorite = isFavorite,
            onToggleFavorite = onToggleFavorite,
            actions = {
                IconButton(onClick = { undo() }, enabled = transactions.isNotEmpty()) {
                    Icon(
                        Icons.AutoMirrored.Outlined.Undo,
                        contentDescription = "Undo",
                        tint = if (transactions.isNotEmpty()) palette.textPrimary else palette.textMuted,
                    )
                }
                IconButton(onClick = {
                    val summary = players.joinToString(" | ") { "${it.name}: $${it.balance}" }
                    viewModel.logHistory(
                        toolId = UtilityIds.MONOPOLY_BANK,
                        title = "Monopoly Bank Ledger",
                        summary = summary,
                        detailText = transactions.take(15).joinToString("\n") { it.description },
                        primaryValue = "$${players.maxOfOrNull { it.balance } ?: 0}",
                    )
                    Toast.makeText(context, "Ledger saved to History", Toast.LENGTH_SHORT).show()
                }) {
                    Icon(Icons.Outlined.Save, contentDescription = "Save ledger", tint = palette.textPrimary)
                }
            },
        )

        LazyColumn(
            modifier = Modifier.weight(1f).navigationBarsPadding(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            // Player Balances
            item {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    players.forEachIndexed { idx, p ->
                        val active = idx == selectedPlayerIdx
                        MonoCard(
                            elevated = active,
                            borderColor = if (active) palette.accent else palette.border,
                            onClick = { selectedPlayerIdx = idx },
                            contentPadding = PaddingValues(horizontal = 14.dp, vertical = 12.dp),
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = p.name,
                                        style = MaterialTheme.typography.titleMedium,
                                        color = palette.textPrimary,
                                        fontWeight = if (active) FontWeight.Bold else FontWeight.Medium,
                                    )
                                    IconButton(
                                        onClick = { showRenameDialog = idx },
                                        modifier = Modifier.size(32.dp),
                                    ) {
                                        Icon(Icons.Outlined.Edit, contentDescription = "Rename", tint = palette.textMuted, modifier = Modifier.size(16.dp))
                                    }
                                }
                                Text(
                                    text = "$${p.balance}",
                                    style = TabularNumeralStyle.copy(fontSize = 24.sp, fontWeight = FontWeight.Bold),
                                    color = if (p.balance < 0) palette.teamRed else palette.textPrimary,
                                )
                            }
                        }
                    }
                }
            }

            // Quick Actions for Active Player
            item {
                val activePlayer = players.getOrNull(selectedPlayerIdx)
                if (activePlayer != null) {
                    MonoCard(contentPadding = PaddingValues(14.dp)) {
                        Text(
                            text = "Bank Actions — ${activePlayer.name}",
                            style = MaterialTheme.typography.titleSmall,
                            color = palette.textSecondary,
                        )
                        Spacer(Modifier.height(10.dp))
                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            listOf(
                                "+$200 GO" to 200,
                                "+$100" to 100,
                                "+$500" to 500,
                                "-$50 Jail" to -50,
                                "-$100 Tax" to -100,
                                "-$200 Tax" to -200,
                            ).forEach { (label, delta) ->
                                Surface(
                                    onClick = {
                                        recordAndApply("${activePlayer.name} ${if (delta >= 0) "+" else "-"}$${kotlin.math.abs(delta)} ($label)") {
                                            players[selectedPlayerIdx] = activePlayer.copy(balance = activePlayer.balance + delta)
                                        }
                                    },
                                    shape = RoundedCornerShape(10.dp),
                                    color = palette.surfaceSecondary,
                                    border = BorderStroke(1.dp, palette.border),
                                ) {
                                    Text(
                                        text = label,
                                        style = MaterialTheme.typography.labelLarge,
                                        color = if (delta >= 0) palette.success else palette.teamRed,
                                        fontWeight = FontWeight.SemiBold,
                                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 9.dp),
                                    )
                                }
                            }
                        }
                        Spacer(Modifier.height(10.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            OutlinedTextField(
                                value = customDeltaText,
                                onValueChange = { customDeltaText = it.filter { c -> c.isDigit() }.take(5) },
                                label = { Text("Amount ($)") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                singleLine = true,
                                modifier = Modifier.weight(1f),
                            )
                            MonoPrimaryButton(
                                text = "+Add",
                                onClick = {
                                    val amt = customDeltaText.toIntOrNull() ?: return@MonoPrimaryButton
                                    recordAndApply("${activePlayer.name} +$$amt from Bank") {
                                        players[selectedPlayerIdx] = activePlayer.copy(balance = activePlayer.balance + amt)
                                    }
                                },
                            )
                            MonoSecondaryButton(
                                text = "-Sub",
                                onClick = {
                                    val amt = customDeltaText.toIntOrNull() ?: return@MonoSecondaryButton
                                    recordAndApply("${activePlayer.name} -$$amt to Bank") {
                                        players[selectedPlayerIdx] = activePlayer.copy(balance = activePlayer.balance - amt)
                                    }
                                },
                            )
                        }
                    }
                }
            }

            // Tools: Transfer, Rent Calculator, Add/Reset
            item {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    MonoPrimaryButton(
                        text = "Transfer",
                        icon = Icons.Outlined.SwapHoriz,
                        onClick = { showTransferDialog = true },
                        modifier = Modifier.weight(1f),
                    )
                    MonoSecondaryButton(
                        text = "Rent Calc",
                        icon = Icons.Outlined.Calculate,
                        onClick = { showRentDialog = true },
                        modifier = Modifier.weight(1f),
                    )
                }
                Spacer(Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    MonoSecondaryButton(
                        text = "Add Player",
                        icon = Icons.Outlined.Add,
                        enabled = players.size < 8,
                        onClick = {
                            val nextId = (players.maxOfOrNull { it.id } ?: 0) + 1
                            players.add(MonopolyPlayer(nextId, "Player $nextId", 1500))
                        },
                        modifier = Modifier.weight(1f),
                    )
                    MonoSecondaryButton(
                        text = "New Game",
                        icon = Icons.Outlined.Refresh,
                        onClick = {
                            val count = players.size
                            val names = players.map { it.name }
                            players.clear()
                            repeat(count) { i -> players.add(MonopolyPlayer(i + 1, names[i], 1500)) }
                            freeParkingPot = 0
                            transactions.clear()
                        },
                        modifier = Modifier.weight(1f),
                    )
                }
            }

            // Transaction Ledger
            if (transactions.isNotEmpty()) {
                item {
                    Text("Transaction History", style = MaterialTheme.typography.titleSmall, color = palette.textPrimary)
                }
                items(transactions.take(25)) { tx ->
                    MonoCard(contentPadding = PaddingValues(horizontal = 12.dp, vertical = 10.dp)) {
                        Text(tx.description, style = MaterialTheme.typography.bodyMedium, color = palette.textSecondary)
                    }
                }
            }
        }
    }

    // Transfer Dialog
    if (showTransferDialog) {
        var fromIdx by remember { mutableIntStateOf(selectedPlayerIdx) }
        var toIdx by remember { mutableIntStateOf(if (players.size > 1) (selectedPlayerIdx + 1) % players.size else 0) }
        var amtText by remember { mutableStateOf("100") }
        AlertDialog(
            onDismissRequest = { showTransferDialog = false },
            containerColor = palette.cardElevated,
            titleContentColor = palette.textPrimary,
            title = { Text("Player Transfer") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.verticalScroll(rememberScrollState())) {
                    Text("From:", style = MaterialTheme.typography.labelMedium, color = palette.textSecondary)
                    FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        players.forEachIndexed { idx, p ->
                            MonoChip(p.name, fromIdx == idx, { fromIdx = idx })
                        }
                    }
                    Text("To:", style = MaterialTheme.typography.labelMedium, color = palette.textSecondary)
                    FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        players.forEachIndexed { idx, p ->
                            MonoChip(p.name, toIdx == idx, { toIdx = idx })
                        }
                        MonoChip("Free Parking", toIdx == -1, { toIdx = -1 })
                    }
                    OutlinedTextField(
                        value = amtText,
                        onValueChange = { amtText = it.filter { c -> c.isDigit() }.take(5) },
                        label = { Text("Transfer Amount ($)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    val amt = amtText.toIntOrNull() ?: 0
                    if (amt > 0 && fromIdx != toIdx) {
                        val payer = players[fromIdx]
                        if (toIdx == -1) {
                            recordAndApply("${payer.name} paid $$amt to Free Parking") {
                                players[fromIdx] = payer.copy(balance = payer.balance - amt)
                                freeParkingPot += amt
                            }
                        } else {
                            val receiver = players[toIdx]
                            recordAndApply("${payer.name} paid $$amt to ${receiver.name}") {
                                players[fromIdx] = payer.copy(balance = payer.balance - amt)
                                players[toIdx] = receiver.copy(balance = receiver.balance + amt)
                            }
                        }
                    }
                    showTransferDialog = false
                }) { Text("Transfer", color = palette.accent) }
            },
            dismissButton = {
                TextButton(onClick = { showTransferDialog = false }) { Text("Cancel", color = palette.textSecondary) }
            },
        )
    }

    // Rent Calculator Dialog
    if (showRentDialog) {
        var groupIdx by remember { mutableIntStateOf(0) }
        var tierIdx by remember { mutableIntStateOf(0) } // 0=Base,1=Monopoly,2..5=1..4Houses,6=Hotel
        var payerIdx by remember { mutableIntStateOf(selectedPlayerIdx) }
        var ownerIdx by remember { mutableIntStateOf(if (players.size > 1) (selectedPlayerIdx + 1) % players.size else 0) }
        val tierLabels = listOf("Base", "2x Set", "1H", "2H", "3H", "4H", "Hotel")
        val calculatedRent = rentGroups[groupIdx].rents[tierIdx]

        AlertDialog(
            onDismissRequest = { showRentDialog = false },
            containerColor = palette.cardElevated,
            titleContentColor = palette.textPrimary,
            title = { Text("Rent Calculator") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.verticalScroll(rememberScrollState())) {
                    Text("Property Color Group", style = MaterialTheme.typography.labelMedium, color = palette.textSecondary)
                    FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        rentGroups.forEachIndexed { idx, g ->
                            MonoChip(g.group.substringBefore(" ("), groupIdx == idx, { groupIdx = idx })
                        }
                    }
                    Text("Development", style = MaterialTheme.typography.labelMedium, color = palette.textSecondary)
                    FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        tierLabels.forEachIndexed { idx, l ->
                            MonoChip(l, tierIdx == idx, { tierIdx = idx })
                        }
                    }
                    Text(
                        text = "Calculated Rent: $$calculatedRent",
                        style = TabularNumeralStyle.copy(fontSize = 22.sp, fontWeight = FontWeight.Bold),
                        color = palette.accent,
                    )
                    Text("Payer:", style = MaterialTheme.typography.labelMedium, color = palette.textSecondary)
                    FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        players.forEachIndexed { idx, p -> MonoChip(p.name, payerIdx == idx, { payerIdx = idx }) }
                    }
                    Text("Owner:", style = MaterialTheme.typography.labelMedium, color = palette.textSecondary)
                    FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        players.forEachIndexed { idx, p -> MonoChip(p.name, ownerIdx == idx, { ownerIdx = idx }) }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    if (payerIdx != ownerIdx) {
                        val payer = players[payerIdx]
                        val owner = players[ownerIdx]
                        recordAndApply("${payer.name} paid $$calculatedRent rent to ${owner.name}") {
                            players[payerIdx] = payer.copy(balance = payer.balance - calculatedRent)
                            players[ownerIdx] = owner.copy(balance = owner.balance + calculatedRent)
                        }
                    }
                    showRentDialog = false
                }) { Text("Charge Rent", color = palette.accent) }
            },
            dismissButton = {
                TextButton(onClick = { showRentDialog = false }) { Text("Close", color = palette.textSecondary) }
            },
        )
    }

    // Rename Player Dialog
    showRenameDialog?.let { idx ->
        var nameDraft by remember(idx) { mutableStateOf(players[idx].name) }
        AlertDialog(
            onDismissRequest = { showRenameDialog = null },
            containerColor = palette.cardElevated,
            titleContentColor = palette.textPrimary,
            title = { Text("Rename Player") },
            text = {
                OutlinedTextField(
                    value = nameDraft,
                    onValueChange = { nameDraft = it },
                    label = { Text("Player Name") },
                    singleLine = true,
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    if (nameDraft.isNotBlank()) {
                        players[idx] = players[idx].copy(name = nameDraft.trim())
                    }
                    showRenameDialog = null
                }) { Text("Save", color = palette.accent) }
            },
        )
    }
}
