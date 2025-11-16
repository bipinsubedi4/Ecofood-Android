package com.bipin080.ecofood.ui.theme

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.bipin080.ecofood.R
import com.bipin080.ecofood.data.PantryItem
import com.bipin080.ecofood.viewmodel.PantryViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PantryScreen(pantryViewModel: PantryViewModel = viewModel()) {
    val items by pantryViewModel.pantryItems.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Image(
                            painter = painterResource(id = R.drawable.app_logo),
                            contentDescription = "EcoFood Logo",
                            modifier = Modifier
                                .size(36.dp)
                                .clip(RoundedCornerShape(8.dp))
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text("Smart Inventory", style = MaterialTheme.typography.titleLarge)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    titleContentColor = MaterialTheme.colorScheme.primary
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                "Keep your pantry organized and never let good food go to waste.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                textAlign = TextAlign.Center
            )
            
            AddProductCard { newItem ->
                pantryViewModel.addItem(newItem)
            }
            
            InventoryCard(items)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddProductCard(onAddItem: (PantryItem) -> Unit) {
    var name by remember { mutableStateOf("") }
    var quantity by remember { mutableStateOf("1") }
    var unit by remember { mutableStateOf("pieces") }
    var purchaseDate by remember { mutableStateOf<Date?>(null) }
    var expiryDate by remember { mutableStateOf<Date?>(null) }
    val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
    var isUnitDropdownExpanded by remember { mutableStateOf(false) }
    val units = listOf("pieces", "litre", "kg", "g", "ml")

    val showPurchaseDatePicker = remember { mutableStateOf(false) }
    val showExpiryDatePicker = remember { mutableStateOf(false) }

    val isFormValid by remember(name, quantity, purchaseDate, expiryDate) {
        mutableStateOf(
            name.isNotBlank() && quantity.isNotBlank() && purchaseDate != null && expiryDate != null
        )
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(24.dp)) {
            Text("Add a New Item", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(16.dp))
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Product name (e.g., Milk)") },
                modifier = Modifier.fillMaxWidth(),
                isError = name.isBlank()
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = quantity,
                    onValueChange = { quantity = it },
                    label = { Text("Quantity") },
                    modifier = Modifier.weight(1f),
                    isError = quantity.isBlank()
                )
                ExposedDropdownMenuBox(
                    expanded = isUnitDropdownExpanded,
                    onExpandedChange = { isUnitDropdownExpanded = !isUnitDropdownExpanded },
                    modifier = Modifier.weight(1f)
                ) {
                    OutlinedTextField(
                        value = unit,
                        onValueChange = {},
                        label = { Text("Unit") },
                        readOnly = true,
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = isUnitDropdownExpanded) },
                        modifier = Modifier.menuAnchor()
                    )
                    ExposedDropdownMenu(
                        expanded = isUnitDropdownExpanded,
                        onDismissRequest = { isUnitDropdownExpanded = false }
                    ) {
                        units.forEach {
                            DropdownMenuItem(text = { Text(it) }, onClick = {
                                unit = it
                                isUnitDropdownExpanded = false
                            })
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                value = purchaseDate?.let { dateFormat.format(it) } ?: "",
                onValueChange = {},
                label = { Text("Purchase Date") },
                modifier = Modifier.fillMaxWidth(),
                readOnly = true,
                isError = purchaseDate == null,
                trailingIcon = {
                    IconButton(onClick = { showPurchaseDatePicker.value = true }) {
                        Icon(Icons.Default.DateRange, contentDescription = "Select Purchase Date")
                    }
                }
            )
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                value = expiryDate?.let { dateFormat.format(it) } ?: "",
                onValueChange = {},
                label = { Text("Expiry Date") },
                modifier = Modifier.fillMaxWidth(),
                readOnly = true,
                isError = expiryDate == null,
                trailingIcon = {
                    IconButton(onClick = { showExpiryDatePicker.value = true }) {
                        Icon(Icons.Default.DateRange, contentDescription = "Select Expiry Date")
                    }
                }
            )
            Spacer(modifier = Modifier.height(24.dp))
            Button(
                onClick = {
                    val newItem = PantryItem(
                        name = name,
                        quantity = quantity.toIntOrNull() ?: 1,
                        unit = unit,
                        purchaseDate = purchaseDate!!,
                        expiryDate = expiryDate!!
                    )
                    onAddItem(newItem)
                    
                    name = ""
                    quantity = "1"
                    purchaseDate = null
                    expiryDate = null
                },
                modifier = Modifier.fillMaxWidth().height(50.dp),
                shape = RoundedCornerShape(12.dp),
                enabled = isFormValid
            ) {
                Text("Add to Inventory", style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold)
            }
        }
    }

    if (showPurchaseDatePicker.value) {
        val datePickerState = rememberDatePickerState(initialSelectedDateMillis = System.currentTimeMillis())
        val isDateValid = (datePickerState.selectedDateMillis ?: 0) <= System.currentTimeMillis()

        DatePickerDialog(
            onDismissRequest = { showPurchaseDatePicker.value = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        datePickerState.selectedDateMillis?.let {
                            purchaseDate = Date(it)
                        }
                        showPurchaseDatePicker.value = false
                    },
                    enabled = isDateValid
                ) {
                    Text("OK")
                }
            },
            dismissButton = {
                TextButton(onClick = { showPurchaseDatePicker.value = false }) {
                    Text("Cancel")
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }

    if (showExpiryDatePicker.value) {
        val datePickerState = rememberDatePickerState(initialSelectedDateMillis = System.currentTimeMillis())
        val isDateValid = (datePickerState.selectedDateMillis ?: 0) > System.currentTimeMillis()

        DatePickerDialog(
            onDismissRequest = { showExpiryDatePicker.value = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        datePickerState.selectedDateMillis?.let {
                            expiryDate = Date(it)
                        }
                        showExpiryDatePicker.value = false
                    },
                    enabled = isDateValid
                ) {
                    Text("OK")
                }
            },
            dismissButton = {
                TextButton(onClick = { showExpiryDatePicker.value = false }) {
                    Text("Cancel")
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }
}

@Composable
fun InventoryCard(items: List<PantryItem>) {
    // ...
}

@Composable
fun InventoryItemRow(item: PantryItem) {
    // ...
}
