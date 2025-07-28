package com.example.futboldata.data.model

data class Jugador(
    val id: String = "",
    val nombre: String,
    val posicion: Posicion,
    val equipoId: String,
    val goles: Map<TipoCompeticion, Int> = mapOf(
        TipoCompeticion.LIGA to 0,
        TipoCompeticion.COPA_NACIONAL to 0,
        TipoCompeticion.COPA_INTERNACIONAL to 0,
        TipoCompeticion.SUPERCOPA to 0
    )
) {
    // Constructor sin parámetros requerido por Firestore
    constructor() : this("", "", Posicion.PO, "")

    val golesTotales: Int
        get() = goles.values.sum()

    fun toMap(): Map<String, Any> {
        return mapOf(
            "id" to id,  // Añadido el ID al mapa
            "nombre" to nombre,
            "posicion" to posicion.name,
            "equipoId" to equipoId,
            "goles" to goles.mapKeys { it.key.name },
            "golesTotales" to golesTotales
        )
    }

    companion object {
        fun fromMap(map: Map<String, Any>): Jugador {
            val golesMap = when (val goles = map["goles"]) {
                is Map<*, *> -> goles.mapNotNull { (key, value) ->
                    when {
                        key is String && value is Int -> key to value
                        else -> null
                    }
                }.toMap()
                else -> emptyMap()
            }

            return Jugador(
                id = map["id"] as? String ?: "",  // Obtenemos el ID del mapa
                nombre = map["nombre"] as? String ?: "",
                posicion = (map["posicion"] as? String)?.let { Posicion.valueOf(it) } ?: Posicion.PO,
                equipoId = map["equipoId"] as? String ?: "",
                goles = golesMap.mapKeys { TipoCompeticion.valueOf(it.key) }
            )
        }
    }
}