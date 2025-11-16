package com.bipin080.ecofood.data


import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

@Entity(tableName = "saved_recipes")
data class SavedRecipeEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val description: String,
    val cookingTime: String,
    val servings: String,
    val wasteReduction: String,
    val calories: String,
    val ingredientsJson: String
) {
    companion object {
        fun fromGeneratedRecipe(recipe: GeneratedRecipe, gson: Gson): SavedRecipeEntity {
            val json = gson.toJson(recipe.ingredients)
            return SavedRecipeEntity(
                title = recipe.title,
                description = recipe.description,
                cookingTime = recipe.cookingTime,
                servings = recipe.servings,
                wasteReduction = recipe.wasteReduction,
                calories = recipe.calories,
                ingredientsJson = json
            )
        }
    }
}

fun SavedRecipeEntity.toGeneratedRecipe(gson: Gson): GeneratedRecipe {
    val type = object : TypeToken<List<RecipeIngredient>>() {}.type
    val ingredients: List<RecipeIngredient> = gson.fromJson(ingredientsJson, type)
    return GeneratedRecipe(
        title = title,
        description = description,
        cookingTime = cookingTime,
        servings = servings,
        wasteReduction = wasteReduction,
        calories = calories,
        ingredients = ingredients
    )
}
