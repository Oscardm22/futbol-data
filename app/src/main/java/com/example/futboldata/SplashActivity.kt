package com.example.futboldata

import android.animation.Animator
import android.app.ActivityOptions
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.airbnb.lottie.LottieAnimationView
import com.example.futboldata.ui.auth.LoginActivity
import com.example.futboldata.ui.equipos.EquiposActivity
import com.example.futboldata.utils.SessionManager
import com.google.firebase.auth.FirebaseAuth

class SplashActivity : AppCompatActivity() {
    private val splashDelay = 3000L
    private var animationCompleted = false
    private val handler = Handler(Looper.getMainLooper())

    override fun onCreate(savedInstanceState: Bundle?) {
        // Establecer el tema antes de setContentView
        setTheme(R.style.Theme_FutbolData)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        // Configurar animación Lottie con manejo de errores
        try {
            val animationView = findViewById<LottieAnimationView>(R.id.animation_view)
            animationView.enableMergePathsForKitKatAndAbove(true)
            animationView.setAnimation(R.raw.animation)
            animationView.playAnimation()

            animationView.addAnimatorListener(object : Animator.AnimatorListener {
                override fun onAnimationStart(animation: Animator) {}
                override fun onAnimationEnd(animation: Animator) {
                    animationCompleted = true
                    proceedToLogin()
                }
                override fun onAnimationCancel(animation: Animator) {
                    animationCompleted = true
                    proceedToLogin()
                }
                override fun onAnimationRepeat(animation: Animator) {}
            })
        } catch (e: Exception) {
            Log.e("SplashActivity", "Error en animación Lottie", e)
            proceedToLogin()
        }

        // Plan B por si la animación falla o tarda demasiado
        handler.postDelayed({
            if (!animationCompleted) {
                proceedToLogin()
            }
        }, splashDelay)
    }

    private fun proceedToLogin() {
        if (!isFinishing) {
            handler.removeCallbacksAndMessages(null)

            val auth = FirebaseAuth.getInstance()
            val sessionManager = SessionManager(this)

            val hasFirebaseUser = auth.currentUser != null
            val hasSessionUser = sessionManager.isUserLoggedIn()

            Log.d("SplashActivity", "Firebase user: ${auth.currentUser?.uid}")
            Log.d("SplashActivity", "Session user: ${sessionManager.getCurrentUserUid()}")

            val intent = if (hasFirebaseUser || hasSessionUser) {
                Log.d("SplashActivity", "✅ USUARIO ENCONTRADO - Redirigiendo a Equipos")
                Intent(this, EquiposActivity::class.java)
            } else {
                Log.d("SplashActivity", "❌ NO HAY USUARIO - Redirigiendo a Login")
                Intent(this, LoginActivity::class.java)
            }

            val options = ActivityOptions.makeCustomAnimation(
                this,
                android.R.anim.fade_in,
                android.R.anim.fade_out
            )

            startActivity(intent, options.toBundle())
            finish()
        }
    }

    override fun onDestroy() {
        handler.removeCallbacksAndMessages(null) // Limpiar handlers
        findViewById<LottieAnimationView>(R.id.animation_view)?.cancelAnimation()
        super.onDestroy()
    }
}