package com.example.myservice.data

import com.example.myservice.data.model.AuthResponse
import com.example.myservice.network.AuthService
import com.example.myservice.network.LoginRequest


class AuthRepository(private val authService: AuthService) {
    // Return raw Retrofit response instead of Result
    suspend fun login(email: String, password: String): retrofit2.Response<AuthResponse> {
        return authService.login(LoginRequest(email, password))
    }
}