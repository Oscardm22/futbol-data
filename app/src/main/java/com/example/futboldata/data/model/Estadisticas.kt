package com.example.futboldata.data.model

data class Estadisticas(
    val promedioGoles: Double,
    val porcentajeVictorias: Double,
    val posesionPromedio: Double,
    val golesPorPartido: Map<String, Int>
) {
    companion object {
        fun empty() = Estadisticas(
            promedioGoles = 0.0,
            porcentajeVictorias = 0.0,
            posesionPromedio = 0.0,
            golesPorPartido = emptyMap()
        )
    }
}