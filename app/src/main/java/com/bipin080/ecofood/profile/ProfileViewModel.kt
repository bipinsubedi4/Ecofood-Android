package com.bipin080.ecofood.profile

import androidx.lifecycle.ViewModel
import com.bipin080.ecofood.auth.UserProfile
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

data class ProfileUiState(
    val profile: UserProfile? = null,
    val isLoading: Boolean = false,
    val error: String? = null
)

class ProfileViewModel(
    private val auth: FirebaseAuth = FirebaseAuth.getInstance(),
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
) : ViewModel() {

    private val _uiState = MutableStateFlow(ProfileUiState(isLoading = true))
    val uiState: StateFlow<ProfileUiState> = _uiState

    init {
        loadProfile()
    }

    fun loadProfile() {
        val uid = auth.currentUser?.uid ?: run {
            _uiState.value = ProfileUiState(error = "Not logged in")
            return
        }

        _uiState.value = ProfileUiState(isLoading = true)

        firestore.collection("users")
            .document(uid)
            .get()
            .addOnSuccessListener { doc ->
                val profile = doc.toObject(UserProfile::class.java)
                _uiState.value = ProfileUiState(profile = profile, isLoading = false)
            }
            .addOnFailureListener { e ->
                _uiState.value = ProfileUiState(
                    error = e.message ?: "Failed to load profile",
                    isLoading = false
                )
            }
    }

    fun logout(onLoggedOut: () -> Unit) {
        auth.signOut()
        onLoggedOut()
    }
}
