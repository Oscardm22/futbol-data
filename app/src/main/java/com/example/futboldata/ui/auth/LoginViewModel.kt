package com.example.futboldata.ui.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class LoginViewModel(private val authRepository: AuthRepository) : ViewModel() {
    // Definición de LoginState
    sealed class LoginState {
        object Idle : LoginState()
        object Loading : LoginState()
        data class Success(val user: Any) : LoginState() // Cambia Any por tu tipo de usuario
        data class Error(val message: String) : LoginState()
    }

    private val _loginState = MutableStateFlow<LoginState>(LoginState.Idle)
    val loginState: StateFlow<LoginState> = _loginState

    fun login(email: String, password: String) {
        _loginState.value = LoginState.Loading
        viewModelScope.launch {
            try {
                val result = authRepository.login(email, password)
                _loginState.value = LoginState.Success(result)
            } catch (e: Exception) {
                _loginState.value = LoginState.Error(e.message ?: "Error desconocido")
            }
        }
    }

    val isUserLoggedIn: Boolean
        get() = authRepository.currentUser != null
}