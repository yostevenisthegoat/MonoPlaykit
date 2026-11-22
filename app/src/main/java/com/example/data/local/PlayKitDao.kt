package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface PlayKitDao {
    // History
    @Query("SELECT * FROM history_records ORDER BY timestamp DESC")
    fun observeAllHistory(): Flow<List<HistoryRecordEntity>>

    @Query("SELECT * FROM history_records WHERE toolId = :toolId ORDER BY timestamp DESC")
    fun observeHistoryForTool(toolId: String): Flow<List<HistoryRecordEntity>>

    @Query("SELECT * FROM history_records ORDER BY timestamp DESC")
    suspend fun getAllHistoryOnce(): List<HistoryRecordEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHistory(record: HistoryRecordEntity): Long

    @Update
    suspend fun updateHistory(record: HistoryRecordEntity)

    @Query("DELETE FROM history_records WHERE id = :id")
    suspend fun deleteHistoryById(id: Long)

    @Query("DELETE FROM history_records WHERE toolId = :toolId")
    suspend fun deleteHistoryForTool(toolId: String)

    @Query("DELETE FROM history_records")
    suspend fun clearAllHistory()

    // Favorites
    @Query("SELECT * FROM favorite_tools ORDER BY sortOrder ASC, addedAt ASC")
    fun observeFavorites(): Flow<List<FavoriteToolEntity>>

    @Query("SELECT * FROM favorite_tools ORDER BY sortOrder ASC, addedAt ASC")
    suspend fun getFavoritesOnce(): List<FavoriteToolEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertFavorite(fav: FavoriteToolEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertFavorites(favs: List<FavoriteToolEntity>)

    @Query("DELETE FROM favorite_tools WHERE toolId = :toolId")
    suspend fun deleteFavorite(toolId: String)

    @Query("DELETE FROM favorite_tools")
    suspend fun clearFavorites()

    // Recents
    @Query("SELECT * FROM recent_tools ORDER BY lastOpenedAt DESC LIMIT 12")
    fun observeRecentTools(): Flow<List<RecentToolEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertRecentTool(recent: RecentToolEntity)

    @Query("DELETE FROM recent_tools")
    suspend fun clearRecentTools()

    // Checklist Projects & Items (LEGO, Model Kits, Custom Counters, Interval Presets)
    @Query("SELECT * FROM checklist_projects WHERE projectType = :type ORDER BY createdAt DESC")
    fun observeProjectsByType(type: String): Flow<List<ChecklistProjectEntity>>

    @Query("SELECT * FROM checklist_projects ORDER BY createdAt DESC")
    suspend fun getAllProjectsOnce(): List<ChecklistProjectEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProject(project: ChecklistProjectEntity): Long

    @Update
    suspend fun updateProject(project: ChecklistProjectEntity)

    @Query("DELETE FROM checklist_projects WHERE id = :id")
    suspend fun deleteProjectById(id: Long)

    @Query("DELETE FROM checklist_projects")
    suspend fun clearAllProjects()

    @Query("SELECT * FROM checklist_items WHERE projectId = :projectId ORDER BY sortOrder ASC, id ASC")
    fun observeItemsForProject(projectId: Long): Flow<List<ChecklistItemEntity>>

    @Query("SELECT * FROM checklist_items ORDER BY projectId ASC, sortOrder ASC, id ASC")
    suspend fun getAllItemsOnce(): List<ChecklistItemEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertItem(item: ChecklistItemEntity): Long

    @Update
    suspend fun updateItem(item: ChecklistItemEntity)

    @Query("DELETE FROM checklist_items WHERE id = :id")
    suspend fun deleteItemById(id: Long)
}
