package com.bipin080.ecofood.ui.theme


import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlanScreen() {
    val days = listOf("Mon","Tue","Wed","Thu","Fri","Sat","Sun")
    val plan = remember { days.associateWith { listOf("Lunch: Salad","Dinner: Pasta") } }

    Column(Modifier.fillMaxSize()) {
        TopAppBar(title = { Text("Weekly Plan") })
        LazyColumn(
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(days) { d ->
                Card {
                    Column(Modifier.padding(16.dp)) {
                        Text(d, style = MaterialTheme.typography.titleMedium)
                        plan[d]?.forEach { Text(it) }
                        Spacer(Modifier.height(8.dp))
                        FilledTonalButton(onClick = { /* TODO: swap */ }) { Text("Swap") }
                    }
                }
            }
        }
    }
}
