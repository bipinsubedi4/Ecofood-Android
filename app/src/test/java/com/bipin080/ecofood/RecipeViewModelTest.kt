package com.bipin080.ecofood

import android.os.Looper
import androidx.test.core.app.ApplicationProvider
import com.bipin080.ecofood.data.*
import com.bipin080.ecofood.viewmodel.RecipeViewModel
import com.google.gson.Gson
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.Shadows.shadowOf
import org.robolectric.annotation.Config
import kotlin.intArrayOf

@RunWith(RobolectricTestRunner::class)
@OptIn(ExperimentalCoroutinesApi::class)
@Config(sdk = [34], application = TestApp::class, manifest = Config.NONE)
class RecipeViewModelTest {

    private lateinit var viewModel: RecipeViewModel
    private lateinit var db: SavedRecipeDatabase

    @Before
    fun setup() = runTest {
        val context = ApplicationProvider.getApplicationContext<android.app.Application>()
        val gson = Gson()
        db = SavedRecipeDatabase.getInstance(context)

        // Clear table before each test
        db.savedRecipeDao().clear()

        viewModel = RecipeViewModel(context)

    }

    @After
    fun teardown() = runTest {
        db.savedRecipeDao().clear()
    }

    // -----------------------------------------------------
    // 1️⃣ setRecipe() should update the current recipe state
    // -----------------------------------------------------
    @Test
    fun setRecipe_setsCurrentRecipe() = runTest {
        val recipe = GeneratedRecipe(
            title = "Dal Bhat",
            description = "Classic Nepali food",
            cookingTime = "30 mins",
            servings = "2",
            calories = "500",
            ingredients = emptyList(),
            wasteReduction = ""
        )

        viewModel.setRecipe(recipe)

        val result = viewModel.currentRecipe.first()
        assertEquals("Dal Bhat", result?.title)
    }

    // -----------------------------------------------------
    // 2️⃣ saveRecipe() should insert into DB
    // -----------------------------------------------------
    @Test
    fun saveRecipe_addsRecipeToDatabase() = runTest {
        val recipe = GeneratedRecipe(
            title = "Momo",
            description = "Steamed dumplings",
            cookingTime = "40 mins",
            servings = "4",
            calories = "300",
            ingredients = emptyList(),
            wasteReduction = ""
        )

        viewModel.saveRecipe(recipe)
        shadowOf(Looper.getMainLooper()).idle()


        val saved = db.savedRecipeDao().getAllOnce()
        assertEquals(1, saved.size)
        assertEquals("Momo", saved[0].title)
    }

    // -----------------------------------------------------
    // 3️⃣ deleteRecipe() should remove the item from DB
    // -----------------------------------------------------
    @Test
    fun deleteRecipe_removesRecipeFromDatabase() = runTest {
        val recipe = GeneratedRecipe(
            title = "Thukpa",
            description = "Noodle soup",
            cookingTime = "25 mins",
            servings = "1",
            calories = "200",
            ingredients = emptyList(),
            wasteReduction = ""
        )

        // Convert to saved entity and insert
        val gson = Gson()
        val savedEntity = SavedRecipeEntity.fromGeneratedRecipe(recipe,gson)
        db.savedRecipeDao().insert(savedEntity)

        // Now delete via ViewModel (MUST pass SavedRecipeEntity)
        viewModel.deleteRecipe(recipe)

        val saved = db.savedRecipeDao().getAllOnce()
        assertEquals(0, saved.size)
    }

}

// Helper extension to fetch list without Flow
suspend fun SavedRecipeDao.getAllOnce(): List<SavedRecipeEntity> {
    return getAll().first()
}
