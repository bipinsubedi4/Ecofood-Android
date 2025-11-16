package com.bipin080.ecofood.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.ServerTimestamp
import java.util.Date
import java.util.UUID

@Entity(tableName = "marketplace_items")
data class MarketplaceItem(
    @PrimaryKey val id: UUID = UUID.randomUUID(),
    val name: String = "",
    val quantity: String = "",
    val unit: String = "",
    val price: Double = 0.0,
    val expiryDate: Date? = null,
    val location: String = "",
    val description: String = "",
    val imageUrl: String? = null,
    
    // New fields for seller information
    val sellerUid: String = "",
    val sellerName: String = "",
    @ServerTimestamp
    val postedAt: Date? = null
)
