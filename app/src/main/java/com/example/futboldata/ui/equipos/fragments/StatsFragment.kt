package com.example.futboldata.ui.equipos.fragments

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.example.futboldata.R
import com.example.futboldata.data.model.Estadisticas
import com.example.futboldata.databinding.FragmentStatsBinding
import com.example.futboldata.viewmodel.EquipoDetailViewModel
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter

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
        val pieEntries = listOf(
            PieEntry(stats.victorias.toFloat(), "Victorias"),
            PieEntry(stats.empates.toFloat(), "Empates"),
            PieEntry(stats.derrotas.toFloat(), "Derrotas")
        )

        val pieDataSet = PieDataSet(pieEntries, "").apply {
            colors = listOf(Color.GREEN, Color.YELLOW, Color.RED)
            valueTextColor = Color.BLACK
            valueTextSize = 12f
            setDrawValues(true)
        }

        binding.pieChart.apply {
            data = PieData(pieDataSet)
            setEntryLabelColor(Color.BLACK)
            setEntryLabelTextSize(10f)
            description.isEnabled = false
            animateY(1000)
            invalidate()
        }

        // 2. Nueva configuración para el BarChart
        setupBarChart(stats)

        // Actualiza los TextViews
        binding.tvAvgGoals.text = getString(R.string.promedio_goles, stats.promedioGoles)
        binding.tvWinRate.text = getString(R.string.porcentaje_victorias, stats.porcentajeVictorias)
    }

    private fun setupBarChart(stats: Estadisticas) {
        // 1. Prepara los datos y etiquetas
        val entries = listOf(
            BarEntry(0f, stats.golesFavor.toFloat()),
            BarEntry(1f, stats.golesContra.toFloat())
        )

        val labels = listOf("A Favor", "En Contra") // Lista de etiquetas única

        val dataSet = BarDataSet(entries, "").apply {
            color = Color.BLUE
            valueTextColor = Color.BLACK
            valueTextSize = 12f
        }

        // 2. Configuración avanzada del gráfico
        binding.barChart.apply {
            data = BarData(dataSet)

            // Configuración del eje X
            xAxis.apply {
                valueFormatter = IndexAxisValueFormatter(labels)
                position = XAxis.XAxisPosition.BOTTOM
                granularity = 1f
                setDrawGridLines(false)
            }

            // Configuración del eje Y izquierdo
            axisLeft.apply {
                axisMinimum = 0f
                granularity = 1f
            }

            // Deshabilitar eje Y derecho
            axisRight.isEnabled = false

            // Otras configuraciones
            description.isEnabled = false
            legend.isEnabled = false
            setFitBars(true)
            animateY(1000)
            invalidate()
        }
    }

    private fun showNoDataMessage() {
        binding.pieChart.clear()
        binding.tvAvgGoals.text = getString(R.string.no_hay_datos)
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