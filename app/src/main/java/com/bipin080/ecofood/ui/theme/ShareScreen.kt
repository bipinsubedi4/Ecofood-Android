package com.bipin080.ecofood.ui.theme

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.bipin080.ecofood.data.SharePost
import com.bipin080.ecofood.data.fakeSharePosts
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material.icons.Icons


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ShareScreen() {
    val posts by remember { mutableStateOf(fakeSharePosts()) }

    Scaffold(
        topBar = { TopAppBar(title = { Text("Share Nearby") }) },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { /* TODO: create post */ }
            ) {
                Icon(
                    imageVector = Icons.Filled.Add,
                    contentDescription = "Post Item"
                )
            }
        }


    ) { inner ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(inner),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(posts) { p -> ShareCard(p) }
        }
    }
}

@Composable
private fun ShareCard(p: SharePost) {
    Card {
        Column(Modifier.padding(16.dp)) {
            Text(p.title, style = MaterialTheme.typography.titleMedium)
            Text(p.desc, style = MaterialTheme.typography.bodyMedium)
            Spacer(Modifier.height(8.dp))
            AssistChip(onClick = { /* TODO: chat */ }, label = { Text("${p.distanceKm} km • Message") })
        }
    }
}
