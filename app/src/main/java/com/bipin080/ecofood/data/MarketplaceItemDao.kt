package com.bipin080.ecofood.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface MarketplaceItemDao {

    @Query("SELECT * FROM marketplace_items")
    fun getAll(): Flow<List<MarketplaceItem>>

    @Insert
    suspend fun insert(item: MarketplaceItem)

    @Query("DELETE FROM marketplace_items")
    suspend fun deleteAll()

    @Query("SELECT * FROM marketplace_items WHERE sellerUid = :userId")
    fun getMyListings(userId: String): Flow<List<MarketplaceItem>>

    @Delete
    suspend fun delete(item: MarketplaceItem)
}
