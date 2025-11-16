package com.bipin080.ecofood.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bipin080.ecofood.ai.GeminiRecipeService
import com.bipin080.ecofood.data.GeneratedRecipe
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

data class CookUiState(
    val ingredientsText: String = "",
    val isLoading: Boolean = false,
    val error: String? = null
)

class CookViewModel(
    private val geminiService: GeminiRecipeService = GeminiRecipeService()
) : ViewModel() {

    private val _uiState = MutableStateFlow(CookUiState())
    val uiState: StateFlow<CookUiState> = _uiState

    fun onIngredientsChanged(newText: String) {
        _uiState.value = _uiState.value.copy(
            ingredientsText = newText,
            error = null
        )
    }

    fun generateRecipe(onResult: (GeneratedRecipe) -> Unit) {
        val currentIngredients = _uiState.value.ingredientsText.trim()
        if (currentIngredients.isBlank()) {
            _uiState.value = _uiState.value.copy(
                error = "Please enter some ingredients first."
            )
            return
        }

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isLoading = true,
                error = null
            )

            val recipe = geminiService.generateRecipe(currentIngredients)

            _uiState.value = _uiState.value.copy(isLoading = false)
            onResult(recipe)
        }
    }
}
