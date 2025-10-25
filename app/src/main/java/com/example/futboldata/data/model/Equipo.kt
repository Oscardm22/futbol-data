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
) : Parcelable {
    constructor() : this("", "", Date(), "")
}