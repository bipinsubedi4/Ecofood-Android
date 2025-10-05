package com.bipin080.ecofood.ui.theme


import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen() {
    var veg by remember { mutableStateOf(true) }
    var halal by remember { mutableStateOf(false) }
    var nutFree by remember { mutableStateOf(false) }

    Column(Modifier.fillMaxSize()) {
        TopAppBar(title = { Text("Profile & Preferences") })
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text("Dietary Preferences", style = MaterialTheme.typography.titleMedium)
            PrefChip(veg, { veg = it }, "Vegetarian")
            PrefChip(halal, { halal = it }, "Halal")
            PrefChip(nutFree, { nutFree = it }, "Nut-free")
            Spacer(Modifier.height(16.dp))
            Text("Impact", style = MaterialTheme.typography.titleMedium)
            Text("Waste prevented: 8 items • $42 saved • 3.1 kg CO₂e")
        }
    }
}

@Composable
private fun PrefChip(checked: Boolean, onChange: (Boolean) -> Unit, label: String) {
    AssistChip(onClick = { onChange(!checked) },
        label = { Text(if (checked) "✓ $label" else label) })
}
