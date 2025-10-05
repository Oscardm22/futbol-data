package com.example.futboldata.data.model

import android.os.Parcelable
import com.google.firebase.firestore.Exclude
import kotlinx.parcelize.Parcelize
import kotlin.math.max

@Parcelize
data class Jugador(
    val id: String = "",
    val nombre: String,
    val posicion: Posicion,
    val equipoId: String,
    val activo: Boolean = true,

    // Estadísticas globales
    val partidosJugados: Int = 0,
    val goles: Int = 0,
    val asistencias: Int = 0,
    val porteriasImbatidas: Int = 0,
    val mvp: Int = 0,

    // Estadísticas por competición
    @get:Exclude
    val partidosPorCompeticion: Map<String, Int> = emptyMap(),
    @get:Exclude
    val golesPorCompeticion: Map<String, Int> = emptyMap(),
    @get:Exclude
    val asistenciasPorCompeticion: Map<String, Int> = emptyMap(),
    @get:Exclude
    val porteriasImbatidasPorCompeticion: Map<String, Int> = emptyMap(),
    @get:Exclude
    val mvpPorCompeticion: Map<String, Int> = emptyMap()

) : Parcelable {

    constructor() : this("", "", Posicion.PO, "", true)

    companion object {
        @Exclude
        fun fromFirestore(
            id: String,
            data: Map<String, Any>
        ): Jugador {
            // Función de ayuda para convertir mapas de competición de forma segura
            fun safeCastToIntMap(value: Any?): Map<String, Int> {
                return when (value) {
                    is Map<*, *> -> value.mapNotNull { (key, v) ->
                        when {
                            key !is String -> null
                            v is Int -> key to v
                            v is Long -> key to v.toInt()
                            else -> null
                        }
                    }.toMap()
                    else -> emptyMap()
                }
            }

            return Jugador(
                id = id,
                nombre = data["nombre"] as? String ?: "",
                posicion = Posicion.valueOf(data["posicion"] as? String ?: "PO"),
                equipoId = data["equipoId"] as? String ?: "",
                activo = data["activo"] as? Boolean != false,
                partidosJugados = (data["partidosJugados"] as? Long)?.toInt() ?: 0,
                goles = (data["goles"] as? Long)?.toInt() ?: 0,
                asistencias = (data["asistencias"] as? Long)?.toInt() ?: 0,
                porteriasImbatidas = (data["porteriasImbatidas"] as? Long)?.toInt() ?: 0,
                partidosPorCompeticion = safeCastToIntMap(data["partidosPorCompeticion"]),
                golesPorCompeticion = safeCastToIntMap(data["golesPorCompeticion"]),
                asistenciasPorCompeticion = safeCastToIntMap(data["asistenciasPorCompeticion"]),
                porteriasImbatidasPorCompeticion = safeCastToIntMap(data["porteriasImbatidasPorCompeticion"]),
                mvp = (data["mvp"] as? Long)?.toInt() ?: 0,
                mvpPorCompeticion = safeCastToIntMap(data["mvpPorCompeticion"])
            )
        }
    }

    @Exclude
    fun actualizarEstadisticasPartido(partido: Partido): Jugador {
        val jugoEnPartido = partido.alineacionIds.contains(id)
        val nuevosGoles = partido.goleadoresIds.count { it == id }
        val nuevasAsistencias = partido.asistentesIds.count { it == id }
        val porteriaImbatida = id == partido.porteroImbatidoId
        val esMVP = id == partido.jugadorDelPartido

        return if (!jugoEnPartido) {
            this
        } else {
            this.copy(
                partidosJugados = partidosJugados + 1,
                goles = goles + nuevosGoles,
                asistencias = asistencias + nuevasAsistencias,
                porteriasImbatidas = porteriasImbatidas + (if (porteriaImbatida) 1 else 0),
                mvp = mvp + (if (esMVP) 1 else 0)
            ).actualizarEstadisticasCompeticion(
                competicionId = partido.competicionId,
                goles = nuevosGoles,
                asistencias = nuevasAsistencias,
                porteriaImbatida = porteriaImbatida,
                esMVP = esMVP
            )
        }
    }

    private fun actualizarEstadisticasCompeticion(
        competicionId: String,
        goles: Int,
        asistencias: Int,
        porteriaImbatida: Boolean,
        esMVP: Boolean
    ): Jugador {
        val nuevosPartidos = partidosPorCompeticion.incrementar(competicionId)
        val nuevosGoles = golesPorCompeticion.incrementar(competicionId, goles)
        val nuevasAsistencias = asistenciasPorCompeticion.incrementar(competicionId, asistencias)
        val nuevasPorterias = porteriasImbatidasPorCompeticion.incrementar(
            competicionId,
            if (porteriaImbatida) 1 else 0
        )
        val nuevosMVP = mvpPorCompeticion.incrementar(
            competicionId,
            if (esMVP) 1 else 0
        )
        return this.copy(
            partidosPorCompeticion = nuevosPartidos,
            golesPorCompeticion = nuevosGoles,
            asistenciasPorCompeticion = nuevasAsistencias,
            porteriasImbatidasPorCompeticion = nuevasPorterias,
            mvpPorCompeticion = nuevosMVP
        )
    }

    @Exclude
    fun getEstadisticasCompeticion(competicionId: String): EstadisticasCompeticion {
        return EstadisticasCompeticion(
            partidos = partidosPorCompeticion[competicionId] ?: 0,
            goles = golesPorCompeticion[competicionId] ?: 0,
            asistencias = asistenciasPorCompeticion[competicionId] ?: 0,
            porteriasImbatidas = porteriasImbatidasPorCompeticion[competicionId] ?: 0
        )
    }

    @Exclude
    fun revertirEstadisticasPartido(partido: Partido): Jugador {
        val jugoEnPartido = partido.alineacionIds.contains(id)
        val golesAnteriores = partido.goleadoresIds.count { it == id }
        val asistenciasAnteriores = partido.asistentesIds.count { it == id }
        val porteriaImbatidaAnterior = id == partido.porteroImbatidoId
        val mvpAnterior = id == partido.jugadorDelPartido

        return if (!jugoEnPartido) {
            this
        } else {
            this.copy(
                partidosJugados = max(0, partidosJugados - 1),
                goles = max(0, goles - golesAnteriores),
                asistencias = max(0, asistencias - asistenciasAnteriores),
                porteriasImbatidas = max(0, porteriasImbatidas - (if (porteriaImbatidaAnterior) 1 else 0)),
                mvp = max(0, mvp - (if (mvpAnterior) 1 else 0))
            ).revertirEstadisticasCompeticion(
                competicionId = partido.competicionId,
                goles = golesAnteriores,
                asistencias = asistenciasAnteriores,
                porteriaImbatida = porteriaImbatidaAnterior,
                esMVP = mvpAnterior
            )
        }
    }

    private fun revertirEstadisticasCompeticion(
        competicionId: String,
        goles: Int,
        asistencias: Int,
        porteriaImbatida: Boolean,
        esMVP: Boolean
    ): Jugador {
        val nuevosPartidos = partidosPorCompeticion.decrementar(competicionId)
        val nuevosGoles = golesPorCompeticion.decrementar(competicionId, goles)
        val nuevasAsistencias = asistenciasPorCompeticion.decrementar(competicionId, asistencias)
        val nuevasPorterias = porteriasImbatidasPorCompeticion.decrementar(
            competicionId,
            if (porteriaImbatida) 1 else 0
        )
        val nuevosMVP = mvpPorCompeticion.decrementar(
            competicionId,
            if (esMVP) 1 else 0
        )
        return this.copy(
            partidosPorCompeticion = nuevosPartidos,
            golesPorCompeticion = nuevosGoles,
            asistenciasPorCompeticion = nuevasAsistencias,
            porteriasImbatidasPorCompeticion = nuevasPorterias,
            mvpPorCompeticion = nuevosMVP
        )
    }
}

// Extensión de ayuda
private fun Map<String, Int>.incrementar(key: String, valor: Int = 1): Map<String, Int> {
    return this.toMutableMap().apply {
        this[key] = (this[key] ?: 0) + valor
    }
}

private fun Map<String, Int>.decrementar(key: String, valor: Int = 1): Map<String, Int> {
    return this.toMutableMap().apply {
        val currentValue = this[key] ?: 0
        this[key] = max(0, currentValue - valor)
        // Si queda en 0, eliminar la entrada para mantener limpio el mapa
        if (this[key] == 0) {
            remove(key)
        }
    }
}

data class EstadisticasCompeticion(
    val partidos: Int,
    val goles: Int,
    val asistencias: Int,
    val porteriasImbatidas: Int
) {
    @Exclude
    val promedioGoles: Float = if (partidos > 0) goles.toFloat() / partidos else 0f

    @Exclude
    val promedioAsistencias: Float = if (partidos > 0) asistencias.toFloat() / partidos else 0f
}

// Extensión para convertir Jugador a mapa de Firestore
@Exclude
fun Jugador.toFirestoreMap(): Map<String, Any> {
    return mutableMapOf<String, Any>(
        "nombre" to nombre,
        "posicion" to posicion.name,
        "equipoId" to equipoId,
        "activo" to activo,
        "partidosJugados" to partidosJugados,
        "goles" to goles,
        "asistencias" to asistencias,
        "porteriasImbatidas" to porteriasImbatidas,
        "mvp" to mvp,
        "mvpPorCompeticion" to mvpPorCompeticion
    ).apply {
        // Añadir mapas de competiciones solo si no están vacíos
        if (partidosPorCompeticion.isNotEmpty()) put("partidosPorCompeticion", partidosPorCompeticion)
        if (golesPorCompeticion.isNotEmpty()) put("golesPorCompeticion", golesPorCompeticion)
        if (asistenciasPorCompeticion.isNotEmpty()) put("asistenciasPorCompeticion", asistenciasPorCompeticion)
        if (porteriasImbatidasPorCompeticion.isNotEmpty()) put("porteriasImbatidasPorCompeticion", porteriasImbatidasPorCompeticion)
    }
}