package com.example.futboldata.auth

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.example.futboldata.databinding.ActivityLoginBinding
import com.example.futboldata.equipos.list.EquiposActivity
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.launch

class LoginActivity : AppCompatActivity() {
    private lateinit var binding: ActivityLoginBinding
    private val viewModel: LoginViewModel by viewModels {
        LoginViewModelFactory(AuthRepository())
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        if (viewModel.isUserLoggedIn) {
            navigateToMain()
            return
        }

        setupListeners()
        observeState()
    }

    private fun setupListeners() {
        binding.btnLogin.setOnClickListener {
            val email = binding.etEmail.text.toString().trim()
            val password = binding.etPassword.text.toString()

            if (validateInputs(email, password)) {
                viewModel.login(email, password)
            }
        }

        binding.btnForgotPassword.setOnClickListener {
            navigateToForgotPassword(it)
        }
    }

    fun navigateToForgotPassword(view: View) {
        Toast.makeText(this, "Función de recuperación de contraseña", Toast.LENGTH_SHORT).show()
        // startActivity(Intent(this, ForgotPasswordActivity::class.java))
    }

    private fun observeState() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.loginState.collect { state ->
                    when (state) {
                        is LoginViewModel.LoginState.Loading -> showLoading(true)
                        is LoginViewModel.LoginState.Success -> {
                            showLoading(false)
                            navigateToMain()
                        }
                        is LoginViewModel.LoginState.Error -> {
                            showLoading(false)
                            showError(state.message)
                        }
                        LoginViewModel.LoginState.Idle -> showLoading(false)
                    }
                }
            }
        }
    }

    private fun validateInputs(email: String, password: String): Boolean {
        if (email.isEmpty()) {
            binding.tilEmail.error = "Email requerido"
            return false
        }
        if (password.isEmpty()) {
            binding.tilPassword.error = "Contraseña requerida"
            return false
        }
        return true
    }

    private fun showLoading(loading: Boolean) {
        binding.progressBar.visibility = if (loading) View.VISIBLE else View.GONE
        binding.btnLogin.isEnabled = !loading
    }

    private fun showError(message: String) {
        Snackbar.make(binding.root, message, Snackbar.LENGTH_LONG).show()
    }

    private fun navigateToMain() {
        startActivity(Intent(this, EquiposActivity::class.java))
        finish()
    }
}