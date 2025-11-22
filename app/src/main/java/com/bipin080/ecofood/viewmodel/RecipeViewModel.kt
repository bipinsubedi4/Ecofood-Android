package com.bipin080.ecofood.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.bipin080.ecofood.data.GeneratedRecipe
import com.bipin080.ecofood.data.SavedRecipeDatabase
import com.bipin080.ecofood.data.SavedRecipeEntity
import com.bipin080.ecofood.data.toGeneratedRecipe
import com.google.gson.Gson
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class RecipeViewModel(application: Application) : AndroidViewModel(application) {

    // --- DB + JSON helpers ---
    private val gson = Gson()
    private val dao = SavedRecipeDatabase
        .getInstance(application)
        .savedRecipeDao()

    // --- currently selected recipe (for RecipeScreen) ---
    private val _currentRecipe = MutableStateFlow<GeneratedRecipe?>(null)
    val currentRecipe: StateFlow<GeneratedRecipe?> = _currentRecipe.asStateFlow()

    fun setRecipe(recipe: GeneratedRecipe) {
        _currentRecipe.value = recipe
    }

    // --- saved recipes list backed by Room ---
    private val _savedRecipes = MutableStateFlow<List<GeneratedRecipe>>(emptyList())
    val savedRecipes: StateFlow<List<GeneratedRecipe>> = _savedRecipes.asStateFlow()

    init {
        // Whenever DB changes, update the UI list
        viewModelScope.launch {
            dao.getAll().collectLatest { entities ->
                _savedRecipes.value = entities.map { it.toGeneratedRecipe(gson) }
            }
        }
    }

    /** Save a recipe to "My Recipes" (persists in Room). */
    fun saveRecipe(recipe: GeneratedRecipe) {
        viewModelScope.launch {
            // Optional: avoid duplicate title + description
            val alreadySaved = _savedRecipes.value.any {
                it.title == recipe.title && it.description == recipe.description
            }
            if (alreadySaved) return@launch

            val entity = SavedRecipeEntity.fromGeneratedRecipe(recipe, gson)
            dao.insert(entity)
        }
    }

    /** Delete a recipe from "My Recipes" (removes from Room). */
    fun deleteRecipe(recipe: GeneratedRecipe) {
        viewModelScope.launch {
            // We don't know the id here, so use helper query
            dao.deleteByTitleAndDescription(
                title = recipe.title,
                description = recipe.description
            )
        }
    }
}
