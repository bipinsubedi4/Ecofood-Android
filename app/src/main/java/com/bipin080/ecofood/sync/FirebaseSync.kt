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

/* ---------------------------------------------------------- */
/*  COMMON HELPER                                              */
/* ---------------------------------------------------------- */
private fun currentUserId(): String =
    FirebaseAuth.getInstance().currentUser?.uid ?: "local_user"


/* ---------------------------------------------------------- */
/*  PANTRY SYNC                                                */
/* ---------------------------------------------------------- */

object FirebasePantrySync {

    // Lazy = safe for tests
    private val firestore: FirebaseFirestore by lazy {
        FirebaseFirestore.getInstance()
    }

    fun start(pantryDao: PantryItemDao, scope: CoroutineScope) {
        val userId = currentUserId()
        val pantryCollection = firestore.collection("users")
            .document(userId)
            .collection("pantry")

        // --- Pull Firestore → Local Room ---
        pantryCollection
            .get()
            .addOnSuccessListener { snapshot ->
                scope.launch {
                    snapshot.documents
                        .mapNotNull { it.toPantryItemOrNull() }
                        .forEach { pantryDao.insert(it) }
                }
            }

        // --- Push Local Room → Firestore ---
        scope.launch {
            pantryDao.getAll().collect { items ->
                items.forEach { item ->
                    pantryCollection
                        .document(item.id.toString())
                        .set(item.toFirestoreData())
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


/* ---------------------------------------------------------- */
/*  MARKETPLACE SYNC                                           */
/* ---------------------------------------------------------- */

object FirebaseMarketplaceSync {

    /** Enables/disables sync in tests */
    @JvmStatic
    var ENABLED = true

    // Lazy = safe for tests
    private val firestore: FirebaseFirestore by lazy {
        FirebaseFirestore.getInstance()
    }

    fun start(marketplaceDao: MarketplaceItemDao, scope: CoroutineScope) {
        if (!ENABLED) return

        val userId = currentUserId()
        val marketplaceCollection = firestore.collection("users")
            .document(userId)
            .collection("marketplace")

        // --- Push Local Room → Firestore ---
        scope.launch {
            marketplaceDao.getAll().collect { items ->
                items.forEach { item ->
                    marketplaceCollection
                        .document(item.id.toString())
                        .set(item.toFirestoreData())
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
        // You currently don't use Marketplace pull logic
        return null
    }
}
