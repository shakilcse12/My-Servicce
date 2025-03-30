package com.example.myservice.ui.login

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myservice.data.AuthRepository
import com.example.myservice.data.model.AuthResponse
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class LoginViewModel(
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _loginState = MutableStateFlow(LoginState())
    val loginState: StateFlow<LoginState> = _loginState.asStateFlow()

    fun login(email: String, password: String) {
        _loginState.value = LoginState(isLoading = true)

        viewModelScope.launch {
            try {
                val response = authRepository.login(email, password)

                if (response.isSuccessful) {
                    response.body()?.let { authResponse ->
                        if (authResponse.success) {
                            _loginState.value = LoginState(
                                isSuccess = true,
                                token = authResponse.data?.token,
                                userInfo = authResponse.data?.userInfo
                            )
                        } else {
                            _loginState.value = LoginState(
                                error = authResponse.data?.message ?: "Login failed"
                            )
                        }
                    } ?: run {
                        _loginState.value = LoginState(error = "Empty server response")
                    }
                } else {
                    _loginState.value = LoginState(
                        error = "Server error: ${response.code()}"
                    )
                }
            } catch (e: Exception) {
                _loginState.value = LoginState(
                    error = "Network error: ${e.message ?: "Unknown error"}"
                )
            }
        }
    }

    fun resetState() {
        _loginState.value = LoginState()
    }
}

data class LoginState(
    val isLoading: Boolean = false,
    val isSuccess: Boolean = false,
    val error: String? = null,
    val token: String? = null,
    val userInfo: com.example.myservice.data.model.UserInfo? = null
)