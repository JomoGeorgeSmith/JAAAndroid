package com.example.jaa.model

data class AuthResponse(
    val success: Boolean,
    val vehicles: List<Vehicle>
)