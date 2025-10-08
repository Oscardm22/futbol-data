package com.example.futboldata.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.futboldata.data.repository.AuthRepository
import com.example.futboldata.utils.SessionManager
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class LoginViewModel(
    private val authRepository: AuthRepository,
    private val sessionManager: SessionManager
) : ViewModel() {

    sealed class LoginState {
        object Idle : LoginState()
        object Loading : LoginState()
        data class Success(val user: FirebaseUser) : LoginState()
        data class Error(val message: String) : LoginState()
    }

    private val _loginState = MutableStateFlow<LoginState>(LoginState.Idle)
    val loginState: StateFlow<LoginState> = _loginState

    fun login(email: String, password: String) {
        _loginState.value = LoginState.Loading
        viewModelScope.launch {
            authRepository.login(email, password)
                .onSuccess { user ->
                    sessionManager.saveUser(user.uid, user.email ?: "")
                    _loginState.value = LoginState.Success(user)
                }
                .onFailure { e ->
                    val errorMsg = when (e) {
                        is FirebaseAuthInvalidCredentialsException -> "Credenciales inválidas"
                        is FirebaseAuthInvalidUserException -> "Usuario no encontrado"
                        else -> "Error de autenticación: ${e.message}"
                    }
                    _loginState.value = LoginState.Error(errorMsg)
                }
        }
    }

    fun getCurrentUser(): FirebaseUser? {
        return authRepository.currentUser
    }

    fun sendPasswordResetEmail(email: String) = authRepository.sendPasswordResetEmail(email)
}