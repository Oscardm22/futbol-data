package com.example.futboldata.data.model

import java.util.Date

data class Equipo(
    val id: String = "",
    val nombre: String = "",
    val fechaCreacion: Date = Date(),
    val imagenUrl: String = ""
) {
    fun getIniciales(): String {
        return nombre.split(" ")
            .take(2).joinToString("") { it.firstOrNull()?.toString() ?: "" }
            .uppercase()
    }
}