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


enum class PantryStatus {
    FRESH,
    EXPIRING_SOON,
    EXPIRED
}

fun PantryItem.status(
    now: Long = System.currentTimeMillis(),
    expiringSoonThresholdDays: Int = 3
): PantryStatus {
    val nowDate = Date(now)

    return when {
        expiryDate.before(nowDate) -> PantryStatus.EXPIRED
        expiryDate.time - nowDate.time <= expiringSoonThresholdDays * 24L * 60 * 60 * 1000 ->
            PantryStatus.EXPIRING_SOON
        else -> PantryStatus.FRESH
    }
}
