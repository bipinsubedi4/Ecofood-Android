package com.bipin080.ecofood.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters

@Database(
    entities = [PantryItem::class],
    version = 2,                // 🔥 IMPORTANT: Updated database version
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class PantryDatabase : RoomDatabase() {

    abstract fun pantryItemDao(): PantryItemDao

    companion object {
        @Volatile
        private var INSTANCE: PantryDatabase? = null

        fun getDatabase(context: Context): PantryDatabase {
            return INSTANCE ?: synchronized(this) {
                Room.databaseBuilder(
                    context.applicationContext,
                    PantryDatabase::class.java,
                    "pantry.db"
                )
                    .fallbackToDestructiveMigration()   // 🔥 Prevents migration crash
                    .build()
                    .also { INSTANCE = it }
            }
        }
    }
}
