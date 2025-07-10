package com.example.futboldata.ui.auth

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.futboldata.FutbolDataApp
import com.example.futboldata.databinding.ActivityLoginBinding
import com.example.futboldata.ui.equipos.EquiposActivity
import com.example.futboldata.viewmodel.LoginViewModel
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding
    private val viewModel: LoginViewModel by viewModels {
        (application as FutbolDataApp).viewModelFactory
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        if (viewModel.isUserLoggedIn) {
            navigateToEquipos()
            return
        }

        setupObservers()
        setupLoginButton()
        setupForgotPasswordButton()
    }

    private fun setupObservers() {
        viewModel.loginState.onEach { state ->
            when (state) {
                is LoginViewModel.LoginState.Loading -> showLoading(true)
                is LoginViewModel.LoginState.Success -> {
                    showLoading(false)
                    navigateToEquipos()
                }
                is LoginViewModel.LoginState.Error -> {
                    showLoading(false)
                    showError(state.message)
                }
                LoginViewModel.LoginState.Idle -> showLoading(false)
            }
        }.launchIn(lifecycleScope)
    }

    private fun setupLoginButton() {
        binding.btnLogin.setOnClickListener {
            val email = binding.etEmail.text.toString().trim()
            val password = binding.etPassword.text.toString().trim()

            when {
                email.isEmpty() -> {
                    binding.etEmail.error = "Ingresa tu email"
                    binding.etEmail.requestFocus()
                }
                password.isEmpty() -> {
                    binding.etPassword.error = "Ingresa tu contraseña"
                    binding.etPassword.requestFocus()
                }
                else -> viewModel.login(email, password)
            }
        }
    }

    private fun setupForgotPasswordButton() {
        binding.btnForgotPassword.setOnClickListener {
            val email = binding.etEmail.text.toString().trim()

            if (email.isEmpty()) {
                binding.etEmail.error = "Ingresa tu email para recuperar contraseña"
                binding.etEmail.requestFocus()
                return@setOnClickListener
            }

            showLoading(true)
            binding.btnForgotPassword.isEnabled = false

            lifecycleScope.launch {
                try {
                    viewModel.sendPasswordResetEmail(email)
                    Toast.makeText(
                        this@LoginActivity,
                        "Email de recuperación enviado a tu correo.",
                        Toast.LENGTH_LONG
                    ).show()
                } catch (e: Exception) {
                    val errorMessage = when (e) {
                        is FirebaseAuthInvalidUserException -> "Este email no está registrado"
                        is FirebaseAuthInvalidCredentialsException -> "Formato de email inválido"
                        else -> "Error: ${e.message}"
                    }
                    Toast.makeText(this@LoginActivity, errorMessage, Toast.LENGTH_LONG).show()
                } finally {
                    showLoading(false)
                    binding.btnForgotPassword.isEnabled = true
                }
            }
        }
    }

    private fun showLoading(isLoading: Boolean) {
        binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        binding.btnLogin.isEnabled = !isLoading
        binding.btnForgotPassword.isEnabled = !isLoading
    }

    private fun showError(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show()
    }

    private fun navigateToEquipos() {
        startActivity(Intent(this, EquiposActivity::class.java))
        finish()
    }
}