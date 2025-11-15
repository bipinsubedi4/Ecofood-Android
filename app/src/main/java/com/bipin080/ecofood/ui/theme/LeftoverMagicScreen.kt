package com.bipin080.ecofood.ui.theme

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LeftoverMagicScreen() {
    var leftoverFood by remember { mutableStateOf("") }
    var amount by remember { mutableStateOf("Small amount") }
    var cuisine by remember { mutableStateOf("Any Cuisine") }
    var meal by remember { mutableStateOf("Any Meal") }
    var additionalIngredients by remember { mutableStateOf("") }
    var magicRecipe by remember { mutableStateOf("Describe your leftovers to get AI-powered recipe suggestions!") }
    var isLoading by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Leftover Magic") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.primary
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            Text(
                "Transform your leftover cooked food into delicious new dishes with AI.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
            )

            // Input Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("What leftovers do you have?", style = MaterialTheme.typography.titleLarge)
                    OutlinedTextField(value = leftoverFood, onValueChange = { leftoverFood = it }, label = { Text("Describe your leftover food") }, modifier = Modifier.fillMaxWidth())

                    // Dropdowns for amount, cuisine, and meal
                    UnitDropdown(label = "Amount", selectedUnit = amount, onUnitChange = { amount = it }, units = listOf("Small amount", "Medium amount", "Large amount"))
                    UnitDropdown(label = "Cuisine", selectedUnit = cuisine, onUnitChange = { cuisine = it }, units = listOf("Any Cuisine", "Italian", "Mexican", "Asian", "Indian"))
                    UnitDropdown(label = "Meal", selectedUnit = meal, onUnitChange = { meal = it }, units = listOf("Any Meal", "Breakfast", "Lunch", "Dinner", "Snack"))

                    OutlinedTextField(value = additionalIngredients, onValueChange = { additionalIngredients = it }, label = { Text("Additional ingredients (optional)") }, modifier = Modifier.fillMaxWidth(), maxLines = 3)
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    Button(
                        onClick = {
                            isLoading = true
                            coroutineScope.launch {
                                magicRecipe = generateMagicRecipe(leftoverFood, amount)
                                isLoading = false
                            }
                        },
                        modifier = Modifier.fillMaxWidth().height(50.dp)
                    ) {
                        Text("Create Magic Recipe", style = MaterialTheme.typography.bodyLarge)
                    }
                }
            }

            // Output Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Your Magic Recipe", style = MaterialTheme.typography.titleLarge)
                    Spacer(modifier = Modifier.height(16.dp))
                    if (isLoading) {
                        CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
                    } else {
                        Text(
                            magicRecipe, 
                            style = MaterialTheme.typography.bodyLarge,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp)
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UnitDropdown(label: String, selectedUnit: String, onUnitChange: (String) -> Unit, units: List<String>) {
    var isDropdownExpanded by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(
        expanded = isDropdownExpanded,
        onExpandedChange = { isDropdownExpanded = !isDropdownExpanded },
        modifier = Modifier.fillMaxWidth()
    ) {
        OutlinedTextField(
            value = selectedUnit,
            onValueChange = {},
            label = { Text(label) },
            readOnly = true,
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = isDropdownExpanded) },
            modifier = Modifier.menuAnchor().fillMaxWidth()
        )
        ExposedDropdownMenu(
            expanded = isDropdownExpanded,
            onDismissRequest = { isDropdownExpanded = false }
        ) {
            units.forEach {
                DropdownMenuItem(text = { Text(it) }, onClick = {
                    onUnitChange(it)
                    isDropdownExpanded = false
                })
            }
        }
    }
}

suspend fun generateMagicRecipe(leftover: String, amount: String): String {
    // Simulate a network call to a Gemini-like API
    delay(2000)

    if (leftover.isBlank()) {
        return "Please describe your leftovers to get a magic recipe!"
    }

    val idea = when {
        "pizza" in leftover.lowercase() -> "Chop it up and turn it into a Pizza Frittata. Just mix with eggs, cheese, and bake!"
        "chicken" in leftover.lowercase() -> "Shred the chicken and make some delicious Chicken Tacos. Add some salsa and sour cream."
        "rice" in leftover.lowercase() -> "Create some quick and easy Fried Rice. Just add soy sauce, veggies, and an egg."
        else -> "How about a 'Kitchen Sink' Stir-Fry? Sauté your leftovers with some garlic, ginger, and any veggies you have on hand."
    }

    return "You have a $amount of leftover $leftover. Here's a magical idea:\n\n$idea"
}
