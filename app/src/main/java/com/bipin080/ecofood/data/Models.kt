package com.bipin080.ecofood.data

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
