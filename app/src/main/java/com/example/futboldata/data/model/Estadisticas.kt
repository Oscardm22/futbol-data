package com.example.futboldata.data.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Estadisticas(
    val promedioGoles: Number,
    val porcentajeVictorias: Number,
    val posesionPromedio: Number,
    val golesPorPartido: Map<String, Int>,
    val partidosJugados: Int = 0,
    val victorias: Int = 0,
    val empates: Int = 0,
    val derrotas: Int = 0,
    val golesFavor: Int = 0,
    val golesContra: Int = 0
) : Parcelable {
    companion object {
        fun empty() = Estadisticas(
            promedioGoles = 0.0,
            porcentajeVictorias = 0.0,
            posesionPromedio = 0.0,
            golesPorPartido = emptyMap(),
            partidosJugados = 0,
            victorias = 0,
            empates = 0,
            derrotas = 0,
            golesFavor = 0,
            golesContra = 0
        )
    }

    fun getPromedioGolesDouble() = promedioGoles.toDouble()
    fun getPorcentajeVictoriasDouble() = porcentajeVictorias.toDouble()
    fun getPosesionPromedioDouble() = posesionPromedio.toDouble()
}