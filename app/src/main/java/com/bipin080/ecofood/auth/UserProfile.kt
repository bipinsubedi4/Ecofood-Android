package com.bipin080.ecofood.auth

data class UserProfile(
    val uid: String = "",
    val name: String = "",
    val email: String = "",
    val phone: String = "",
    val address: String = "",
    val photoUri: String = "" // local URI
)
