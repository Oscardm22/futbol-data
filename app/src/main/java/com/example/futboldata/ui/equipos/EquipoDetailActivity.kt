// En ui/equipos/EquipoDetailActivity.kt
package com.example.futboldata.ui.equipos

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.futboldata.databinding.ActivityEquipoDetailBinding

class EquipoDetailActivity : AppCompatActivity() {
    private lateinit var binding: ActivityEquipoDetailBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEquipoDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val equipoId = intent.getStringExtra("equipo_id")
        // Configura la vista con los datos del equipo
    }
}