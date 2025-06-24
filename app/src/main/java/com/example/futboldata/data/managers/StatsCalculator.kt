package com.example.futboldata.data.managers

import com.example.futboldata.data.model.Estadisticas
import com.example.futboldata.data.model.Partido

class StatsCalculator {
    fun calculate(partidos: List<Partido>): Estadisticas {
        if (partidos.isEmpty()) return Estadisticas.empty()

        return Estadisticas(
            promedioGoles = partidos.map { it.getGolesAFavor().toDouble() }.average(),
            porcentajeVictorias = calcularPorcentajeVictorias(partidos),
            posesionPromedio = 0.0,
            golesPorPartido = partidos.associate {
                it.fecha.toString() to it.getGolesAFavor()
            }
        )
    }

    private fun calcularPorcentajeVictorias(partidos: List<Partido>): Double {
        val victorias = partidos.count {
            it.obtenerEstadoPartido() == "Victoria" // Usamos el método getResultado()
        }
        return if (partidos.isNotEmpty()) {
            (victorias.toDouble() / partidos.size) * 100
        } else {
            0.0
        }
    }
}