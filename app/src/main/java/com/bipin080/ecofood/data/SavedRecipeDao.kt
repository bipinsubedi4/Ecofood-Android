package com.bipin080.ecofood.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface SavedRecipeDao {

    @Query("SELECT * FROM saved_recipes ORDER BY id DESC")
    fun getAll(): Flow<List<SavedRecipeEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(recipe: SavedRecipeEntity)

    @Delete
    suspend fun delete(recipe: SavedRecipeEntity)

    @Query("DELETE FROM saved_recipes")
    suspend fun clear()
}
