package com.bipin080.ecofood.viewmodel

import androidx.lifecycle.ViewModel
import com.bipin080.ecofood.data.GeneratedRecipe
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class RecipeViewModel : ViewModel() {

    // currently selected recipe (for RecipeScreen)
    private val _currentRecipe = MutableStateFlow<GeneratedRecipe?>(null)
    val currentRecipe: StateFlow<GeneratedRecipe?> = _currentRecipe

    fun setRecipe(recipe: GeneratedRecipe) {
        _currentRecipe.value = recipe
    }

    // in-memory list of saved recipes ("My Recipes")
    private val _savedRecipes = MutableStateFlow<List<GeneratedRecipe>>(emptyList())
    val savedRecipes: StateFlow<List<GeneratedRecipe>> = _savedRecipes.asStateFlow()

    /** Save a recipe to "My Recipes". */
    fun saveRecipe(recipe: GeneratedRecipe) {
        _savedRecipes.update { current ->
            // avoid duplicates with same title + description
            if (current.any {
                    it.title == recipe.title && it.description == recipe.description
                }
            ) current else current + recipe
        }
    }

    /** Delete a recipe from "My Recipes". */
    fun deleteRecipe(recipe: GeneratedRecipe) {
        _savedRecipes.update { list -> list.filterNot { it == recipe } }
    }
}
