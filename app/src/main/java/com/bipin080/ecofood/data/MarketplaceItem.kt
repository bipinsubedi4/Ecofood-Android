package com.bipin080.ecofood.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.Date
import java.util.UUID

@Entity(tableName = "marketplace_items")
data class MarketplaceItem(
    @PrimaryKey val id: UUID = UUID.randomUUID(),
    val name: String,
    val quantity: String,
    val unit: String,
    val price: Double,
    val expiryDate: Date,
    val location: String,
    val description: String,
    val sellerName: String,
    val contact: String,
    val imageUrl: String? = null // New field for the item image
)
