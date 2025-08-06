package com.example.futboldata.adapter.diffcallbacks

import androidx.recyclerview.widget.DiffUtil
import com.example.futboldata.data.model.Jugador

class JugadorDiffCallback : DiffUtil.ItemCallback<Jugador>() {
    override fun areItemsTheSame(oldItem: Jugador, newItem: Jugador): Boolean {
        return oldItem.id == newItem.id
    }

    override fun areContentsTheSame(oldItem: Jugador, newItem: Jugador): Boolean {
        return oldItem == newItem
    }
}