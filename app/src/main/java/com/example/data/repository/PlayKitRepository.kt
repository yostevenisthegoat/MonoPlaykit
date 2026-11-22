package com.example.data.repository

import android.content.Context
import com.example.data.local.ChecklistItemEntity
import com.example.data.local.ChecklistProjectEntity
import com.example.data.local.FavoriteToolEntity
import com.example.data.local.HistoryRecordEntity
import com.example.data.local.PlayKitDao
import com.example.data.local.PlayKitDatabase
import com.example.data.local.RecentToolEntity
import com.example.data.model.UtilityCatalog
import com.example.data.model.UtilityIds
import com.example.data.prefs.PlayKitPreferences
import com.example.data.prefs.PlayKitSettings
import com.example.ui.theme.ThemeMode
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import org.json.JSONArray
import org.json.JSONObject

class PlayKitRepository(
    private val dao: PlayKitDao,
    val preferences: PlayKitPreferences,
) {
    val settings: Flow<PlayKitSettings> = preferences.settings
    val allHistory: Flow<List<HistoryRecordEntity>> = dao.observeAllHistory()
    val favorites: Flow<List<FavoriteToolEntity>> = dao.observeFavorites()
    val recentTools: Flow<List<RecentToolEntity>> = dao.observeRecentTools()

    fun observeHistoryForTool(toolId: String): Flow<List<HistoryRecordEntity>> =
        dao.observeHistoryForTool(toolId)

    fun observeFavoriteIds(): Flow<Set<String>> =
        favorites.map { list -> list.map { it.toolId }.toSet() }

    fun observeProjects(type: String): Flow<List<ChecklistProjectEntity>> =
        dao.observeProjectsByType(type)

    fun observeProjectItems(projectId: Long): Flow<List<ChecklistItemEntity>> =
        dao.observeItemsForProject(projectId)

    suspend fun ensureDefaultFavoritesIfEmpty() {
        val current = dao.getFavoritesOnce()
        if (current.isEmpty()) {
            val defaults = listOf(
                FavoriteToolEntity(UtilityIds.SCOREBOARD, 0),
                FavoriteToolEntity(UtilityIds.CHESS_CLOCK, 1),
                FavoriteToolEntity(UtilityIds.DICE_ROLLER, 2),
                FavoriteToolEntity(UtilityIds.CUBE_TIMER, 3),
            )
            dao.upsertFavorites(defaults)
        }
    }

    suspend fun markToolOpened(toolId: String) {
        dao.upsertRecentTool(RecentToolEntity(toolId = toolId, lastOpenedAt = System.currentTimeMillis()))
    }

    suspend fun toggleFavorite(toolId: String) {
        val current = dao.getFavoritesOnce()
        if (current.any { it.toolId == toolId }) {
            dao.deleteFavorite(toolId)
        } else {
            val nextOrder = (current.maxOfOrNull { it.sortOrder } ?: -1) + 1
            dao.upsertFavorite(FavoriteToolEntity(toolId = toolId, sortOrder = nextOrder))
        }
    }

    suspend fun moveFavorite(toolId: String, direction: Int) {
        val current = dao.getFavoritesOnce().toMutableList()
        val index = current.indexOfFirst { it.toolId == toolId }
        if (index == -1) return
        val target = (index + direction).coerceIn(0, current.lastIndex)
        if (target == index) return
        val item = current.removeAt(index)
        current.add(target, item)
        val reindexed = current.mapIndexed { idx, fav -> fav.copy(sortOrder = idx) }
        dao.upsertFavorites(reindexed)
    }

    suspend fun logHistory(
        toolId: String,
        title: String,
        summary: String,
        detailText: String = "",
        primaryValue: String = "",
        numericValueMs: Long = 0L,
        penaltyFlag: String = "",
    ): Long {
        val categoryId = UtilityCatalog.findById(toolId)?.category?.id ?: "general"
        return dao.insertHistory(
            HistoryRecordEntity(
                toolId = toolId,
                categoryId = categoryId,
                title = title,
                summary = summary,
                detailText = detailText,
                primaryValue = primaryValue,
                numericValueMs = numericValueMs,
                penaltyFlag = penaltyFlag,
            )
        )
    }

    suspend fun updateHistoryRecord(record: HistoryRecordEntity) = dao.updateHistory(record)
    suspend fun deleteHistoryRecord(id: Long) = dao.deleteHistoryById(id)
    suspend fun deleteHistoryForTool(toolId: String) = dao.deleteHistoryForTool(toolId)
    suspend fun clearAllHistory() = dao.clearAllHistory()

    suspend fun createProject(
        type: String,
        name: String,
        subtitle: String = "",
        notes: String = "",
    ): Long = dao.insertProject(
        ChecklistProjectEntity(
            projectType = type,
            name = name,
            subtitle = subtitle,
            notes = notes,
        )
    )

    suspend fun updateProject(project: ChecklistProjectEntity) = dao.updateProject(project)
    suspend fun deleteProject(id: Long) = dao.deleteProjectById(id)

    suspend fun addProjectItem(
        projectId: Long,
        name: String,
        detail: String = "",
        currentCount: Int = 0,
        targetCount: Int = 1,
        isChecked: Boolean = false,
        sortOrder: Int = 0,
    ): Long = dao.insertItem(
        ChecklistItemEntity(
            projectId = projectId,
            name = name,
            detail = detail,
            currentCount = currentCount,
            targetCount = targetCount,
            isChecked = isChecked,
            sortOrder = sortOrder,
        )
    )

    suspend fun updateProjectItem(item: ChecklistItemEntity) = dao.updateItem(item)
    suspend fun deleteProjectItem(id: Long) = dao.deleteItemById(id)

    suspend fun resetAllAppData() {
        dao.clearAllHistory()
        dao.clearFavorites()
        dao.clearRecentTools()
        dao.clearAllProjects()
        preferences.resetAll()
    }

    suspend fun exportBackupJson(): String {
        val prefs = settings.first()
        val history = dao.getAllHistoryOnce()
        val favs = dao.getFavoritesOnce()
        val projects = dao.getAllProjectsOnce()
        val items = dao.getAllItemsOnce()

        val root = JSONObject().apply {
            put("app", "Mono PlayKit")
            put("version", 1)
            put("exportedAt", System.currentTimeMillis())

            put("settings", JSONObject().apply {
                put("themeMode", prefs.themeMode.name)
                put("soundEnabled", prefs.soundEnabled)
                put("hapticEnabled", prefs.hapticEnabled)
                put("cubeInspectionEnabled", prefs.cubeInspectionEnabled)
                put("keepScreenAwake", prefs.keepScreenAwake)
                put("saveDiceHistory", prefs.saveDiceHistory)
                put("defaultScoreboardPreset", prefs.defaultScoreboardPreset)
                put("defaultRedTeamName", prefs.defaultRedTeamName)
                put("defaultBlueTeamName", prefs.defaultBlueTeamName)
            })

            put("favorites", JSONArray().apply {
                favs.forEach { f ->
                    put(JSONObject().apply {
                        put("toolId", f.toolId)
                        put("sortOrder", f.sortOrder)
                        put("addedAt", f.addedAt)
                    })
                }
            })

            put("history", JSONArray().apply {
                history.forEach { h ->
                    put(JSONObject().apply {
                        put("toolId", h.toolId)
                        put("categoryId", h.categoryId)
                        put("title", h.title)
                        put("summary", h.summary)
                        put("detailText", h.detailText)
                        put("primaryValue", h.primaryValue)
                        put("numericValueMs", h.numericValueMs)
                        put("penaltyFlag", h.penaltyFlag)
                        put("timestamp", h.timestamp)
                    })
                }
            })

            put("projects", JSONArray().apply {
                projects.forEach { p ->
                    put(JSONObject().apply {
                        put("oldId", p.id)
                        put("projectType", p.projectType)
                        put("name", p.name)
                        put("subtitle", p.subtitle)
                        put("notes", p.notes)
                        put("createdAt", p.createdAt)
                    })
                }
            })

            put("projectItems", JSONArray().apply {
                items.forEach { item ->
                    put(JSONObject().apply {
                        put("oldProjectId", item.projectId)
                        put("name", item.name)
                        put("detail", item.detail)
                        put("currentCount", item.currentCount)
                        put("targetCount", item.targetCount)
                        put("isChecked", item.isChecked)
                        put("sortOrder", item.sortOrder)
                    })
                }
            })
        }
        return root.toString(2)
    }

    suspend fun importBackupJson(jsonString: String): Result<Int> = runCatching {
        val root = JSONObject(jsonString)
        require(root.optString("app") == "Mono PlayKit") { "Invalid Mono PlayKit backup file." }

        var importedCount = 0

        root.optJSONObject("settings")?.let { s ->
            val mode = ThemeMode.entries.find { it.name == s.optString("themeMode") } ?: ThemeMode.DARK
            preferences.setThemeMode(mode)
            preferences.setSoundEnabled(s.optBoolean("soundEnabled", true))
            preferences.setHapticEnabled(s.optBoolean("hapticEnabled", true))
            preferences.setCubeInspectionEnabled(s.optBoolean("cubeInspectionEnabled", true))
            preferences.setKeepScreenAwake(s.optBoolean("keepScreenAwake", true))
            preferences.setSaveDiceHistory(s.optBoolean("saveDiceHistory", true))
            preferences.setDefaultScoreboardPreset(s.optString("defaultScoreboardPreset", "CUSTOM"))
            preferences.setDefaultTeamNames(
                s.optString("defaultRedTeamName", "Red Team"),
                s.optString("defaultBlueTeamName", "Blue Team"),
            )
        }

        root.optJSONArray("favorites")?.let { favArr ->
            val list = buildList {
                for (i in 0 until favArr.length()) {
                    val obj = favArr.getJSONObject(i)
                    add(
                        FavoriteToolEntity(
                            toolId = obj.getString("toolId"),
                            sortOrder = obj.optInt("sortOrder", i),
                            addedAt = obj.optLong("addedAt", System.currentTimeMillis()),
                        )
                    )
                }
            }
            if (list.isNotEmpty()) {
                dao.clearFavorites()
                dao.upsertFavorites(list)
                importedCount += list.size
            }
        }

        root.optJSONArray("history")?.let { histArr ->
            for (i in 0 until histArr.length()) {
                val obj = histArr.getJSONObject(i)
                dao.insertHistory(
                    HistoryRecordEntity(
                        toolId = obj.getString("toolId"),
                        categoryId = obj.optString("categoryId", "general"),
                        title = obj.optString("title", ""),
                        summary = obj.optString("summary", ""),
                        detailText = obj.optString("detailText", ""),
                        primaryValue = obj.optString("primaryValue", ""),
                        numericValueMs = obj.optLong("numericValueMs", 0L),
                        penaltyFlag = obj.optString("penaltyFlag", ""),
                        timestamp = obj.optLong("timestamp", System.currentTimeMillis()),
                    )
                )
                importedCount++
            }
        }

        val idMap = mutableMapOf<Long, Long>()
        root.optJSONArray("projects")?.let { projArr ->
            for (i in 0 until projArr.length()) {
                val obj = projArr.getJSONObject(i)
                val oldId = obj.optLong("oldId", -1L)
                val newId = dao.insertProject(
                    ChecklistProjectEntity(
                        projectType = obj.getString("projectType"),
                        name = obj.getString("name"),
                        subtitle = obj.optString("subtitle", ""),
                        notes = obj.optString("notes", ""),
                        createdAt = obj.optLong("createdAt", System.currentTimeMillis()),
                    )
                )
                if (oldId >= 0) idMap[oldId] = newId
                importedCount++
            }
        }

        root.optJSONArray("projectItems")?.let { itemsArr ->
            for (i in 0 until itemsArr.length()) {
                val obj = itemsArr.getJSONObject(i)
                val newProjectId = idMap[obj.optLong("oldProjectId", -1L)] ?: continue
                dao.insertItem(
                    ChecklistItemEntity(
                        projectId = newProjectId,
                        name = obj.optString("name", ""),
                        detail = obj.optString("detail", ""),
                        currentCount = obj.optInt("currentCount", 0),
                        targetCount = obj.optInt("targetCount", 1),
                        isChecked = obj.optBoolean("isChecked", false),
                        sortOrder = obj.optInt("sortOrder", i),
                    )
                )
            }
        }

        importedCount
    }

    companion object {
        @Volatile
        private var INSTANCE: PlayKitRepository? = null

        fun getInstance(context: Context): PlayKitRepository {
            return INSTANCE ?: synchronized(this) {
                val db = PlayKitDatabase.getInstance(context)
                val prefs = PlayKitPreferences(context.applicationContext)
                INSTANCE ?: PlayKitRepository(db.playKitDao(), prefs).also { INSTANCE = it }
            }
        }
    }
}
