package com.bipin080.ecofood.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface PantryItemDao {

    @Query("SELECT * FROM pantry_items ORDER BY expiryDate ASC")
    fun getAll(): Flow<List<PantryItem>>

    @Insert
    suspend fun insert(item: PantryItem)
}
