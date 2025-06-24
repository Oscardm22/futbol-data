package com.example.futboldata.ui.auth

import android.content.Intent
import android.os.Bundle
import android.util.Patterns
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.futboldata.databinding.ActivityLoginBinding
import com.example.futboldata.ui.equipos.EquiposActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

class LoginActivity : AppCompatActivity() {
    private lateinit var binding: ActivityLoginBinding
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = Firebase.auth

        setupLoginButton()
        setupForgotPasswordButton()
    }

    private fun setupLoginButton() {
        binding.btnLogin.setOnClickListener {
            val email = binding.etEmail.text.toString().trim()
            val password = binding.etPassword.text.toString().trim()

            if (!validateForm(email, password)) {
                return@setOnClickListener
            }

            // Credenciales hardcodeadas solo para desarrollo
            if (email == "oscarj.rierav@gmail.com" && password == "12345678") {
                navigateToEquipos()
            } else {
                showLoginError("Credenciales incorrectas")
            }

            binding.progressBar.visibility = View.VISIBLE
            binding.btnLogin.isEnabled = false

            auth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this) { task ->
                    binding.progressBar.visibility = View.GONE
                    binding.btnLogin.isEnabled = true

                    if (task.isSuccessful) {
                        navigateToEquipos()
                    } else {
                        showLoginError(task.exception?.message ?: "Error desconocido")
                    }
                }
        }
    }

    private fun validateForm(email: String, password: String): Boolean {
        var isValid = true

        // Validación de email
        if (email.isEmpty()) {
            binding.tilEmail.error = "Email requerido"
            isValid = false
        } else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            binding.tilEmail.error = "Email no válido"
            isValid = false
        } else {
            binding.tilEmail.error = null
        }

        // Validación de contraseña
        if (password.isEmpty()) {
            binding.tilPassword.error = "Contraseña requerida"
            isValid = false
        } else if (password.length < 6) {
            binding.tilPassword.error = "Mínimo 6 caracteres"
            isValid = false
        } else {
            binding.tilPassword.error = null
        }

        return isValid
    }

    private fun showLoginError(errorMessage: String) {
        Toast.makeText(
            this,
            "Error en login: $errorMessage",
            Toast.LENGTH_LONG
        ).show()
    }

    private fun navigateToEquipos() {
        startActivity(Intent(this, EquiposActivity::class.java))
        finish()
    }

    private fun setupForgotPasswordButton() {
        binding.btnForgotPassword.setOnClickListener {
            // Implementa recuperación de contraseña si es necesario
            Toast.makeText(
                this,
                "Función de recuperación de contraseña",
                Toast.LENGTH_SHORT
            ).show()
        }
    }
}