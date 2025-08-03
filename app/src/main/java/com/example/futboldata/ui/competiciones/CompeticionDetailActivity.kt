package com.example.futboldata.ui.competiciones

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.futboldata.databinding.ActivityCompeticionDetailBinding

class CompeticionDetailActivity : AppCompatActivity() {
    private lateinit var binding: ActivityCompeticionDetailBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCompeticionDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val competicionId = intent.getStringExtra("COMPETICION_ID")
    }
}