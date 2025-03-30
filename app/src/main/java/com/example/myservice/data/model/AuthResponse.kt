package com.example.myservice.data.model

import com.google.gson.annotations.SerializedName

data class AuthResponse(
    @SerializedName("success") val success: Boolean,
    @SerializedName("data") val data: AuthData?
)

data class AuthData(
    @SerializedName("userinfo") val userInfo: UserInfo,
    @SerializedName("message") val message: String,
    @SerializedName("token") val token: String
)

data class UserInfo(
    @SerializedName("id") val id: Long,
    @SerializedName("name") val name: String,
    @SerializedName("email") val email: String,
    @SerializedName("email_verified_at") val emailVerifiedAt: String?,
    @SerializedName("avatar") val avatar: String?,
    @SerializedName("created_at") val createdAt: String,
    @SerializedName("updated_at") val updatedAt: String
)