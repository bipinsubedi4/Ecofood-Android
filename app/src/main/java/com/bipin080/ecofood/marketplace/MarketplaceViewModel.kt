package com.bipin080.ecofood.marketplace

import androidx.lifecycle.ViewModel
import com.bipin080.ecofood.data.MarketplaceItem
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import java.util.UUID

data class MarketplaceUiState(
    val items: List<MarketplaceItem> = emptyList(),
    val isLoading: Boolean = true,
    val error: String? = null
)

class MarketplaceViewModel(
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance(),
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
) : ViewModel() {

    private val _uiState = MutableStateFlow(MarketplaceUiState())
    val uiState: StateFlow<MarketplaceUiState> = _uiState

    private var listenerRegistration: ListenerRegistration? = null

    init {
        observeMarketplace()
    }

    private fun observeMarketplace() {
        listenerRegistration?.remove()

        listenerRegistration = firestore.collection("marketplace")
            .orderBy("postedAt")
            .addSnapshotListener { snapshot, e ->
                if (e != null) {
                    _uiState.value = MarketplaceUiState(
                        isLoading = false,
                        error = e.message
                    )
                    return@addSnapshotListener
                }

                val list = snapshot?.documents?.mapNotNull { doc ->
                    doc.toObject(MarketplaceItem::class.java)
                }.orEmpty()

                _uiState.value = MarketplaceUiState(
                    items = list,
                    isLoading = false
                )
            }
    }

    // FIXED FUNCTION — matches MarketplaceItem data class
    fun addItem(
        name: String,
        quantity: Int,
        unit: String,
        price: Double,
        expiryDate: String?,
        location: String,
        description: String,
        imageUrl: String? = null,
        onResult: (Boolean, String?) -> Unit = { _, _ -> }
    ) {
        val user = auth.currentUser
        if (user == null) {
            onResult(false, "You must be logged in")
            return
        }

        val item = MarketplaceItem(
            id = UUID.randomUUID(),
            name = name,
            quantity = quantity,
            unit = unit,
            price = price,
            expiryDate = expiryDate,
            location = location,
            description = description,
            imageUrl = imageUrl,
            sellerUid = user.uid,
            sellerName = user.displayName ?: user.email ?: "Unknown"
        )

        firestore.collection("marketplace")
            .document(item.id.toString())
            .set(item)
            .addOnSuccessListener { onResult(true, null) }
            .addOnFailureListener { e -> onResult(false, e.message) }
    }

    override fun onCleared() {
        listenerRegistration?.remove()
        super.onCleared()
    }
}
