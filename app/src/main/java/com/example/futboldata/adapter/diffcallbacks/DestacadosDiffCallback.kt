package com.example.futboldata.adapter.diffcallbacks

import androidx.recyclerview.widget.DiffUtil
import com.example.futboldata.ui.equipos.fragments.EstadisticaDestacada

class DestacadosDiffCallback : DiffUtil.ItemCallback<EstadisticaDestacada>() {
    override fun areItemsTheSame(oldItem: EstadisticaDestacada, newItem: EstadisticaDestacada): Boolean {
        return oldItem.tipo == newItem.tipo && oldItem.jugador == newItem.jugador
    }

    override fun areContentsTheSame(oldItem: EstadisticaDestacada, newItem: EstadisticaDestacada): Boolean {
        return oldItem == newItem
    }
}