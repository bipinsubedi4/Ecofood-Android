package com.bipin080.ecofood.ai

import com.bipin080.ecofood.BuildConfig
import com.bipin080.ecofood.data.GeneratedRecipe
import com.bipin080.ecofood.data.RecipeIngredient
import com.google.ai.client.generativeai.GenerativeModel
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json

class GeminiRecipeService {

    private val model = GenerativeModel(
        modelName = "gemini-flash-latest",           // ✅ current model
        apiKey = BuildConfig.GEMINI_API_KEY
    )

    private val json = Json {
        ignoreUnknownKeys = true
    }

    /**
     * @param ingredientsText text the user typed into the AI screen
     */
    suspend fun generateRecipe(
        ingredientsText: String
    ): GeneratedRecipe {
        val prompt = """
            You are an assistant helping people reduce household food waste.

            The user has these ingredients available (usually in their pantry/fridge):
            $ingredientsText

            You must respond with **ONLY valid JSON**, no markdown, no backticks, no comments.
            The JSON must match this Kotlin data model:

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

            Rules:
            - title: short, human-friendly dish name.
            - description: 2–4 sentences describing the dish and **difficulty level**.
            - cookingTime: e.g. "30 minutes".
            - servings: e.g. "2 servings".
            - wasteReduction: 1–2 sentences explaining how this uses up existing ingredients.
            - calories: approximate calories per serving as a string, e.g. "400 kcal".
            - ingredients: include ALL ingredients needed for the recipe.
              * name: simple name like "tomato", "olive oil", "rice".
              * quantity: human-readable, e.g. "2 medium tomatoes", "1 tbsp".
              * tags: e.g. ["vegetable"], ["protein"], ["spice"], ["oil"], ["grain"].
              * inPantry:
                  - true if this ingredient clearly comes from the user's list above
                  - false if this ingredient is an additional item they might need to buy.

            IMPORTANT:
            - Respond ONLY with a single JSON object that can be parsed as GeneratedRecipe.
            - Do NOT wrap the JSON in ```json``` or any other text.
        """.trimIndent()

        return try {
            val response = model.generateContent(prompt)
            val raw = response.text?.trim()
                ?: return fallbackRecipe("No response from AI.", ingredientsText)

            val baseRecipe = json.decodeFromString<GeneratedRecipe>(raw)

            // Just return what AI produced (it already set inPantry flags)
            baseRecipe
        } catch (e: Exception) {
            fallbackRecipe(
                "Error generating recipe: ${e.message ?: "Unknown error"}",
                ingredientsText
            )
        }
    }

    private fun fallbackRecipe(errorMessage: String, ingredientsText: String): GeneratedRecipe {
        return GeneratedRecipe(
            title = "AI Recipe",
            description = errorMessage,
            cookingTime = "Unknown",
            servings = "Unknown",
            wasteReduction = "Uses your available ingredients to reduce food waste.",
            calories = "Unknown",
            ingredients = listOf(
                RecipeIngredient(
                    name = ingredientsText.ifBlank { "Your ingredients" },
                    quantity = "",
                    tags = emptyList(),
                    inPantry = true
                )
            )
        )
    }
}
