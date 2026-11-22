package com.example.ui.screens

import android.content.Intent
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.BugReport
import androidx.compose.material.icons.outlined.Code
import androidx.compose.material.icons.outlined.DeleteForever
import androidx.compose.material.icons.outlined.DeleteSweep
import androidx.compose.material.icons.outlined.Download
import androidx.compose.material.icons.outlined.Forum
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Policy
import androidx.compose.material.icons.outlined.Upload
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.data.prefs.PlayKitSettings
import com.example.ui.MainViewModel
import com.example.ui.components.MonoCard
import com.example.ui.theme.MonoTheme
import com.example.ui.theme.ThemeMode
import kotlinx.coroutines.launch

@Composable
fun SettingsScreen(
    settings: PlayKitSettings,
    viewModel: MainViewModel,
    modifier: Modifier = Modifier,
) {
    val palette = MonoTheme.palette
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var showClearHistoryDialog by remember { mutableStateOf(false) }
    var showResetDialog by remember { mutableStateOf(false) }
    var showTeamDefaultsDialog by remember { mutableStateOf(false) }
    var showPresetDialog by remember { mutableStateOf(false) }
    var infoDialogContent by remember { mutableStateOf<Pair<String, String>?>(null) }

    val exportLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/json")
    ) { uri ->
        if (uri != null) {
            scope.launch {
                runCatching {
                    val json = viewModel.repository.exportBackupJson()
                    context.contentResolver.openOutputStream(uri)?.use { out ->
                        out.write(json.toByteArray(Charsets.UTF_8))
                    }
                }.onSuccess {
                    Toast.makeText(context, "Backup exported successfully", Toast.LENGTH_SHORT).show()
                }.onFailure {
                    Toast.makeText(context, "Export failed: ${it.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    val importLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri ->
        if (uri != null) {
            scope.launch {
                runCatching {
                    val json = context.contentResolver.openInputStream(uri)?.bufferedReader()?.use { it.readText() }
                        ?: error("Could not read file")
                    viewModel.repository.importBackupJson(json).getOrThrow()
                }.onSuccess { count ->
                    Toast.makeText(context, "Restored $count records & preferences", Toast.LENGTH_SHORT).show()
                }.onFailure {
                    Toast.makeText(context, "Import failed: ${it.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.TopCenter,
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .widthIn(max = 840.dp),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 32.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .statusBarsPadding()
                        .padding(top = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    Column {
                        Text(
                            text = "Settings",
                            style = MaterialTheme.typography.headlineMedium,
                            color = palette.textPrimary,
                            fontWeight = FontWeight.Bold,
                        )
                        Text(
                            text = "100% Free • Offline-First • No Ads",
                            style = MaterialTheme.typography.bodyMedium,
                            color = palette.textSecondary,
                        )
                    }
                    // Visual Mono Identity Card
                    val bannerShape = RoundedCornerShape(18.dp)
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(96.dp)
                            .clip(bannerShape)
                            .border(1.dp, palette.border, bannerShape),
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.img_hero_boardgames_1795355382325),
                            contentDescription = "Mono PlayKit studio hardware",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize(),
                        )
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(
                                    Brush.horizontalGradient(
                                        0.0f to Color(0xEC080808),
                                        0.70f to Color(0xC0080808),
                                        1.0f to Color(0x66080808),
                                    )
                                )
                        )
                        Row(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(horizontal = 16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Image(
                                painter = painterResource(id = R.drawable.ic_playkit_mark_1795354177166),
                                contentDescription = "Mono PlayKit emblem",
                                modifier = Modifier
                                    .size(50.dp)
                                    .clip(RoundedCornerShape(14.dp))
                                    .border(1.dp, Color(0x55FFFFFF), RoundedCornerShape(14.dp)),
                            )
                            Spacer(Modifier.width(14.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Mono PlayKit",
                                    style = MaterialTheme.typography.titleMedium,
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold,
                                )
                                Text(
                                    text = "Your Games. Your Tools.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Color(0xFFD0D0D0),
                                )
                            }
                        }
                    }
                }
            }

            // Appearance
            item {
                SettingsSectionTitle("Appearance")
                Spacer(Modifier.height(8.dp))
                MonoCard(contentPadding = PaddingValues(vertical = 4.dp)) {
                    ThemeMode.entries.forEachIndexed { idx, mode ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .minimumInteractiveComponentSize()
                                .clickable { viewModel.setThemeMode(mode) }
                                .padding(horizontal = 16.dp, vertical = 10.dp)
                                .testTag("theme_option_${mode.name}"),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            RadioButton(
                                selected = settings.themeMode == mode,
                                onClick = { viewModel.setThemeMode(mode) },
                                colors = RadioButtonDefaults.colors(
                                    selectedColor = palette.accent,
                                    unselectedColor = palette.textMuted,
                                ),
                            )
                            Spacer(Modifier.width(8.dp))
                            Text(
                                text = mode.label,
                                style = MaterialTheme.typography.bodyLarge,
                                color = palette.textPrimary,
                            )
                        }
                        if (idx < ThemeMode.entries.lastIndex) {
                            HorizontalDivider(color = palette.border, thickness = 0.5.dp)
                        }
                    }
                }
            }

            // Preferences
            item {
                SettingsSectionTitle("Preferences")
                Spacer(Modifier.height(8.dp))
                MonoCard(contentPadding = PaddingValues(0.dp)) {
                    SettingsSwitchRow(
                        title = "Sound Effects",
                        subtitle = "Synthesized offline cues for timers, dice & metronome",
                        checked = settings.soundEnabled,
                        onCheckedChange = viewModel::setSoundEnabled,
                    )
                    HorizontalDivider(color = palette.border, thickness = 0.5.dp)
                    SettingsSwitchRow(
                        title = "Haptic Feedback",
                        subtitle = "Tactile pulse on clock switches, rolls & counters",
                        checked = settings.hapticEnabled,
                        onCheckedChange = viewModel::setHapticEnabled,
                    )
                    HorizontalDivider(color = palette.border, thickness = 0.5.dp)
                    SettingsSwitchRow(
                        title = "WCA 15s Cube Inspection",
                        subtitle = "Default inspection countdown in Rubik's Cube Timer",
                        checked = settings.cubeInspectionEnabled,
                        onCheckedChange = viewModel::setCubeInspectionEnabled,
                    )
                    HorizontalDivider(color = palette.border, thickness = 0.5.dp)
                    SettingsSwitchRow(
                        title = "Keep Screen Awake",
                        subtitle = "Prevent screen sleep while using clocks and scoreboards",
                        checked = settings.keepScreenAwake,
                        onCheckedChange = viewModel::setKeepScreenAwake,
                    )
                    HorizontalDivider(color = palette.border, thickness = 0.5.dp)
                    SettingsSwitchRow(
                        title = "Save Dice History",
                        subtitle = "Log dice rolls to local History database",
                        checked = settings.saveDiceHistory,
                        onCheckedChange = viewModel::setSaveDiceHistory,
                    )
                    HorizontalDivider(color = palette.border, thickness = 0.5.dp)
                    SettingsActionRow(
                        icon = null,
                        title = "Default Scoreboard Preset",
                        subtitle = settings.defaultScoreboardPreset.lowercase().replaceFirstChar { it.uppercase() }.replace("_", " "),
                        onClick = { showPresetDialog = true },
                    )
                    HorizontalDivider(color = palette.border, thickness = 0.5.dp)
                    SettingsActionRow(
                        icon = null,
                        title = "Default Team Names",
                        subtitle = "${settings.defaultRedTeamName} vs ${settings.defaultBlueTeamName}",
                        onClick = { showTeamDefaultsDialog = true },
                    )
                }
            }

            // Data Management
            item {
                SettingsSectionTitle("Data Management")
                Spacer(Modifier.height(8.dp))
                MonoCard(contentPadding = PaddingValues(0.dp)) {
                    SettingsActionRow(
                        icon = Icons.Outlined.Upload,
                        title = "Export Backup (JSON)",
                        subtitle = "Save history, favorites, checklists & settings locally",
                        onClick = { exportLauncher.launch("mono_playkit_backup.json") },
                    )
                    HorizontalDivider(color = palette.border, thickness = 0.5.dp)
                    SettingsActionRow(
                        icon = Icons.Outlined.Download,
                        title = "Import Backup",
                        subtitle = "Restore from a Mono PlayKit JSON backup file",
                        onClick = { importLauncher.launch(arrayOf("application/json", "*/*")) },
                    )
                    HorizontalDivider(color = palette.border, thickness = 0.5.dp)
                    SettingsActionRow(
                        icon = Icons.Outlined.DeleteSweep,
                        title = "Clear History",
                        subtitle = "Delete all saved solves, matches & timer records",
                        onClick = { showClearHistoryDialog = true },
                    )
                    HorizontalDivider(color = palette.border, thickness = 0.5.dp)
                    SettingsActionRow(
                        icon = Icons.Outlined.DeleteForever,
                        title = "Reset Application Data",
                        subtitle = "Restore factory defaults and erase all local data",
                        tintRed = true,
                        onClick = { showResetDialog = true },
                    )
                }
            }

            // About
            item {
                SettingsSectionTitle("About")
                Spacer(Modifier.height(8.dp))
                MonoCard(contentPadding = PaddingValues(0.dp)) {
                    SettingsActionRow(
                        icon = Icons.Outlined.Info,
                        title = "About Mono PlayKit",
                        subtitle = "Version 1.0.0 • Your Games. Your Tools.",
                        onClick = {
                            infoDialogContent = "About Mono PlayKit" to
                                "Mono PlayKit is an offline-first companion toolkit for physical games, board games, puzzles, and tabletop toys. Built with the Mono Black & Grey design system. 100% free, zero ads, and zero tracking."
                        },
                    )
                    HorizontalDivider(color = palette.border, thickness = 0.5.dp)
                    SettingsActionRow(
                        icon = Icons.Outlined.Policy,
                        title = "Open Source Licenses",
                        subtitle = "Jetpack Compose, Material 3, Room, DataStore & Google Fonts",
                        onClick = {
                            infoDialogContent = "Open Source Licenses" to
                                "Uses Apache 2.0 licensed libraries: AndroidX Jetpack Compose, Material 3, Navigation Compose, Room Database, DataStore Preferences, Kotlin Coroutines, and OFL-licensed Space Grotesk, Inter, and JetBrains Mono fonts."
                        },
                    )
                    HorizontalDivider(color = palette.border, thickness = 0.5.dp)
                    SettingsActionRow(
                        icon = Icons.Outlined.Code,
                        title = "Open Source Architecture",
                        subtitle = "Mono Ecosystem • Local-first storage & zero telemetry",
                        onClick = {
                            infoDialogContent = "Open Source & Offline Philosophy" to
                                "Mono PlayKit stores all user sessions, cube solves, checklists, and settings exclusively in local Room SQLite and DataStore files on your device. You can export or import your entire state anytime as plain JSON."
                        },
                    )
                    HorizontalDivider(color = palette.border, thickness = 0.5.dp)
                    SettingsActionRow(
                        icon = Icons.Outlined.BugReport,
                        title = "Share Diagnostic Summary",
                        subtitle = "Generate an offline issue report snapshot via Share Sheet",
                        onClick = {
                            val summary = "Mono PlayKit v1.0.0\nTheme: ${settings.themeMode.label}\nOffline Mode: Active\nFeedback / Issue Description:\n"
                            val intent = Intent(Intent.ACTION_SEND).apply {
                                type = "text/plain"
                                putExtra(Intent.EXTRA_SUBJECT, "Mono PlayKit Issue Report")
                                putExtra(Intent.EXTRA_TEXT, summary)
                            }
                            context.startActivity(Intent.createChooser(intent, "Share Report"))
                        },
                    )
                    HorizontalDivider(color = palette.border, thickness = 0.5.dp)
                    SettingsActionRow(
                        icon = Icons.Outlined.Forum,
                        title = "Mono Ecosystem",
                        subtitle = "Designed alongside MonoWidgets & MonoPlayer",
                        onClick = {
                            infoDialogContent = "Mono Community & Ecosystem" to
                                "Mono PlayKit shares the minimalist black-and-grey hardware-inspired aesthetic of MonoWidgets and MonoPlayer—built for physical play without digital distractions."
                        },
                    )
                }
            }
        }
    }

    if (showClearHistoryDialog) {
        AlertDialog(
            onDismissRequest = { showClearHistoryDialog = false },
            containerColor = palette.cardElevated,
            titleContentColor = palette.textPrimary,
            textContentColor = palette.textSecondary,
            title = { Text("Clear All History?") },
            text = { Text("All saved timer records, scoreboards, cube solves, and dice rolls will be removed.") },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.clearAllHistory()
                    showClearHistoryDialog = false
                    Toast.makeText(context, "History cleared", Toast.LENGTH_SHORT).show()
                }) { Text("Clear", color = palette.teamRed) }
            },
            dismissButton = {
                TextButton(onClick = { showClearHistoryDialog = false }) { Text("Cancel", color = palette.textSecondary) }
            },
        )
    }

    if (showResetDialog) {
        AlertDialog(
            onDismissRequest = { showResetDialog = false },
            containerColor = palette.cardElevated,
            titleContentColor = palette.textPrimary,
            textContentColor = palette.textSecondary,
            title = { Text("Reset Application Data?") },
            text = { Text("This erases all history, checklists, custom presets, favorites, and preferences.") },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.resetAllAppData()
                    showResetDialog = false
                    Toast.makeText(context, "Application data reset", Toast.LENGTH_SHORT).show()
                }) { Text("Reset", color = palette.teamRed) }
            },
            dismissButton = {
                TextButton(onClick = { showResetDialog = false }) { Text("Cancel", color = palette.textSecondary) }
            },
        )
    }

    if (showPresetDialog) {
        val presets = listOf("CUSTOM", "BASKETBALL", "VOLLEYBALL", "BADMINTON", "TABLE_TENNIS", "TENNIS", "FOOTBALL", "BOXING", "DARTS_501")
        AlertDialog(
            onDismissRequest = { showPresetDialog = false },
            containerColor = palette.cardElevated,
            titleContentColor = palette.textPrimary,
            textContentColor = palette.textSecondary,
            title = { Text("Default Scoreboard Preset") },
            text = {
                Column {
                    presets.forEach { preset ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    viewModel.setDefaultScoreboardPreset(preset)
                                    showPresetDialog = false
                                }
                                .padding(vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            RadioButton(
                                selected = settings.defaultScoreboardPreset == preset,
                                onClick = {
                                    viewModel.setDefaultScoreboardPreset(preset)
                                    showPresetDialog = false
                                },
                            )
                            Text(
                                text = preset.lowercase().replace("_", " ").replaceFirstChar { it.uppercase() },
                                color = palette.textPrimary,
                            )
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showPresetDialog = false }) { Text("Done", color = palette.accent) }
            },
        )
    }

    if (showTeamDefaultsDialog) {
        var redName by remember { mutableStateOf(settings.defaultRedTeamName) }
        var blueName by remember { mutableStateOf(settings.defaultBlueTeamName) }
        AlertDialog(
            onDismissRequest = { showTeamDefaultsDialog = false },
            containerColor = palette.cardElevated,
            titleContentColor = palette.textPrimary,
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = redName,
                        onValueChange = { redName = it },
                        label = { Text("Left (Red) Default Name") },
                        singleLine = true,
                    )
                    OutlinedTextField(
                        value = blueName,
                        onValueChange = { blueName = it },
                        label = { Text("Right (Blue) Default Name") },
                        singleLine = true,
                    )
                }
            },
            title = { Text("Default Team Names") },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.setDefaultTeamNames(redName, blueName)
                    showTeamDefaultsDialog = false
                }) { Text("Save", color = palette.accent) }
            },
            dismissButton = {
                TextButton(onClick = { showTeamDefaultsDialog = false }) { Text("Cancel", color = palette.textSecondary) }
            },
        )
    }

    infoDialogContent?.let { (title, body) ->
        AlertDialog(
            onDismissRequest = { infoDialogContent = null },
            containerColor = palette.cardElevated,
            titleContentColor = palette.textPrimary,
            textContentColor = palette.textSecondary,
            title = { Text(title) },
            text = { Text(body) },
            confirmButton = {
                TextButton(onClick = { infoDialogContent = null }) { Text("Close", color = palette.accent) }
            },
        )
    }
}

@Composable
private fun SettingsSectionTitle(title: String) {
    Text(
        text = title.uppercase(),
        style = MaterialTheme.typography.labelMedium,
        color = MonoTheme.palette.textMuted,
        fontWeight = FontWeight.SemiBold,
    )
}

@Composable
private fun SettingsSwitchRow(
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
) {
    val palette = MonoTheme.palette
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .minimumInteractiveComponentSize()
            .clickable { onCheckedChange(!checked) }
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(title, style = MaterialTheme.typography.titleSmall, color = palette.textPrimary)
            Text(subtitle, style = MaterialTheme.typography.bodyMedium, color = palette.textSecondary)
        }
        Spacer(Modifier.width(12.dp))
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = palette.onAccent,
                checkedTrackColor = palette.accent,
                uncheckedThumbColor = palette.textSecondary,
                uncheckedTrackColor = palette.surfaceSecondary,
                uncheckedBorderColor = palette.border,
            ),
        )
    }
}

@Composable
private fun SettingsActionRow(
    icon: ImageVector?,
    title: String,
    subtitle: String,
    tintRed: Boolean = false,
    onClick: () -> Unit,
) {
    val palette = MonoTheme.palette
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .minimumInteractiveComponentSize()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (icon != null) {
            Icon(
                imageVector = icon,
                contentDescription = title,
                tint = if (tintRed) palette.teamRed else palette.textPrimary,
                modifier = Modifier.size(22.dp),
            )
            Spacer(Modifier.width(14.dp))
        }
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall,
                color = if (tintRed) palette.teamRed else palette.textPrimary,
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodyMedium,
                color = palette.textSecondary,
            )
        }
    }
}
