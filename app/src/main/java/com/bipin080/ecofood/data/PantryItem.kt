package com.bipin080.ecofood.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.Date
import java.util.UUID

@Entity(tableName = "pantry_items")
data class PantryItem(
    @PrimaryKey val id: UUID = UUID.randomUUID(),
    val name: String,
    val quantity: Int,
    val unit: String,
    val purchaseDate: Date,
    val expiryDate: Date
)
