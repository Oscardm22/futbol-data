package com.example.futboldata.utils

import com.example.futboldata.data.model.Partido
import com.example.futboldata.data.model.Estadisticas

object StatsCalculator {
    fun calculate(partidos: List<Partido>): Estadisticas {
        val partidosJugados = partidos.size
        val victorias = partidos.count { it.fueVictoria() }
        val empates = partidos.count { it.obtenerEstadoPartido() == "Empate" }
        val derrotas = partidosJugados - victorias - empates

        val golesFavor = partidos.sumOf { it.golesEquipo }
        val golesContra = partidos.sumOf { it.golesRival }

        val promedioGoles = if (partidosJugados > 0) golesFavor.toDouble() / partidosJugados else 0.0
        val porcentajeVictorias = if (partidosJugados > 0) victorias.toDouble() / partidosJugados * 100 else 0.0

        return Estadisticas(
            promedioGoles = promedioGoles,
            porcentajeVictorias = porcentajeVictorias,
            posesionPromedio = 0.0,
            golesPorPartido = emptyMap(),
            partidosJugados = partidosJugados,
            victorias = victorias,
            empates = empates,
            derrotas = derrotas,
            golesFavor = golesFavor,
            golesContra = golesContra
        )
    }
}