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
    val jugadorDelPartido: String? = null,
    val alineacionIds: List<String> = emptyList(),
    val goleadoresIds: List<String> = emptyList(),
    val asistentesIds: List<String> = emptyList(),
    val porteroImbatidoId: String? = null,
    val autogolesFavor: Int = 0

) {
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

    @Exclude
    fun fuePorteriaImbatida(): Boolean = porteroImbatidoId != null

    @Exclude
    fun esValido(): Boolean {
        return equipoId.isNotBlank() && rival.isNotBlank() &&
                competicionId.isNotBlank() && temporada.isNotBlank() &&
                golesEquipo >= 0 && golesRival >= 0
    }
}