package com.bipin080.ecofood.sync

import com.bipin080.ecofood.data.PantryItem
import com.bipin080.ecofood.data.PantryItemDao
import com.bipin080.ecofood.data.MarketplaceItem
import com.bipin080.ecofood.data.MarketplaceItemDao
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.collect
import java.util.Date
import java.util.UUID

/**
 * Common helper to get current user ID.
 * Falls back to "local_user" if not logged in.
 */
private fun currentUserId(): String =
    FirebaseAuth.getInstance().currentUser?.uid ?: "local_user"

/* ----------------------- PANTRY SYNC ----------------------- */

object FirebasePantrySync {

    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()

    fun start(pantryDao: PantryItemDao, scope: CoroutineScope) {
        val userId = currentUserId()
        val pantryCollection = firestore.collection("users")
            .document(userId)
            .collection("pantry")

        // 1) One-time pull from Firestore -> Room
        pantryCollection
            .get()
            .addOnSuccessListener { snapshot ->
                scope.launch {
                    snapshot.documents
                        .mapNotNull { it.toPantryItemOrNull() }
                        .forEach { pantryDao.insert(it) }
                }
            }

        // 2) Continuous push from Room -> Firestore
        scope.launch {
            pantryDao.getAll().collect { items ->
                items.forEach { item ->
                    val data = item.toFirestoreData()
                    pantryCollection
                        .document(item.id.toString())
                        .set(data)
                }
            }
        }
    }

    private fun PantryItem.toFirestoreData(): Map<String, Any?> = mapOf(
        "name" to name,
        "quantity" to quantity,
        "unit" to unit,
        "purchaseDate" to purchaseDate,
        "expiryDate" to expiryDate
    )

    private fun DocumentSnapshot.toPantryItemOrNull(): PantryItem? {
        val uuid = try {
            UUID.fromString(id)
        } catch (e: IllegalArgumentException) {
            return null
        }

        val name = getString("name") ?: return null
        val quantity = getLong("quantity")?.toInt() ?: 0
        val unit = getString("unit") ?: ""
        val purchaseDate = getDate("purchaseDate") ?: Date()
        val expiryDate = getDate("expiryDate") ?: Date()

        return PantryItem(
            id = uuid,
            name = name,
            quantity = quantity,
            unit = unit,
            purchaseDate = purchaseDate,
            expiryDate = expiryDate
        )
    }
}

/* -------------------- MARKETPLACE SYNC --------------------- */

object FirebaseMarketplaceSync {

    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()

    fun start(marketplaceDao: MarketplaceItemDao, scope: CoroutineScope) {
        val userId = currentUserId()
        val marketplaceCollection = firestore.collection("users")
            .document(userId)
            .collection("marketplace")

        // 1) One-time pull from Firestore -> Room
        marketplaceCollection
            .get()
            .addOnSuccessListener { snapshot ->
                scope.launch {
                    snapshot.documents
                        .mapNotNull { it.toMarketplaceItemOrNull() }
                        .forEach { marketplaceDao.insert(it) }
                }
            }

        // 2) Continuous push from Room -> Firestore
        scope.launch {
            marketplaceDao.getAll().collect { items ->
                items.forEach { item ->
                    val data = item.toFirestoreData()
                    marketplaceCollection
                        .document(item.id.toString())
                        .set(data)
                }
            }
        }
    }

    private fun MarketplaceItem.toFirestoreData(): Map<String, Any?> = mapOf(
        "name" to name,
        "quantity" to quantity,
        "unit" to unit,
        "price" to price,
        "expiryDate" to expiryDate,
        "location" to location,
        "description" to description,
        "imageUrl" to imageUrl,
        "sellerUid" to sellerUid,
        "sellerName" to sellerName,
        "postedAt" to postedAt
    )

    private fun DocumentSnapshot.toMarketplaceItemOrNull(): MarketplaceItem? {
        val uuid = try {
            UUID.fromString(id)
        } catch (e: IllegalArgumentException) {
            return null
        }

        val name = getString("name") ?: return null
        val quantity = getLong("quantity")?.toInt() ?: 0
        val unit = getString("unit") ?: ""
        val price = getDouble("price") ?: 0.0
        val expiryDate = getString("expiryDate")
        val location = getString("location") ?: ""
        val description = getString("description") ?: ""
        val imageUrl = getString("imageUrl")
        val sellerUid = getString("sellerUid") ?: ""
        val sellerName = getString("sellerName") ?: ""
        val postedAt = getDate("postedAt")

        return MarketplaceItem(
            id = uuid,
            name = name,
            quantity = quantity,
            unit = unit,
            price = price,
            expiryDate = expiryDate,
            location = location,
            description = description,
            imageUrl = imageUrl,
            sellerUid = sellerUid,
            sellerName = sellerName,
            postedAt = postedAt
        )
    }
}
