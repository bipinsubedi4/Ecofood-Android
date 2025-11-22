package com.bipin080.ecofood.ai

import com.bipin080.ecofood.BuildConfig
import com.bipin080.ecofood.data.GeneratedRecipe
import com.bipin080.ecofood.data.PantryItem
import com.bipin080.ecofood.data.RecipeIngredient
import com.google.ai.client.generativeai.GenerativeModel
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import kotlin.math.abs

class GeminiRecipeService {

    private val model = GenerativeModel(
        modelName = "gemini-flash-latest",
        apiKey = BuildConfig.GEMINI_API_KEY
    )

    private val json = Json {
        ignoreUnknownKeys = true
    }

    /**
     * Generate a recipe for a specific dish name and servings.
     *
     * - `dishName` = what the user typed (e.g., "Mutton Curry")
     * - `servings` = how many people they are cooking for
     * - `pantryItems` = used to mark ingredients as inPantry (green) or not (red)
     */
    suspend fun generateRecipe(
        dishName: String,
        servings: Int,
        pantryItems: List<PantryItem>
    ): GeneratedRecipe {
        val pantryText = if (pantryItems.isEmpty()) {
            "The user's pantry is currently empty."
        } else {
            pantryItems.joinToString(", ") { it.name }
        }

        val prompt = """
            You are an assistant helping people reduce household food waste.

            The user wants to cook this dish:
            "$dishName"

            These are the ingredients they currently have in their pantry or fridge:
            $pantryText

            You must respond with ONLY valid JSON (no markdown, no backticks).
            The JSON must match these Kotlin data classes exactly:

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

            - title:
              * Short, human-friendly dish name (usually keep the original name of the dish).
            - description:
              * 3–6 sentences.
              * MUST explain traditional origin and history of the dish (region, culture, occasions).
              * Briefly describe flavour profile.
              * Mention approximate difficulty (easy/medium/hard).
            - cookingTime:
              * Total time like "45 minutes".
            - servings:
              * Set this field to the STRING "2". Always assume the BASE recipe is for 2 servings.
              * The app will scale the ingredients to the user’s actual serving count.
            - wasteReduction:
              * 1–2 sentences of eco tips: how this recipe reduces food waste / uses leftovers / saves energy.
            - calories:
              * Approximate calories PER SERVING, as a string like "450 kcal".
            - ingredients:
              * Include ALL ingredients required for the recipe.
              * name: very simple like "mutton", "onion", "garam masala", "rice".
              * quantity:
                  - Start with a numeric value when possible.
                  - Example formats: "2 tomatoes", "1.5 cups rice", "0.5 kg mutton".
              * tags: e.g. ["protein"], ["vegetable"], ["spice"], ["grain"], ["oil"].
              * inPantry:
                  - true if this ingredient clearly appears in the pantry list text above.
                  - false if it is something they probably need to buy.

            Very important:
            - Generate the recipe assuming it serves EXACTLY 2 people and set servings = "2".
            - The app will automatically scale the ingredient quantities to the user's requested servings.
            - Respond ONLY with a single JSON object that can be parsed as GeneratedRecipe.
            - Do NOT add explanations, markdown, or backticks.
        """.trimIndent()

        return try {
            val response = model.generateContent(prompt)
            val raw = response.text?.trim()
                ?: return fallbackRecipe(
                    errorMessage = "No response from AI.",
                    dishName = dishName,
                    servings = servings,
                    pantryItems = pantryItems
                )

            val baseRecipe = json.decodeFromString<GeneratedRecipe>(raw)

            val pantryNames = pantryItems.map { it.name.lowercase() }

            // Base servings from AI (should be "2", but we guard it)
            val baseServings = baseRecipe.servings.filter { it.isDigit() }.toIntOrNull() ?: 2
            val scaleFactor = if (baseServings > 0) {
                servings.toDouble() / baseServings.toDouble()
            } else {
                1.0
            }

            val scaledIngredients = baseRecipe.ingredients.map { ingredient ->
                val ingNameLower = ingredient.name.lowercase()
                val inPantry = pantryNames.any { pantryName ->
                    val key = ingNameLower.take(4)
                    pantryName.contains(key) || ingNameLower.contains(pantryName.take(4))
                }

                ingredient.copy(
                    inPantry = inPantry,
                    quantity = scaleQuantity(ingredient.quantity, scaleFactor)
                )
            }

            baseRecipe.copy(
                servings = servings.toString(),
                ingredients = scaledIngredients
            )
        } catch (e: Exception) {
            fallbackRecipe(
                errorMessage = "Error generating recipe: ${e.message ?: "Unknown error"}",
                dishName = dishName,
                servings = servings,
                pantryItems = pantryItems
            )
        }
    }

    /**
     * Very simple quantity scaler:
     * - Looks for a leading number (int or decimal) at the start of the string.
     * - Multiplies it by [factor].
     * - Leaves everything else (units/text) unchanged.
     *
     * Examples:
     * - "2 cups rice" with factor 2 -> "4 cups rice"
     * - "0.5 kg mutton" with factor 3 -> "1.5 kg mutton"
     * - "a pinch of salt" -> unchanged
     */
    private fun scaleQuantity(quantity: String, factor: Double): String {
        val trimmed = quantity.trim()
        if (trimmed.isEmpty() || abs(factor - 1.0) < 0.0001) return trimmed

        val regex = Regex("""^(\d+(\.\d+)?)(.*)$""")
        val match = regex.find(trimmed) ?: return trimmed

        val numberPart = match.groupValues[1].toDoubleOrNull() ?: return trimmed
        val rest = match.groupValues[3]

        val scaled = numberPart * factor
        val scaledStr = if (scaled % 1.0 == 0.0) {
            scaled.toInt().toString()
        } else {
            String.format("%.2f", scaled)
        }

        return (scaledStr + rest).trim()
    }

    private fun fallbackRecipe(
        errorMessage: String,
        dishName: String,
        servings: Int,
        pantryItems: List<PantryItem>
    ): GeneratedRecipe {
        val pantryNames = pantryItems.joinToString(", ") { it.name }
        val ingredients = if (pantryItems.isNotEmpty()) {
            pantryItems.map {
                RecipeIngredient(
                    name = it.name,
                    quantity = "Some of this",
                    tags = listOf("pantry"),
                    inPantry = true
                )
            }
        } else {
            listOf(
                RecipeIngredient(
                    name = "Basic ingredients for $dishName",
                    quantity = "",
                    tags = emptyList(),
                    inPantry = false
                )
            )
        }

        return GeneratedRecipe(
            title = dishName.ifBlank { "AI Recipe" },
            description = "Fallback recipe because of an error: $errorMessage",
            cookingTime = "Unknown",
            servings = servings.toString(),
            wasteReduction = "Try to use what you already have at home to reduce waste.",
            calories = "Unknown",
            ingredients = ingredients
        )
    }
}
