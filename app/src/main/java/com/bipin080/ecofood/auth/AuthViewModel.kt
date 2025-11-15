package com.bipin080.ecofood.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

data class AuthUiState(
    val displayName: String = "",
    val email: String = "",
    val password: String = "",
    val isLoading: Boolean = false,
    val error: String? = null,
    val isAuthenticated: Boolean = false
)

data class UserProfile(
    val uid: String = "",
    val displayName: String = "",
    val email: String = ""
)

class AuthViewModel(
    private val auth: FirebaseAuth = FirebaseAuth.getInstance(),
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
) : ViewModel() {

    private val _uiState = MutableStateFlow(AuthUiState())
    val uiState: StateFlow<AuthUiState> = _uiState

    // Simple auth state snapshot
    val currentUser get() = auth.currentUser

    fun onDisplayNameChange(value: String) {
        _uiState.value = _uiState.value.copy(displayName = value, error = null)
    }

    fun onEmailChange(value: String) {
        _uiState.value = _uiState.value.copy(email = value, error = null)
    }

    fun onPasswordChange(value: String) {
        _uiState.value = _uiState.value.copy(password = value, error = null)
    }

    fun checkIfAuthenticated() {
        _uiState.value = _uiState.value.copy(
            isAuthenticated = auth.currentUser != null
        )
    }

    fun login(onSuccess: () -> Unit = {}) {
        val state = _uiState.value
        if (state.email.isBlank() || state.password.isBlank()) {
            _uiState.value = state.copy(error = "Email and password are required")
            return
        }

        _uiState.value = state.copy(isLoading = true, error = null)

        auth.signInWithEmailAndPassword(state.email.trim(), state.password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        isAuthenticated = true
                    )
                    onSuccess()
                } else {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = task.exception?.message ?: "Login failed"
                    )
                }
            }
    }

    fun signUp(onSuccess: () -> Unit = {}) {
        val state = _uiState.value
        if (state.displayName.isBlank() || state.email.isBlank() || state.password.isBlank()) {
            _uiState.value = state.copy(error = "All fields are required")
            return
        }

        _uiState.value = state.copy(isLoading = true, error = null)

        auth.createUserWithEmailAndPassword(state.email.trim(), state.password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val uid = auth.currentUser?.uid ?: return@addOnCompleteListener
                    val profile = UserProfile(
                        uid = uid,
                        displayName = state.displayName.trim(),
                        email = state.email.trim()
                    )
                    createUserProfile(profile, onSuccess)
                } else {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = task.exception?.message ?: "Sign up failed"
                    )
                }
            }
    }

    private fun createUserProfile(profile: UserProfile, onSuccess: () -> Unit) {
        firestore.collection("users")
            .document(profile.uid)
            .set(profile)
            .addOnSuccessListener {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    isAuthenticated = true
                )
                onSuccess()
            }
            .addOnFailureListener { e ->
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = e.message ?: "Failed to save profile"
                )
            }
    }

    fun signOut() {
        auth.signOut()
        _uiState.value = AuthUiState()
    }
}

