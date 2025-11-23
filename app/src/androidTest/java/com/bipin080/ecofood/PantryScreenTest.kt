package com.bipin080.ecofood

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.*
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.bipin080.ecofood.ui.theme.PantryScreen
import com.bipin080.ecofood.viewmodel.PantryViewModel
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import androidx.test.core.app.ApplicationProvider

@RunWith(AndroidJUnit4::class)
class PantryScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun openAddDialog_andAddPantryItem_displaysInList() {
        val context = ApplicationProvider.getApplicationContext<android.content.Context>()
        val viewModel = PantryViewModel(context)

        composeTestRule.setContent {
            PantryScreen(pantryViewModel = viewModel)
        }

        // Click FAB to open add dialog
        composeTestRule.onNodeWithContentDescription("Add Item").performClick()

        // Enter name
        composeTestRule.onNodeWithTag("name_field").performTextInput("Tomato")

        // Enter quantity
        composeTestRule.onNodeWithTag("quantity_field").performTextInput("2")

        // Select unit dropdown
        composeTestRule.onNodeWithTag("unit_dropdown").performClick()
        composeTestRule.onNodeWithText("kg").performClick()

        // Select expiry date
        composeTestRule.onNodeWithTag("date_picker").performClick()

        // Pick tomorrow (valid date)
        composeTestRule.onNodeWithTag("confirm_date").performClick()

        // Save Item
        composeTestRule.onNodeWithText("Save").performClick()

        // Item should appear in list
        composeTestRule.onNodeWithText("Tomato").assertExists()
    }

    @Test
    fun cannotSelectPastDate_showsValidationError() {
        val context = ApplicationProvider.getApplicationContext<android.content.Context>()
        val viewModel = PantryViewModel(context)

        composeTestRule.setContent {
            PantryScreen(pantryViewModel = viewModel)
        }

        composeTestRule.onNodeWithContentDescription("Add Item").performClick()

        // Click date picker
        composeTestRule.onNodeWithTag("date_picker").performClick()

        // Try selecting a past date
        composeTestRule.onNodeWithTag("past_date").performClick()

        // Expect error message
        composeTestRule.onNodeWithText("Date cannot be in the past").assertExists()
    }
}
