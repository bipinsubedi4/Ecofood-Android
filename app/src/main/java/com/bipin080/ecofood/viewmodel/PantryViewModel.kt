package com.bipin080.ecofood.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.bipin080.ecofood.data.PantryDatabase
import com.bipin080.ecofood.data.PantryItem
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class PantryViewModel(application: Application) : AndroidViewModel(application) {

    private val dao = PantryDatabase.getDatabase(application).pantryItemDao()

    // Live list of pantry items
    val pantryItems = dao.getAll()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    fun addItem(item: PantryItem) {
        viewModelScope.launch {
            dao.insert(item)
        }
    }

    fun deleteItem(item: PantryItem) {
        viewModelScope.launch {
            dao.delete(item)
        }
    }
}
