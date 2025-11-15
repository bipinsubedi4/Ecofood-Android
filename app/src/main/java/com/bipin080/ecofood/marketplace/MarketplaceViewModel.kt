package com.bipin080.ecofood.marketplace

import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

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
            .orderBy("timestamp")
            .addSnapshotListener { snapshot, e ->
                if (e != null) {
                    _uiState.value = MarketplaceUiState(
                        items = emptyList(),
                        isLoading = false,
                        error = e.message ?: "Failed to load marketplace"
                    )
                    return@addSnapshotListener
                }

                val list = snapshot?.documents
                    ?.mapNotNull { doc ->
                        doc.toObject(MarketplaceItem::class.java)
                            ?.copy(id = doc.id)
                    }
                    .orEmpty()

                _uiState.value = MarketplaceUiState(
                    items = list,
                    isLoading = false,
                    error = null
                )
            }
    }

    fun addItem(
        title: String,
        description: String,
        quantity: String,
        expiryDate: String,
        onResult: (Boolean, String?) -> Unit
    ) {
        val user = auth.currentUser
        if (user == null) {
            onResult(false, "You must be logged in to post")
            return
        }

        val sellerName = user.displayName ?: user.email ?: "Unknown"

        val item = MarketplaceItem(
            title = title,
            description = description,
            quantity = quantity,
            expiryDate = expiryDate,
            sellerUid = user.uid,
            sellerName = sellerName,
            contactEmail = user.email ?: ""
        )

        firestore.collection("marketplace")
            .add(item)
            .addOnSuccessListener {
                onResult(true, null)
            }
            .addOnFailureListener { e ->
                onResult(false, e.message ?: "Failed to post item")
            }
    }

    override fun onCleared() {
        super.onCleared()
        listenerRegistration?.remove()
    }
}
