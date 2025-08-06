package com.example.futboldata.data.model

import android.os.Parcelable
import com.google.firebase.firestore.Exclude
import kotlinx.parcelize.Parcelize

@Parcelize
data class Jugador(
    val id: String = "",
    val nombre: String,
    val posicion: Posicion,
    val equipoId: String,
    @get:Exclude
    val golesPorCompeticion: Map<String, Int> = emptyMap()
) : Parcelable {

    @get:Exclude
    val golesTotales: Int
        get() = golesPorCompeticion.values.sum()

    // Constructor para Firestore
    constructor() : this("", "", Posicion.PO, "")

    companion object {
        fun fromFirestore(
            id: String,
            nombre: String,
            posicion: String,
            equipoId: String,
            goles: Map<String, Int>?
        ): Jugador {
            return Jugador(
                id = id,
                nombre = nombre,
                posicion = Posicion.valueOf(posicion),
                equipoId = equipoId,
                golesPorCompeticion = goles ?: emptyMap()
            )
        }
    }

    fun toFirestoreMap(): Map<String, Any> {
        return mapOf(
            "nombre" to nombre,
            "posicion" to posicion.name,
            "equipoId" to equipoId,
            "golesPorCompeticion" to golesPorCompeticion
        )
    }

    fun agregarGoles(competicionId: String, cantidad: Int): Jugador {
        val nuevosGoles = golesPorCompeticion.toMutableMap()
        nuevosGoles[competicionId] = (nuevosGoles[competicionId] ?: 0) + cantidad
        return this.copy(golesPorCompeticion = nuevosGoles)
    }
}