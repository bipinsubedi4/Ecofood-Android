package com.bipin080.ecofood.ui.theme

import android.app.DatePickerDialog
import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SmallTopAppBar
import androidx.compose.material3.Text
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.bipin080.ecofood.viewmodel.MarketplaceViewModel
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import com.bipin080.ecofood.data.MarketplaceItem


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddMarketplaceItemScreen(
    marketplaceViewModel: MarketplaceViewModel = viewModel(),
    onNavigateBack: () -> Unit
) {
    var name by remember { mutableStateOf("") }
    var quantity by remember { mutableStateOf("") }
    var unit by remember { mutableStateOf("pieces") }
    var price by remember { mutableStateOf("") }
    var expiryDate by remember { mutableStateOf("") }
    var location by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }

    var showErrors by remember { mutableStateOf(false) }

    val context = LocalContext.current

    /** DATE PICKER FUNCTION **/
    fun pickDate() {
        val today = Calendar.getInstance()
        val datePicker = DatePickerDialog(
            context,
            { _, y, m, d ->
                val selected = Calendar.getInstance()
                selected.set(y, m, d)

                // BLOCK old dates
                if (selected.before(today)) {
                    Toast.makeText(context, "Expiry date cannot be in the past", Toast.LENGTH_SHORT).show()
                    return@DatePickerDialog
                }

                val format = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
                expiryDate = format.format(selected.time)
            },
            today.get(Calendar.YEAR),
            today.get(Calendar.MONTH),
            today.get(Calendar.DAY_OF_MONTH)
        )
        datePicker.datePicker.minDate = today.timeInMillis
        datePicker.show()
    }

    Scaffold(
        topBar = {
            SmallTopAppBar(
                title = { Text("Share Your Surplus Food") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->

        Column(
            modifier = Modifier
                .padding(padding)
                .padding(16.dp)
                .fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {

            // PRODUCT NAME
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Product name") },
                isError = showErrors && name.isBlank(),
                modifier = Modifier.fillMaxWidth()
            )

            // QUANTITY + UNIT ROW
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedTextField(
                    value = quantity,
                    onValueChange = { quantity = it },
                    label = { Text("Quantity") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    isError = showErrors && quantity.isBlank(),
                    modifier = Modifier.weight(1f)
                )

                /** UNIT DROPDOWN **/
                var unitExpanded by remember { mutableStateOf(false) }
                val unitOptions = listOf("pieces", "kg", "g", "ml", "L")

                ExposedDropdownMenuBox(
                    expanded = unitExpanded,
                    onExpandedChange = { unitExpanded = it },
                    modifier = Modifier.weight(1f)
                ) {
                    OutlinedTextField(
                        value = unit,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Unit") },
                        trailingIcon = {
                            ExposedDropdownMenuDefaults.TrailingIcon(expanded = unitExpanded)
                        },
                        modifier = Modifier
                            .menuAnchor()
                            .fillMaxWidth()
                    )

                    ExposedDropdownMenu(
                        expanded = unitExpanded,
                        onDismissRequest = { unitExpanded = false }
                    ) {
                        unitOptions.forEach { option ->
                            DropdownMenuItem(
                                text = { Text(option) },
                                onClick = {
                                    unit = option
                                    unitExpanded = false
                                }
                            )
                        }
                    }
                }

            }

            // PRICE
            OutlinedTextField(
                value = price,
                onValueChange = { price = it },
                label = { Text("Price ($)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                isError = showErrors && price.isBlank(),
                modifier = Modifier.fillMaxWidth()
            )

            // EXPIRY DATE
            OutlinedTextField(
                value = expiryDate,
                onValueChange = {},
                readOnly = true,
                label = { Text("Expiry Date") },
                trailingIcon = {
                    IconButton(onClick = { pickDate() }) {
                        Icon(Icons.Default.CalendarToday, contentDescription = "Pick Date")
                    }
                },
                isError = showErrors && expiryDate.isBlank(),
                modifier = Modifier.fillMaxWidth()
            )

            // LOCATION
            OutlinedTextField(
                value = location,
                onValueChange = { location = it },
                label = { Text("Location/Area") },
                isError = showErrors && location.isBlank(),
                modifier = Modifier.fillMaxWidth()
            )

            // DESCRIPTION
            OutlinedTextField(
                value = description,
                onValueChange = { description = it },
                label = { Text("Description (condition, reason for sharing, etc.)") },
                modifier = Modifier.fillMaxWidth(),
                maxLines = 3
            )

            // BUTTON
            Button(
                onClick = {
                    if (name.isBlank() || quantity.isBlank() || price.isBlank() ||
                        expiryDate.isBlank() || location.isBlank()
                    ) {
                        showErrors = true
                        return@Button
                    }

                    // SAFE PARSING
                    val qty = quantity.toIntOrNull() ?: 0
                    val priceVal = price.toDoubleOrNull() ?: 0.0

                    val expiry = expiryDate
                    val item = MarketplaceItem(
                        name = name,
                        quantity = qty,
                        unit = unit,
                        price = priceVal,
                        expiryDate = expiry,
                        location = location,
                        description = description
                    )

                    marketplaceViewModel.addItem(item)


                    onNavigateBack()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
            ) {
                Text("Share Food")
            }
        }
    }
}
