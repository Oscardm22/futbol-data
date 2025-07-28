package com.example.futboldata.data.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import java.util.Date

@Parcelize
data class Equipo(
    val id: String = "",
    val nombre: String = "",
    val fechaCreacion: Date = Date(),
    val imagenBase64: String = "",
    val estadisticas: Estadisticas = Estadisticas.empty()
) : Parcelable {
    fun getIniciales(): String {
        return nombre.split(" ")
            .take(2).joinToString("") { it.firstOrNull()?.toString() ?: "" }
            .uppercase()
    }

    constructor() : this("", "", Date(), "", Estadisticas.empty())
}