package com.example.myservice.network

import com.example.myservice.data.constants.Api
import com.example.myservice.data.model.AuthResponse
import retrofit2.http.Body
import retrofit2.http.POST

interface AuthService {
    @POST(Api.LOGIN_ENDPOINT)
    suspend fun login(
        @Body request: LoginRequest
    ): retrofit2.Response<AuthResponse>
}

data class LoginRequest(
    val email: String,
    val password: String
)