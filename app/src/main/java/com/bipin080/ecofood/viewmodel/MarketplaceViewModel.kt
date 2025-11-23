package com.bipin080.ecofood.viewmodel

import android.app.Application
import android.service.notification.Condition.newId
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.bipin080.ecofood.data.MarketplaceDatabase
import com.bipin080.ecofood.data.MarketplaceItem
import com.bipin080.ecofood.data.MarketplaceItemDao
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.Date
import com.bipin080.ecofood.sync.FirebaseMarketplaceSync
import java.util.UUID

class MarketplaceViewModel(
    application: Application) : AndroidViewModel(application) {

    companion object {
        const val LOCAL_USER_ID = "local_user"
        const val LOCAL_USER_NAME = "Local User"
    }
    private val dao: MarketplaceItemDao =
        MarketplaceDatabase.getDatabase(application).marketplaceItemDao()
    /** Marketplace feed = all items except user's own */
    val marketplaceItems = dao.getAll()
        .map { list -> list.filter { it.sellerUid != LOCAL_USER_ID } }
        .stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            emptyList()
        )

    /** My listings = only items created by local user */
    val myListings = dao.getMyListings(LOCAL_USER_ID)
        .stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            emptyList()
        )

    init{
        FirebaseMarketplaceSync.start(dao, viewModelScope)
    }

    /** Add an item */
    fun addItem(item: MarketplaceItem) {
        viewModelScope.launch {
            val newId = UUID.randomUUID().toString()
            val newItem = item.copy(
                id= newId,
                sellerUid = LOCAL_USER_ID,
                sellerName = LOCAL_USER_NAME,
                postedAt = Date()
            )
            dao.insert(newItem)
        }
    }

    /** Delete an item */
    fun deleteItem(item: MarketplaceItem) {
        viewModelScope.launch {
            dao.delete(item)
        }

        }

}
