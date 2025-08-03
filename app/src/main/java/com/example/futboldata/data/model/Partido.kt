package com.example.futboldata.data.model

import com.google.firebase.firestore.Exclude
import java.util.*

data class Partido(
    val id: String = "",
    val equipoId: String = "",
    val fecha: Date = Date(),
    val rival: String = "",
    val golesEquipo: Int = 0,
    val golesRival: Int = 0,
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
    // Constructor sin argumentos para Firestore (corregido)
    constructor() : this(
        id = "",
        equipoId = "",
        fecha = Date(),
        rival = "",
        golesEquipo = 0,
        golesRival = 0,
        competicionId = "",
        competicionNombre = "",
        temporada = "",
        fase = null,
        jornada = null,
        esLocal = true,
        goleadores = emptyList(),
        asistentes = emptyList(),
        jugadorDelPartido = null,
        alineacion = emptyList()
    )

    @get:Exclude
    val resultado: String
        get() = "$golesEquipo-$golesRival"

    @Exclude
    fun obtenerEstadoPartido(): String = when {
        golesEquipo > golesRival -> "Victoria"
        golesEquipo < golesRival -> "Derrota"
        else -> "Empate"
    }

    @Exclude
    fun fueVictoria(): Boolean = golesEquipo > golesRival

    @Exclude
    fun getDiferenciaGoles(): Int = golesEquipo - golesRival
}