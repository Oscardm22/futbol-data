package com.example.futboldata.data.model

import com.google.firebase.firestore.Exclude
import java.util.*

data class Partido(
    val id: String = "",
    val equipoId: String = "",
    val fecha: Date = Date(),
    val rival: String = "",
    val resultado: String = "0-0",
    val competicionId: String = "",
    val competicionNombre: String = "",
    val temporada: String = "",
    val fase: String? = null,
    val jornada: Int? = null,
    val esLocal: Boolean = true,
    val goleadores: List<Gol> = emptyList(),
    val asistentes: List<Asistencia> = emptyList(),
    val jugadorDelPartido: String? = null,
    val alineacion: List<ParticipacionJugador> = emptyList()
) {
    // Constructor sin argumentos para Firestore
    constructor() : this("", "", Date(), "", "0-0", "", "", "", "",
        null, true, emptyList(), emptyList(), null, emptyList())

    @Exclude
    fun getGolesAFavor(): Int = resultado.split("-")[0].toIntOrNull() ?: 0

    @Exclude
    fun getGolesEnContra(): Int = resultado.split("-")[1].toIntOrNull() ?: 0

    @Exclude
    fun obtenerEstadoPartido(): String = when {
        getGolesAFavor() > getGolesEnContra() -> "Victoria"
        getGolesAFavor() < getGolesEnContra() -> "Derrota"
        else -> "Empate"
    }

    @Exclude
    fun fueVictoria(): Boolean {
        return if (esLocal) {
            getGolesAFavor() > getGolesEnContra()
        } else {
            getGolesEnContra() > getGolesAFavor()
        }
    }

    @Exclude
    fun getGolesRival(): Int = if (esLocal) getGolesEnContra() else getGolesAFavor()

    @Exclude
    fun resultadoValido(): Boolean {
        return try {
            val partes = resultado.split("-")
            partes.size == 2 && partes[0].toIntOrNull() != null && partes[1].toIntOrNull() != null
        } catch (e: Exception) {
            false
        }
    }

    @Exclude
    fun getGolesEquipo(): Int {
        return if (!resultadoValido()) 0 else {
            val partes = resultado.split("-")
            if (esLocal) partes[0].toInt() else partes[1].toInt()
        }
    }
}