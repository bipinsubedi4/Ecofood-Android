package com.bipin080.ecofood.viewmodel

import androidx.lifecycle.ViewModel
import com.bipin080.ecofood.data.GeneratedRecipe
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class RecipeViewModel : ViewModel() {

    private val _recipe = MutableStateFlow<GeneratedRecipe?>(null)
    val recipe: StateFlow<GeneratedRecipe?> = _recipe

    fun setRecipe(recipe: GeneratedRecipe) {
        _recipe.value = recipe
    }
}
