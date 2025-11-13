package com.bipin080.ecofood.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(tableName = "marketplace_items")
data class MarketplaceItem(
    @PrimaryKey val id: UUID = UUID.randomUUID(),
    val name: String,
    val description: String,
    val location: String,
    val contact: String
)
