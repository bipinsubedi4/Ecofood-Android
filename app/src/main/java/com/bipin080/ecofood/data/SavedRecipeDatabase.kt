package com.bipin080.ecofood.data


import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [SavedRecipeEntity::class],
    version = 1,
    exportSchema = false
)
abstract class SavedRecipeDatabase : RoomDatabase() {

    abstract fun savedRecipeDao(): SavedRecipeDao

    companion object {
        @Volatile
        private var INSTANCE: SavedRecipeDatabase? = null

        fun getInstance(context: Context): SavedRecipeDatabase {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    SavedRecipeDatabase::class.java,
                    "saved_recipes_db"
                ).build().also { INSTANCE = it }
            }
        }
    }
}
