package com.example.futboldata

import android.animation.Animator
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.airbnb.lottie.LottieAnimationView
import com.example.futboldata.auth.LoginActivity

class SplashActivity : AppCompatActivity() {
    private val SPLASH_DELAY = 3000L // 3 segundos
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
        }, SPLASH_DELAY)
    }

    private fun proceedToLogin() {
        if (!isFinishing) {
            handler.removeCallbacksAndMessages(null) // Cancelar cualquier handler pendiente
            startActivity(Intent(this, LoginActivity::class.java))
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
            finish()
        }
    }

    override fun onDestroy() {
        handler.removeCallbacksAndMessages(null) // Limpiar handlers
        findViewById<LottieAnimationView>(R.id.animation_view)?.cancelAnimation()
        super.onDestroy()
    }
}