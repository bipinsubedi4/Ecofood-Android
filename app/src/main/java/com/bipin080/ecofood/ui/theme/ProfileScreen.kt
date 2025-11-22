package com.bipin080.ecofood.ui.theme

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.bipin080.ecofood.R
import com.google.firebase.auth.FirebaseAuth
import com.bipin080.ecofood.profile.ProfileViewModel

@Composable
fun ProfileScreen(
    onLogout: () -> Unit,
    viewModel: ProfileViewModel = viewModel()
) {
    val state by viewModel.uiState.collectAsState()
    val profile = state.profile

    var isEditing by remember { mutableStateOf(false) }

    var name by remember { mutableStateOf(profile?.name ?: "") }
    var phone by remember { mutableStateOf(profile?.phone ?: "") }
    var address by remember { mutableStateOf(profile?.address ?: "") }
    var photoUri by remember { mutableStateOf(profile?.photoUri ?: "") }

    // ------------------ IMAGE PICKER ------------------
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        if (uri != null) {
            photoUri = uri.toString()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        // ------------------ Profile Image ------------------
        if (photoUri.isNotEmpty()) {
            AsyncImage(
                model = photoUri,
                contentDescription = "Profile Image",
                modifier = Modifier
                    .size(120.dp)
                    .clip(CircleShape)
            )
        } else {
            Image(
                painter = painterResource(id = R.drawable.random_guy),
                contentDescription = "Default",
                modifier = Modifier
                    .size(120.dp)
                    .clip(CircleShape)
            )
        }

        Spacer(Modifier.height(8.dp))

        if (isEditing) {
            TextButton(onClick = { launcher.launch("image/*") }) {
                Text("Choose Photo")
            }
        }

        Spacer(Modifier.height(16.dp))

        // ------------------ Fields ------------------
        OutlinedTextField(
            value = name,
            onValueChange = { if (isEditing) name = it },
            label = { Text("Full Name") },
            modifier = Modifier.fillMaxWidth(),
            enabled = isEditing
        )

        OutlinedTextField(
            value = profile?.email ?: "",
            onValueChange = {},
            label = { Text("Email") },
            enabled = false,
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = phone,
            onValueChange = { if (isEditing) phone = it },
            label = { Text("Phone Number") },
            modifier = Modifier.fillMaxWidth(),
            enabled = isEditing
        )

        OutlinedTextField(
            value = address,
            onValueChange = { if (isEditing) address = it },
            label = { Text("Address") },
            modifier = Modifier.fillMaxWidth(),
            enabled = isEditing
        )

        Spacer(Modifier.height(24.dp))

        // ------------------ Buttons ------------------
        if (!isEditing) {
            Button(
                onClick = { isEditing = true },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Edit Profile")
            }
        } else {
            Button(
                onClick = {
                    viewModel.updateProfile(name, phone, address, photoUri)
                    isEditing = false
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Save")
            }
        }

        Spacer(Modifier.height(20.dp))

        Button(
            onClick = {
                FirebaseAuth.getInstance().signOut()
                onLogout()
            },
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(MaterialTheme.colorScheme.error)
        ) {
            Text("Logout", color = MaterialTheme.colorScheme.onError)
        }
    }
}
