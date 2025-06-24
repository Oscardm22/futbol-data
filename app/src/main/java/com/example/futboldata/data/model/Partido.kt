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
    val fase: String = "",
    val jornada: Int? = null,
    val goleadores: List<Gol> = emptyList(),
    val asistentes: List<Asistencia> = emptyList(),
    val jugadorDelPartido: String? = null,
    // Nuevo campo para la alineación completa
    val alineacion: List<ParticipacionJugador> = emptyList()
) {
    // Constructor sin argumentos para Firestore
    constructor() : this("", "", Date(), "", "0-0", "", "", "", "",
        null, emptyList(), emptyList(), null)

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
}