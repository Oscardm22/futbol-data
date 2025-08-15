package com.example.futboldata.ui.equipos.fragments

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.example.futboldata.data.model.Estadisticas
import com.example.futboldata.databinding.FragmentStatsBinding
import com.example.futboldata.viewmodel.EquipoDetailViewModel
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry

class StatsFragment : Fragment() {
    private var _binding: FragmentStatsBinding? = null
    private val binding get() = _binding!!
    private val viewModel: EquipoDetailViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentStatsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupObservers()
    }

    private fun setupObservers() {
        viewModel.estadisticas.observe(viewLifecycleOwner) { stats ->
            stats?.let {
                if (stats.partidosJugados > 0) {
                    updateCharts(stats)
                    binding.progressBar.visibility = View.GONE
                } else {
                    showNoDataMessage()
                }
            } ?: showNoDataMessage()
        }
    }

    private fun updateCharts(stats: Estadisticas) {
        // Configura el gráfico de torta (victorias/empates/derrotas)
        val pieEntries = listOf(
            PieEntry(stats.victorias.toFloat(), "Victorias"),
            PieEntry(stats.empates.toFloat(), "Empates"),
            PieEntry(stats.derrotas.toFloat(), "Derrotas")
        )

        val pieDataSet = PieDataSet(pieEntries, "").apply {
            colors = listOf(Color.GREEN, Color.YELLOW, Color.RED)
            valueTextColor = Color.BLACK
        }

        binding.pieChart.data = PieData(pieDataSet)
        binding.pieChart.description.isEnabled = false
        binding.pieChart.animateY(1000)
        binding.pieChart.invalidate()

        // Actualiza los TextViews
        binding.tvAvgGoals.text = "Promedio goles: ${stats.promedioGoles}"
        binding.tvWinRate.text = "Porcentaje victorias: ${stats.porcentajeVictorias}%"
    }

    private fun showNoDataMessage() {
        binding.pieChart.clear()
        binding.tvAvgGoals.text = "No hay datos disponibles"
        binding.tvWinRate.text = ""
        binding.progressBar.visibility = View.GONE
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        fun newInstance() = StatsFragment()
    }
}