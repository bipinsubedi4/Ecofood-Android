package com.bipin080.ecofood.data

data class RecipeIngredient(
    val name: String,
    val quantity: String,
    val tags: List<String>,
    val inPantry: Boolean
)

data class GeneratedRecipe(
    val title: String,
    val description: String,
    val cookingTime: String,
    val servings: String,
    val wasteReduction: String,
    val calories: String,
    val ingredients: List<RecipeIngredient>
)
