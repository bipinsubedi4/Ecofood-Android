package com.bipin080.ecofood.ui.theme

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.bipin080.ecofood.data.MarketplaceItem
import com.bipin080.ecofood.viewmodel.MarketplaceViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddMarketplaceItemScreen(
    marketplaceViewModel: MarketplaceViewModel = viewModel(),
    onNavigateBack: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Share Your Surplus Food") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { paddingValues ->
        ShareFoodForm(
            modifier = Modifier.padding(paddingValues),
            onShareFood = { newItem ->
                marketplaceViewModel.addItem(newItem)
                onNavigateBack() // Go back after sharing
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ShareFoodForm(modifier: Modifier = Modifier, onShareFood: (MarketplaceItem) -> Unit) {
    var name by remember { mutableStateOf("") }
    var quantity by remember { mutableStateOf("") }
    var unit by remember { mutableStateOf("pieces") }
    var price by remember { mutableStateOf("") }
    var expiryDate by remember { mutableStateOf<Date?>(null) }
    var location by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var isUnitDropdownExpanded by remember { mutableStateOf(false) }
    val units = listOf("pieces", "litre", "kg", "g", "ml")
    val showDatePicker = remember { mutableStateOf(false) }
    val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Product name") }, modifier = Modifier.fillMaxWidth())
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedTextField(value = quantity, onValueChange = { quantity = it }, label = { Text("Quantity") }, modifier = Modifier.weight(1f))
            ExposedDropdownMenuBox(expanded = isUnitDropdownExpanded, onExpandedChange = { isUnitDropdownExpanded = !isUnitDropdownExpanded }, modifier = Modifier.weight(1f)) {
                OutlinedTextField(value = unit, onValueChange = {}, label = { Text("Unit") }, readOnly = true, trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = isUnitDropdownExpanded) }, modifier = Modifier.menuAnchor())
                ExposedDropdownMenu(expanded = isUnitDropdownExpanded, onDismissRequest = { isUnitDropdownExpanded = false }) {
                    units.forEach { DropdownMenuItem(text = { Text(it) }, onClick = { unit = it; isUnitDropdownExpanded = false }) }
                }
            }
        }
        OutlinedTextField(value = price, onValueChange = { price = it }, label = { Text("Price ($)") }, modifier = Modifier.fillMaxWidth())
        OutlinedTextField(value = expiryDate?.let { dateFormat.format(it) } ?: "", onValueChange = {}, label = { Text("Expiry Date") }, readOnly = true, trailingIcon = { IconButton(onClick = { showDatePicker.value = true }) { Icon(Icons.Default.CalendarToday, contentDescription = null) } }, modifier = Modifier.fillMaxWidth())
        OutlinedTextField(value = location, onValueChange = { location = it }, label = { Text("Location/Area") }, modifier = Modifier.fillMaxWidth())
        OutlinedTextField(value = description, onValueChange = { description = it }, label = { Text("Description (condition, reason for sharing, etc.)") }, modifier = Modifier.fillMaxWidth(), maxLines = 4)
        Spacer(modifier = Modifier.height(16.dp))
        Button(
            onClick = {
                val newItem = MarketplaceItem(
                    name = name,
                    quantity = quantity,
                    unit = unit,
                    price = price.toDoubleOrNull() ?: 0.0,
                    expiryDate = expiryDate ?: Date(),
                    location = location,
                    description = description,
                    sellerName = "You", // TODO: Replace with actual user's name
                    contact = "your.email@example.com" // TODO: Replace with actual user's email
                )
                onShareFood(newItem)
            },
            modifier = Modifier.fillMaxWidth().height(50.dp)
        ) {
            Text("Share Food", style = MaterialTheme.typography.bodyLarge)
        }
    }

    if (showDatePicker.value) {
        val datePickerState = rememberDatePickerState()
        DatePickerDialog(onDismissRequest = { showDatePicker.value = false }, confirmButton = {
            TextButton(onClick = {
                datePickerState.selectedDateMillis?.let { expiryDate = Date(it) }
                showDatePicker.value = false
            }) { Text("OK") }
        }, dismissButton = { TextButton(onClick = { showDatePicker.value = false }) { Text("Cancel") } }) {
            DatePicker(state = datePickerState)
        }
    }
}
