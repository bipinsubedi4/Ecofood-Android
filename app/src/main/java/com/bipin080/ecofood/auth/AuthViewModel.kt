package com.bipin080.ecofood.auth

import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class AuthViewModel : ViewModel() {

    private val auth = FirebaseAuth.getInstance()

    private val _loginState = MutableStateFlow<LoginState>(LoginState.Idle)
    val loginState = _loginState.asStateFlow()

    private val _signUpState = MutableStateFlow<SignUpState>(SignUpState.Idle)
    val signUpState = _signUpState.asStateFlow()

    // LOGIN
    fun login(email: String, password: String) {
        _loginState.value = LoginState.Loading

        if (email.isBlank() || password.isBlank()) {
            _loginState.value = LoginState.Error("Email and password cannot be empty.")
            return
        }

        auth.signInWithEmailAndPassword(email, password)
            .addOnSuccessListener {
                _loginState.value = LoginState.Success
            }
            .addOnFailureListener { e ->
                _loginState.value = LoginState.Error(e.message ?: "Incorrect email or password")
            }
    }

    // SIGN UP
    fun signUp(email: String, password: String) {
        _signUpState.value = SignUpState.Loading

        if (email.isBlank() || password.isBlank()) {
            _signUpState.value = SignUpState.Error("Email and password cannot be empty.")
            return
        }

        if (password.length < 6) {
            _signUpState.value = SignUpState.Error("Password must be at least 6 characters.")
            return
        }

        auth.createUserWithEmailAndPassword(email, password)
            .addOnSuccessListener {
                _signUpState.value = SignUpState.Success
            }
            .addOnFailureListener { e ->
                _signUpState.value = SignUpState.Error(e.message ?: "Sign-up failed")
            }
    }

    fun logout() {
        auth.signOut()
    }
}

sealed class LoginState {
    object Idle : LoginState()
    object Loading : LoginState()
    object Success : LoginState()
    data class Error(val message: String) : LoginState()
}

sealed class SignUpState {
    object Idle : SignUpState()
    object Loading : SignUpState()
    object Success : SignUpState()
    data class Error(val message: String) : SignUpState()
}
