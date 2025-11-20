package com.bipin080.ecofood.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.bipin080.ecofood.data.MarketplaceDatabase
import com.bipin080.ecofood.data.MarketplaceItem
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.Date

class MarketplaceViewModel(application: Application) : AndroidViewModel(application) {

    companion object {
        // Temporary local-only identity
        const val LOCAL_USER_ID = "local_user"
        const val LOCAL_USER_NAME = "Local User"
    }

    private val marketplaceDao =
        MarketplaceDatabase.getDatabase(application).marketplaceItemDao()


    /**
     * MAIN MARKETPLACE:
     * - Start from all items in Room
     * - Filter out the ones created by the local user
     */
    val marketplaceItems: StateFlow<List<MarketplaceItem>> =
        marketplaceDao.getAll()
            .map { items -> items.filter { it.sellerUid != LOCAL_USER_ID } }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    /**
     * MY LISTINGS:
     * - Only items where sellerUid == LOCAL_USER_ID
     */
    val myListings: StateFlow<List<MarketplaceItem>> =
        marketplaceDao.getMyListings(LOCAL_USER_ID)
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    /**
     * Insert new listing into LOCAL Room DB,
     * tagging it as created by this user.
     */
    fun addItem(item: MarketplaceItem) {
        viewModelScope.launch {
            val newItem = item.copy(
                sellerUid = LOCAL_USER_ID,
                sellerName = LOCAL_USER_NAME,
                postedAt = Date()
            )
            marketplaceDao.insert(newItem)
        }
    }

    /**
     * Delete one listing.
     * This will automatically update both marketplaceItems and myListings flows.
     */
    fun deleteItem(item: MarketplaceItem) {
        viewModelScope.launch {
            marketplaceDao.delete(item)
        }
    }
}
