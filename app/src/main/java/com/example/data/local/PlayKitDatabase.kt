package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [
        HistoryRecordEntity::class,
        FavoriteToolEntity::class,
        RecentToolEntity::class,
        ChecklistProjectEntity::class,
        ChecklistItemEntity::class,
    ],
    version = 1,
    exportSchema = false,
)
abstract class PlayKitDatabase : RoomDatabase() {
    abstract fun playKitDao(): PlayKitDao

    companion object {
        @Volatile
        private var INSTANCE: PlayKitDatabase? = null

        fun getInstance(context: Context): PlayKitDatabase {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    PlayKitDatabase::class.java,
                    "mono_playkit.db",
                ).fallbackToDestructiveMigration(dropAllTables = true)
                    .build()
                    .also { INSTANCE = it }
            }
        }
    }
}
