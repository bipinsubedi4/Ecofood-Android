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

    /** Allow disabling sync in unit tests */
    @JvmStatic
    var ENABLED = true

    /** Lazy Firebase — prevents crash during JVM tests */
    private val firestore: FirebaseFirestore by lazy {
        FirebaseFirestore.getInstance()
    }

    private fun currentUserId(): String =
        FirebaseAuth.getInstance().currentUser?.uid ?: "local_user"

    fun start(recipeDao: SavedRecipeDao, scope: CoroutineScope) {
        if (!ENABLED) return  // <-- TEST SAFE

        val userId = currentUserId()
        val collection = firestore.collection("users")
            .document(userId)
            .collection("savedRecipes")

        /* -------------------------------
         * STEP 1: Firestore → Room (pull)
         * ------------------------------- */
        collection.get().addOnSuccessListener { snapshot ->
            scope.launch {
                snapshot.documents
                    .mapNotNull { it.toSavedRecipeEntity() }
                    .forEach { recipeDao.insert(it) }
            }
        }

        /* -------------------------------
         * STEP 2: Room → Firestore (push)
         * ------------------------------- */
        scope.launch {
            recipeDao.getAll().collect { recipes ->
                recipes.forEach { recipe ->
                    collection
                        .document(recipe.id.toString())
                        .set(recipe.toFirestoreData())
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

        return SavedRecipeEntity(
            id = idLong,
            title = getString("title") ?: return null,
            description = getString("description") ?: "",
            cookingTime = getString("cookingTime") ?: "",
            servings = getString("servings") ?: "",
            wasteReduction = getString("wasteReduction") ?: "",
            calories = getString("calories") ?: "",
            ingredientsJson = getString("ingredientsJson") ?: "[]"
        )
    }
}
