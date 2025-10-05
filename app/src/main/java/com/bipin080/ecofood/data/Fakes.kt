package com.bipin080.ecofood.data

fun fakePantry() = listOf(
    PantryItem("1", "Milk 2L", "1", "fridge", 1),
    PantryItem("2", "Spinach", "200g", "fridge", 2),
    PantryItem("3", "Pasta", "500g", "pantry", 90),
    PantryItem("4", "Chicken thighs", "600g", "freezer", 14),
    PantryItem("5", "Tomatoes", "3", "fridge", 3),
)

fun fakeRecipes() = listOf(
    RecipeCardModel("r1", "Creamy Tomato Pasta", listOf("Pasta", "Tomatoes")),
    RecipeCardModel("r2", "Spinach Omelette", listOf("Spinach", "Eggs")),
    RecipeCardModel("r3", "Chicken Pasta Bake", listOf("Chicken", "Pasta")),
)

fun fakeSharePosts() = listOf(
    SharePost("s1", "Free Bananas (ripe)", "Pick up near Southbank", 1.2),
    SharePost("s2", "Half loaf sourdough", "Today only", 0.7),
    SharePost("s3", "Leftover curry", "Please bring container", 2.5),
)
