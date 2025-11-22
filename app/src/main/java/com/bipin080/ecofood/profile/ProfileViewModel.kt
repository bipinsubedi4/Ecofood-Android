package com.bipin080.ecofood.profile

import androidx.lifecycle.ViewModel
import com.bipin080.ecofood.auth.UserProfile
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class ProfileViewModel : ViewModel() {

    private val firestore = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    private val _uiState = MutableStateFlow(ProfileUiState())
    val uiState: StateFlow<ProfileUiState> = _uiState

    init {
        loadUserProfile()
    }

    fun loadUserProfile() {
        val user = auth.currentUser ?: return

        firestore.collection("users")
            .document(user.uid)
            .get()
            .addOnSuccessListener { doc ->
                if (doc.exists()) {
                    val profile = doc.toObject(UserProfile::class.java)
                    _uiState.value = ProfileUiState(profile = profile)
                } else {
                    val newProfile = UserProfile(
                        uid = user.uid,
                        name = user.displayName ?: "",
                        email = user.email ?: "",
                        phone = "",
                        address = "",
                        photoUri = ""
                    )

                    firestore.collection("users")
                        .document(user.uid)
                        .set(newProfile)

                    _uiState.value = ProfileUiState(profile = newProfile)
                }
            }
            .addOnFailureListener {
                _uiState.value = ProfileUiState(error = it.message)
            }
    }

    fun updateProfile(
        name: String,
        phone: String,
        address: String,
        photoUri: String
    ) {
        val user = auth.currentUser ?: return

        val updated = mapOf(
            "name" to name,
            "phone" to phone,
            "address" to address,
            "photoUri" to photoUri
        )

        firestore.collection("users")
            .document(user.uid)
            .update(updated)
            .addOnSuccessListener {
                _uiState.value = _uiState.value.copy(
                    profile = _uiState.value.profile?.copy(
                        name = name,
                        phone = phone,
                        address = address,
                        photoUri = photoUri
                    ),
                    success = true
                )
            }
            .addOnFailureListener {
                _uiState.value = ProfileUiState(error = it.message)
            }
    }
}

data class ProfileUiState(
    val profile: UserProfile? = null,
    val isLoading: Boolean = false,
    val error: String? = null,
    val success: Boolean = false
)
