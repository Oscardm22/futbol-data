package com.example.futboldata

import android.animation.Animator
import android.content.Intent
import android.os.Bundle
import android.os.Looper
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.airbnb.lottie.LottieAnimationView
import android.os.Handler

class SplashActivity : AppCompatActivity() {
    private val SPLASH_DELAY = 3000L

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
                override fun onAnimationEnd(animation: Animator) { proceedToMain() }
                override fun onAnimationCancel(animation: Animator) { proceedToMain() }
                override fun onAnimationRepeat(animation: Animator) {}
            })
        } catch (e: Exception) {
            Log.e("SplashActivity", "Error en animación Lottie", e)
            proceedToMain()
        }

        // Plan B por si la animación falla
        Handler(Looper.getMainLooper()).postDelayed({
            proceedToMain()
        }, SPLASH_DELAY)
    }

    private fun proceedToMain() {
        if (!isFinishing) {
            startActivity(Intent(this, MainActivity::class.java))
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
            finish()
        }
    }

    override fun onDestroy() {
        findViewById<LottieAnimationView>(R.id.animation_view)?.cancelAnimation()
        super.onDestroy()
    }
}