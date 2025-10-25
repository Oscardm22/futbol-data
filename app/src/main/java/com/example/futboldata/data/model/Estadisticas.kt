package com.example.futboldata.data.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Estadisticas(
    val promedioGoles: Double = 0.0,
    val porcentajeVictorias: Double = 0.0,
    val posesionPromedio: Double = 0.0,
    val golesPorPartido: Map<String, Int> = emptyMap(),
    val partidosJugados: Int = 0,
    val victorias: Int = 0,
    val empates: Int = 0,
    val derrotas: Int = 0,
    val golesFavor: Int = 0,
    val golesContra: Int = 0
) : Parcelable {
    // Constructor sin argumentos
    constructor() : this(0.0, 0.0, 0.0, emptyMap(), 0, 0, 0, 0, 0, 0)

    companion object {
        fun empty() = Estadisticas()
    }
}