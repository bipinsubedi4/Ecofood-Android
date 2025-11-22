package com.bipin080.ecofood.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bipin080.ecofood.ai.GeminiRecipeService
import com.bipin080.ecofood.data.GeneratedRecipe
import com.bipin080.ecofood.data.PantryItem
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

data class CookUiState(
    val foodName: String = "",
    val isLoading: Boolean = false,
    val error: String? = null
)

class CookViewModel(
    private val geminiService: GeminiRecipeService = GeminiRecipeService()
) : ViewModel() {

    private val _uiState = MutableStateFlow(CookUiState())
    val uiState: StateFlow<CookUiState> = _uiState

    fun onFoodNameChanged(newText: String) {
        _uiState.value = _uiState.value.copy(
            foodName = newText,
            error = null
        )
    }

    /**
     * Generate a recipe based on:
     * - dish name the user typed
     * - how many people they're cooking for
     * - what's in their pantry (for green/red ingredient flags)
     */
    fun generateRecipe(
        servings: Int,
        pantryItems: List<PantryItem>,
        onResult: (GeneratedRecipe) -> Unit
    ) {
        val dishName = _uiState.value.foodName.trim()
        if (dishName.isBlank()) {
            _uiState.value = _uiState.value.copy(
                error = "Please enter a food name first."
            )
            return
        }

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isLoading = true,
                error = null
            )

            val recipe = geminiService.generateRecipe(
                dishName = dishName,
                servings = servings,
                pantryItems = pantryItems
            )

            _uiState.value = _uiState.value.copy(isLoading = false)
            onResult(recipe)
        }
    }
}
