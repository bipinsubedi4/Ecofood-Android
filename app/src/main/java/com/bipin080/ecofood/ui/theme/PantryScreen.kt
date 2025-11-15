package com.bipin080.ecofood.ui.theme

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
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
                            modifier = Modifier.size(32.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Smart Inventory")
                    }
                },
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
                .padding(horizontal = 16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                "Track your groceries with expiry dates and get AI-powered notifications.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
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

    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Add Product to Inventory", style = MaterialTheme.typography.titleLarge)
            Spacer(modifier = Modifier.height(16.dp))
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Product name (e.g., Milk)") },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = quantity,
                    onValueChange = { quantity = it },
                    label = { Text("Qty") },
                    modifier = Modifier.weight(1f)
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
                trailingIcon = {
                    IconButton(onClick = { showExpiryDatePicker.value = true }) {
                        Icon(Icons.Default.DateRange, contentDescription = "Select Expiry Date")
                    }
                }
            )
            Spacer(modifier = Modifier.height(24.dp))
            Button(
                onClick = {
                    val pDate = purchaseDate ?: Date()
                    val eDate = expiryDate ?: Date()
                    val qty = quantity.toIntOrNull() ?: 1
                    
                    val newItem = PantryItem(
                        name = name,
                        quantity = qty,
                        unit = unit,
                        purchaseDate = pDate,
                        expiryDate = eDate
                    )
                    onAddItem(newItem)
                    
                    name = ""
                    quantity = "1"
                    purchaseDate = null
                    expiryDate = null
                },
                modifier = Modifier.fillMaxWidth().height(50.dp)
            ) {
                Text("Add to Inventory", style = MaterialTheme.typography.bodyLarge)
            }
        }
    }

    if (showPurchaseDatePicker.value) {
        val datePickerState = rememberDatePickerState()
        DatePickerDialog(
            onDismissRequest = { showPurchaseDatePicker.value = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let {
                        purchaseDate = Date(it)
                    }
                    showPurchaseDatePicker.value = false
                }) {
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
        val datePickerState = rememberDatePickerState()
        DatePickerDialog(
            onDismissRequest = { showExpiryDatePicker.value = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let {
                        expiryDate = Date(it)
                    }
                    showExpiryDatePicker.value = false
                }) {
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
    var filter by remember { mutableStateOf("All") }

    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Your Inventory", style = MaterialTheme.typography.titleLarge)
            Spacer(modifier = Modifier.height(16.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                FilterChip(selected = filter == "All", onClick = { filter = "All" }, label = { Text("All Items") })
                FilterChip(selected = filter == "Expiring Soon", onClick = { filter = "Expiring Soon" }, label = { Text("Expiring Soon") })
                FilterChip(selected = filter == "Expired", onClick = { filter = "Expired" }, label = { Text("Expired") })
            }
            Spacer(modifier = Modifier.height(16.dp))

            val filteredItems = when (filter) {
                "Expiring Soon" -> items.filter { it.expiryDate.time > System.currentTimeMillis() && (it.expiryDate.time - System.currentTimeMillis()) < 3 * 24 * 60 * 60 * 1000 } // 3 days
                "Expired" -> items.filter { it.expiryDate.time < System.currentTimeMillis() }
                else -> items
            }

            if (filteredItems.isEmpty()) {
                Box(modifier = Modifier.fillMaxWidth().padding(vertical = 32.dp), contentAlignment = Alignment.Center) {
                    Text(
                        "No items in inventory.\nAdd some products to get started!",
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                }
            } else {
                Column(modifier = Modifier.heightIn(max = 400.dp)) {
                    LazyColumn {
                        items(filteredItems) { item ->
                            InventoryItemRow(item)
                            Divider(modifier = Modifier.padding(horizontal = 8.dp))
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun InventoryItemRow(item: PantryItem) {
    val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp, horizontal = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(item.name, style = MaterialTheme.typography.bodyLarge)
            Text("${item.quantity} ${item.unit}", style = MaterialTheme.typography.bodySmall, color = Gray)
        }
        Text(
            "Expires: ${dateFormat.format(item.expiryDate)}",
            style = MaterialTheme.typography.bodyMedium,
            color = if (item.expiryDate.time < System.currentTimeMillis()) ErrorRed else MaterialTheme.colorScheme.onSurface
        )
    }
}
