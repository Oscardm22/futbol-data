package com.example.futboldata.ui.equipos.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.example.futboldata.R
import com.example.futboldata.data.model.Estadisticas
import com.example.futboldata.databinding.FragmentStatsBinding
import com.example.futboldata.viewmodel.StatsViewModel
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry

class StatsFragment : Fragment() {
    private var _binding: FragmentStatsBinding? = null
    private val binding get() = _binding!!
    private val viewModel: StatsViewModel by viewModels()

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

        val equipoId = arguments?.getString("equipoId") ?: return

        viewModel.statsState.observe(viewLifecycleOwner) { state ->
            when (state) {
                is StatsViewModel.StatsState.Loading -> showLoading()
                is StatsViewModel.StatsState.Success -> showStats(state.data)
                is StatsViewModel.StatsState.Error -> showError(state.message)
            }
        }

        viewModel.loadStats(equipoId)
        setupChartViews()
    }

    private fun showLoading() {
        binding.progressBar.visibility = View.VISIBLE
    }

    private fun updatePieChart(posesion: Float) {
        // Configurar datos para el gráfico de pie
        val entries = listOf(
            PieEntry(posesion, "Tu equipo"),
            PieEntry(100f - posesion, "Rival")
        )

        val dataSet = PieDataSet(entries, "Distribución de posesión").apply {
            colors = listOf(
                ContextCompat.getColor(requireContext(), R.color.team_primary),
                ContextCompat.getColor(requireContext(), R.color.rival_primary)
            )
            valueTextSize = 12f
        }

        binding.pieChart.apply {
            data = PieData(dataSet)
            description.isEnabled = false
            legend.isEnabled = true
            animateY(1000)
            invalidate() // Refrescar el gráfico
        }
    }

    private fun updateBarChart(goles: List<Float>) {
        // Crear entradas para el gráfico de barras
        val entries = goles.mapIndexed { index, goles ->
            BarEntry(index.toFloat(), goles)
        }

        val dataSet = BarDataSet(entries, "Goles por partido").apply {
            color = ContextCompat.getColor(requireContext(), R.color.goal_color)
            valueTextSize = 12f
        }

        binding.barChart.apply {
            data = BarData(dataSet)
            description.isEnabled = false
            legend.isEnabled = true
            animateY(1000)
            invalidate() // Refrescar el gráfico
        }
    }

    private fun showStats(stats: Estadisticas) {
        binding.progressBar.visibility = View.GONE
        binding.apply {
            // Conversión explícita de Double a Float para los textos
            tvAvgGoals.text = getString(R.string.avg_goals_format, stats.promedioGoles.toFloat())
            tvWinRate.text = getString(R.string.win_rate_format, stats.porcentajeVictorias.toFloat())

            // Actualizar gráficos
            updatePieChart(stats.posesionPromedio.toFloat())

            // Convertir Map<String, Int> a List<Float> para el gráfico de barras
            updateBarChart(stats.golesPorPartido.values.map { it.toFloat() })
        }
    }

    private fun showError(message: String) {
        binding.progressBar.visibility = View.GONE
        Toast.makeText(requireContext(), message, Toast.LENGTH_LONG).show()
    }

    private fun setupChartViews() {
        binding.pieChart.apply {
            setUsePercentValues(true)
            description.isEnabled = false
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}