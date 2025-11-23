package com.bipin080.ecofood.sync

import com.bipin080.ecofood.data.SavedRecipeDao
import com.bipin080.ecofood.data.SavedRecipeEntity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

object FirebaseSavedRecipeSync {

    private val firestore = FirebaseFirestore.getInstance()

    private fun currentUserId(): String =
        FirebaseAuth.getInstance().currentUser?.uid ?: "local_user"

    fun start(recipeDao: SavedRecipeDao, scope: CoroutineScope) {
        val userId = currentUserId()
        val collection = firestore.collection("users")
            .document(userId)
            .collection("savedRecipes")

        /* -------------------------------
         * STEP 1: One-time pull from Firestore → Room
         * ------------------------------- */
        collection.get().addOnSuccessListener { snapshot ->
            scope.launch {
                val recipes = snapshot.documents.mapNotNull { it.toSavedRecipeEntity() }

                recipes.forEach { recipeDao.insert(it) }
            }
        }

        /* -------------------------------
         * STEP 2: Continuous push Room → Firestore
         * ------------------------------- */
        scope.launch {
            recipeDao.getAll().collect { recipes ->
                recipes.forEach { recipe ->
                    val data = recipe.toFirestoreData()
                    collection
                        .document(recipe.id.toString())
                        .set(data)
                }
            }
        }
    }

    /* Convert Room → Firestore */
    private fun SavedRecipeEntity.toFirestoreData(): Map<String, Any?> = mapOf(
        "title" to title,
        "description" to description,
        "cookingTime" to cookingTime,
        "servings" to servings,
        "wasteReduction" to wasteReduction,
        "calories" to calories,
        "ingredientsJson" to ingredientsJson
    )

    /* Convert Firestore → Room */
    private fun DocumentSnapshot.toSavedRecipeEntity(): SavedRecipeEntity? {
        val idLong = id.toLongOrNull() ?: return null

        val title = getString("title") ?: return null
        val description = getString("description") ?: ""
        val cookingTime = getString("cookingTime") ?: ""
        val servings = getString("servings") ?: ""
        val wasteReduction = getString("wasteReduction") ?: ""
        val calories = getString("calories") ?: ""
        val ingredientsJson = getString("ingredientsJson") ?: "[]"

        return SavedRecipeEntity(
            id = idLong,
            title = title,
            description = description,
            cookingTime = cookingTime,
            servings = servings,
            wasteReduction = wasteReduction,
            calories = calories,
            ingredientsJson = ingredientsJson
        )
    }
}
