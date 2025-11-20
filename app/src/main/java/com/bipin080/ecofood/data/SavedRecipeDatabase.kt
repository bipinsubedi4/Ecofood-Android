package com.bipin080.ecofood.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [SavedRecipeEntity::class],
    version = 2,          // 🔥 UPDATED version to avoid “migration required” crash
    exportSchema = false
)
abstract class SavedRecipeDatabase : RoomDatabase() {

    abstract fun savedRecipeDao(): SavedRecipeDao

    companion object {
        @Volatile
        private var INSTANCE: SavedRecipeDatabase? = null

        fun getInstance(context: Context): SavedRecipeDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    SavedRecipeDatabase::class.java,
                    "saved_recipes.db"
                )
                    .fallbackToDestructiveMigration()   // 🔥 prevents migration crash
                    .build()

                INSTANCE = instance
                instance
            }
        }
    }
}
