package com.bipin080.ecofood.ui.theme

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.bipin080.ecofood.data.MarketplaceDatabase
import com.bipin080.ecofood.data.MarketplaceItem
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MarketplaceScreen() {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val db = remember { MarketplaceDatabase.getDatabase(context, coroutineScope) }
    val items by db.marketplaceItemDao().getAll().collectAsState(initial = emptyList())
    var showDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Marketplace") })
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { showDialog = true }) {
                Icon(Icons.Default.Add, contentDescription = "Add item")
            }
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            items(items) { item ->
                MarketplaceItemCard(item = item)
            }
        }

        if (showDialog) {
            var name by remember { mutableStateOf("") }
            var description by remember { mutableStateOf("") }
            var location by remember { mutableStateOf("") }
            var contact by remember { mutableStateOf("") }
            var isNameError by remember { mutableStateOf(false) }
            var isDescriptionError by remember { mutableStateOf(false) }
            var isLocationError by remember { mutableStateOf(false) }
            var isContactError by remember { mutableStateOf(false) }

            fun validateFields() {
                isNameError = name.isBlank()
                isDescriptionError = description.isBlank()
                isLocationError = location.isBlank()
                isContactError = contact.isBlank()
            }

            AlertDialog(
                onDismissRequest = { showDialog = false },
                title = { Text("Add Item to Marketplace") },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = name,
                            onValueChange = { name = it; isNameError = it.isBlank() },
                            label = { Text("Item Name") },
                            isError = isNameError,
                            supportingText = { if (isNameError) Text("Item name cannot be empty") }
                        )
                        OutlinedTextField(
                            value = description,
                            onValueChange = { description = it; isDescriptionError = it.isBlank() },
                            label = { Text("Description") },
                            isError = isDescriptionError,
                            supportingText = { if (isDescriptionError) Text("Description cannot be empty") }
                        )
                        OutlinedTextField(
                            value = location,
                            onValueChange = { location = it; isLocationError = it.isBlank() },
                            label = { Text("Location") },
                            isError = isLocationError,
                            supportingText = { if (isLocationError) Text("Location cannot be empty") }
                        )
                        OutlinedTextField(
                            value = contact,
                            onValueChange = { contact = it; isContactError = it.isBlank() },
                            label = { Text("Contact Info") },
                            isError = isContactError,
                            supportingText = { if (isContactError) Text("Contact info cannot be empty") }
                        )
                    }
                },
                confirmButton = {
                    TextButton(
                        onClick = {
                            validateFields()
                            if (!isNameError && !isDescriptionError && !isLocationError && !isContactError) {
                                coroutineScope.launch {
                                    db.marketplaceItemDao().insert(MarketplaceItem(name = name, description = description, location = location, contact = contact))
                                    showDialog = false
                                }
                            }
                        }
                    ) {
                        Text("Add")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showDialog = false }) {
                        Text("Cancel")
                    }
                }
            )
        }
    }
}

@Composable
fun MarketplaceItemCard(item: MarketplaceItem) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(item.name, style = MaterialTheme.typography.titleLarge)
            Spacer(modifier = Modifier.height(8.dp))
            Text(item.description, style = MaterialTheme.typography.bodyMedium)
            Spacer(modifier = Modifier.height(8.dp))
            Text(item.location, style = MaterialTheme.typography.bodySmall)
            Spacer(modifier = Modifier.height(16.dp))
            Button(onClick = { /* TODO: Implement contact functionality */ }) {
                Text("Contact: ${item.contact}")
            }
        }
    }
}
