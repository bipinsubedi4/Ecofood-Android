package com.bipin080.ecofood.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.bipin080.ecofood.data.MarketplaceDatabase
import com.bipin080.ecofood.data.MarketplaceItem
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

@OptIn(ExperimentalCoroutinesApi::class)
class MarketplaceViewModel(application: Application) : AndroidViewModel(application) {

    private val auth = FirebaseAuth.getInstance()
    private val marketplaceDao = MarketplaceDatabase.getDatabase(application, viewModelScope).marketplaceItemDao()

    // A robust flow that emits the current user whenever the auth state changes.
    private val currentUserFlow: StateFlow<FirebaseUser?> = callbackFlow<FirebaseUser?> {
        val listener = FirebaseAuth.AuthStateListener { auth ->
            trySend(auth.currentUser).isSuccess
        }
        auth.addAuthStateListener(listener)
        awaitClose { auth.removeAuthStateListener(listener) }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), auth.currentUser)

    val marketplaceItems: StateFlow<List<MarketplaceItem>> = marketplaceDao.getAll()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
        
    // This flow is now guaranteed to be reactive to the user's auth state for reading data.
    val myListings: StateFlow<List<MarketplaceItem>> = currentUserFlow.flatMapLatest { user ->
        if (user == null) {
            flowOf(emptyList())
        } else {
            marketplaceDao.getMyListings(user.uid)
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun addItem(item: MarketplaceItem) {
        viewModelScope.launch {
            // Get the user directly from the source of truth at the moment of creation.
            // This is the most reliable way to handle the write operation.
            val user = auth.currentUser
            if (user != null) {
                val newItem = item.copy(
                    sellerUid = user.uid,
                    sellerName = user.displayName ?: "Anonymous",
                )
                marketplaceDao.insert(newItem)
            }
        }
    }
}
