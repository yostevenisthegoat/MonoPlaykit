package com.example.data.local

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(tableName = "history_records")
data class HistoryRecordEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val toolId: String,
    val categoryId: String,
    val title: String,
    val summary: String,
    val detailText: String = "",
    val primaryValue: String = "",
    val numericValueMs: Long = 0L,
    val penaltyFlag: String = "", // "", "+2", "DNF" for cube solves
    val timestamp: Long = System.currentTimeMillis(),
)

@Entity(tableName = "favorite_tools")
data class FavoriteToolEntity(
    @PrimaryKey val toolId: String,
    val sortOrder: Int,
    val addedAt: Long = System.currentTimeMillis(),
)

@Entity(tableName = "recent_tools")
data class RecentToolEntity(
    @PrimaryKey val toolId: String,
    val lastOpenedAt: Long = System.currentTimeMillis(),
)

@Entity(tableName = "checklist_projects")
data class ChecklistProjectEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val projectType: String, // "LEGO", "MODEL_KIT", "COUNTER_PRESET", "INTERVAL_PRESET"
    val name: String,
    val subtitle: String = "",
    val notes: String = "",
    val createdAt: Long = System.currentTimeMillis(),
)

@Entity(
    tableName = "checklist_items",
    foreignKeys = [
        ForeignKey(
            entity = ChecklistProjectEntity::class,
            parentColumns = ["id"],
            childColumns = ["projectId"],
            onDelete = ForeignKey.CASCADE,
        )
    ],
    indices = [Index("projectId")],
)
data class ChecklistItemEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val projectId: Long,
    val name: String,
    val detail: String = "",
    val currentCount: Int = 0,
    val targetCount: Int = 1,
    val isChecked: Boolean = false,
    val sortOrder: Int = 0,
)
