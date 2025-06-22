package com.example.futboldata.equipos.list

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.futboldata.databinding.ActivityEquiposBinding

class EquiposActivity : AppCompatActivity() {
    private lateinit var binding: ActivityEquiposBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEquiposBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Configura tu UI aquí
    }
}