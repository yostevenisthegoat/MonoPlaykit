package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.ChecklistItemEntity
import com.example.data.local.ChecklistProjectEntity
import com.example.data.local.FavoriteToolEntity
import com.example.data.local.HistoryRecordEntity
import com.example.data.model.UtilityCatalog
import com.example.data.model.UtilityTool
import com.example.data.prefs.PlayKitSettings
import com.example.data.repository.PlayKitRepository
import com.example.ui.theme.ThemeMode
import com.example.util.FeedbackManager
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class MainViewModel(application: Application) : AndroidViewModel(application) {
    val repository: PlayKitRepository = PlayKitRepository.getInstance(application)
    val feedback = FeedbackManager(application)

    val settings: StateFlow<PlayKitSettings> = repository.settings.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = PlayKitSettings(),
    )

    val favorites: StateFlow<List<FavoriteToolEntity>> = repository.favorites.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = emptyList(),
    )

    val favoriteIds: StateFlow<Set<String>> = repository.observeFavoriteIds().stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = emptySet(),
    )

    val favoriteTools: StateFlow<List<UtilityTool>> = favorites.combine(settings) { favs, _ ->
        favs.mapNotNull { UtilityCatalog.findById(it.toolId) }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = emptyList(),
    )

    val recentTools: StateFlow<List<UtilityTool>> = repository.recentTools.combine(settings) { recents, _ ->
        recents.mapNotNull { UtilityCatalog.findById(it.toolId) }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = emptyList(),
    )

    val allHistory: StateFlow<List<HistoryRecordEntity>> = repository.allHistory.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = emptyList(),
    )

    init {
        viewModelScope.launch {
            repository.ensureDefaultFavoritesIfEmpty()
        }
    }

    fun markToolOpened(toolId: String) {
        viewModelScope.launch { repository.markToolOpened(toolId) }
    }

    fun toggleFavorite(toolId: String) {
        viewModelScope.launch { repository.toggleFavorite(toolId) }
    }

    fun moveFavorite(toolId: String, direction: Int) {
        viewModelScope.launch { repository.moveFavorite(toolId, direction) }
    }

    fun logHistory(
        toolId: String,
        title: String,
        summary: String,
        detailText: String = "",
        primaryValue: String = "",
        numericValueMs: Long = 0L,
        penaltyFlag: String = "",
    ) {
        viewModelScope.launch {
            repository.logHistory(
                toolId = toolId,
                title = title,
                summary = summary,
                detailText = detailText,
                primaryValue = primaryValue,
                numericValueMs = numericValueMs,
                penaltyFlag = penaltyFlag,
            )
        }
    }

    fun observeHistoryForTool(toolId: String): Flow<List<HistoryRecordEntity>> =
        repository.observeHistoryForTool(toolId)

    fun updateHistoryRecord(record: HistoryRecordEntity) {
        viewModelScope.launch { repository.updateHistoryRecord(record) }
    }

    fun deleteHistoryRecord(id: Long) {
        viewModelScope.launch { repository.deleteHistoryRecord(id) }
    }

    fun deleteHistoryForTool(toolId: String) {
        viewModelScope.launch { repository.deleteHistoryForTool(toolId) }
    }

    fun clearAllHistory() {
        viewModelScope.launch { repository.clearAllHistory() }
    }

    // Settings updates
    fun setThemeMode(mode: ThemeMode) = viewModelScope.launch { repository.preferences.setThemeMode(mode) }
    fun setSoundEnabled(v: Boolean) = viewModelScope.launch { repository.preferences.setSoundEnabled(v) }
    fun setHapticEnabled(v: Boolean) = viewModelScope.launch { repository.preferences.setHapticEnabled(v) }
    fun setCubeInspectionEnabled(v: Boolean) = viewModelScope.launch { repository.preferences.setCubeInspectionEnabled(v) }
    fun setKeepScreenAwake(v: Boolean) = viewModelScope.launch { repository.preferences.setKeepScreenAwake(v) }
    fun setSaveDiceHistory(v: Boolean) = viewModelScope.launch { repository.preferences.setSaveDiceHistory(v) }
    fun setDefaultScoreboardPreset(preset: String) = viewModelScope.launch { repository.preferences.setDefaultScoreboardPreset(preset) }
    fun setDefaultTeamNames(red: String, blue: String) = viewModelScope.launch { repository.preferences.setDefaultTeamNames(red, blue) }
    fun addRecentSearch(query: String) = viewModelScope.launch { repository.preferences.addRecentSearch(query) }
    fun clearRecentSearches() = viewModelScope.launch { repository.preferences.clearRecentSearches() }

    fun resetAllAppData() {
        viewModelScope.launch {
            repository.resetAllAppData()
            repository.ensureDefaultFavoritesIfEmpty()
        }
    }

    // Checklists / Presets
    fun observeProjects(type: String): Flow<List<ChecklistProjectEntity>> = repository.observeProjects(type)
    fun observeProjectItems(projectId: Long): Flow<List<ChecklistItemEntity>> = repository.observeProjectItems(projectId)

    fun createProject(type: String, name: String, subtitle: String = "", notes: String = "", onCreated: (Long) -> Unit = {}) {
        viewModelScope.launch {
            val id = repository.createProject(type, name, subtitle, notes)
            onCreated(id)
        }
    }

    fun updateProject(project: ChecklistProjectEntity) = viewModelScope.launch { repository.updateProject(project) }
    fun deleteProject(id: Long) = viewModelScope.launch { repository.deleteProject(id) }

    fun addProjectItem(
        projectId: Long,
        name: String,
        detail: String = "",
        currentCount: Int = 0,
        targetCount: Int = 1,
        isChecked: Boolean = false,
        sortOrder: Int = 0,
    ) = viewModelScope.launch {
        repository.addProjectItem(projectId, name, detail, currentCount, targetCount, isChecked, sortOrder)
    }

    fun updateProjectItem(item: ChecklistItemEntity) = viewModelScope.launch { repository.updateProjectItem(item) }
    fun deleteProjectItem(id: Long) = viewModelScope.launch { repository.deleteProjectItem(id) }

    fun feedbackClick() {
        val s = settings.value
        feedback.click(s.soundEnabled, s.hapticEnabled)
    }

    fun feedbackAccent() {
        val s = settings.value
        feedback.accentClick(s.soundEnabled, s.hapticEnabled)
    }

    fun feedbackBeep() {
        val s = settings.value
        feedback.shortBeep(s.soundEnabled, s.hapticEnabled)
    }

    fun feedbackAlert() {
        val s = settings.value
        feedback.alert(s.soundEnabled, s.hapticEnabled)
    }

    override fun onCleared() {
        super.onCleared()
        feedback.release()
    }
}
