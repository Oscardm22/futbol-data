package com.example.futboldata.ui.auth

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.lifecycleScope
import com.example.futboldata.FutbolDataApp
import com.example.futboldata.databinding.ActivityLoginBinding
import com.example.futboldata.ui.equipos.EquiposActivity
import com.example.futboldata.utils.SessionManager
import com.example.futboldata.viewmodel.LoginViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding
    private lateinit var sessionManager: SessionManager
    private val viewModel: LoginViewModel by viewModels {
        (application as FutbolDataApp).viewModelFactory
    }
    private var keepSplashOnScreen = true // Controla cuándo ocultar el splash

    override fun onCreate(savedInstanceState: Bundle?) {
        val splashScreen = installSplashScreen()
        super.onCreate(savedInstanceState)

        splashScreen.setKeepOnScreenCondition { keepSplashOnScreen }

        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        sessionManager = SessionManager(this)

        val auth = FirebaseAuth.getInstance()
        Log.d("LoginActivity", "=== LOGIN ACTIVITY ===")
        Log.d("LoginActivity", "Firebase user: ${auth.currentUser?.uid}")
        Log.d("LoginActivity", "Session user: ${sessionManager.getCurrentUserUid()}")

        // Verifica si el usuario ya está autenticado
        checkAuthState()

        setupObservers()
        setupLoginButton()
        setupForgotPasswordButton()
    }

    private fun checkAuthState() {
        val auth = FirebaseAuth.getInstance()

        val hasFirebaseUser = auth.currentUser != null
        val hasSessionUser = sessionManager.isUserLoggedIn()

        Log.d("LoginActivity", "Firebase user: ${auth.currentUser?.uid}")
        Log.d("LoginActivity", "Session user: ${sessionManager.getCurrentUserUid()}")

        if (hasFirebaseUser || hasSessionUser) {
            Log.d("LoginActivity", "✅ USUARIO ENCONTRADO - Redirigiendo a Equipos")
            // Pequeño delay para mostrar el splash screen
            binding.root.postDelayed({
                navigateToEquipos()
            }, 1000) // 1 segundo de delay
        } else {
            Log.d("LoginActivity", "❌ NO HAY USUARIO - Mostrando formulario de login")
            // Una vez que sabemos que no hay usuario autenticado, ocultamos el splash
            keepSplashOnScreen = false
        }
    }

    private fun setupObservers() {
        viewModel.loginState.onEach { state ->
            when (state) {
                is LoginViewModel.LoginState.Loading -> showLoading(true)
                is LoginViewModel.LoginState.Success -> {
                    showLoading(false)
                    // Guardar sesión después de login exitoso
                    val user = FirebaseAuth.getInstance().currentUser
                    user?.let {
                        sessionManager.saveUser(it.uid, it.email ?: "")
                        Log.d("LoginActivity", "Sesión guardada: UID=${it.uid}, Email=${it.email}")
                    }
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

            binding.tilEmail.error = null
            binding.tilPassword.error = null

            when {
                email.isEmpty() -> {
                    binding.tilEmail.error = "Ingresa tu email"
                    binding.etEmail.requestFocus()
                }
                password.isEmpty() -> {
                    binding.tilPassword.error = "Ingresa tu contraseña"
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