package com.bipin080.ecofood.data

data class PantryItem(
    val id: String,
    val name: String,
    val quantity: String,
    val location: String,      // pantry / fridge / freezer
    val daysToExpire: Int
)

data class RecipeCardModel(
    val id: String,
    val title: String,
    val uses: List<String>     // ingredients used
)

data class SharePost(
    val id: String,
    val title: String,
    val desc: String,
    val distanceKm: Double
)
