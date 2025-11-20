package com.bipin080.ecofood.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow
import java.util.UUID

@Dao
interface PantryItemDao {

    @Query("SELECT * FROM pantry_items ORDER BY expiryDate ASC")
    fun getAll(): Flow<List<PantryItem>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(item: PantryItem)

    @Delete
    suspend fun delete(item: PantryItem)
}
