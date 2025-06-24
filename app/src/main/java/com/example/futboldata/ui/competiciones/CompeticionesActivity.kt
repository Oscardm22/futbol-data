package com.example.futboldata.ui.competiciones

import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.example.futboldata.adapter.CompeticionAdapter
import com.example.futboldata.databinding.ActivityCompeticionesBinding
import com.example.futboldata.viewmodel.CompeticionViewModel
import kotlinx.coroutines.launch

class CompeticionesActivity : AppCompatActivity() {
    private lateinit var binding: ActivityCompeticionesBinding
    private val viewModel: CompeticionViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCompeticionesBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val adapter = CompeticionAdapter { competicion ->
            // Manejar clic en competición
        }

        binding.rvCompeticiones.adapter = adapter

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.competiciones.collect { competiciones ->
                    adapter.submitList(competiciones)
                }
            }
        }
    }
}
