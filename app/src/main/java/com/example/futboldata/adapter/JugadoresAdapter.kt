package com.example.futboldata.adapter

import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.futboldata.adapter.diffcallbacks.JugadorDiffCallback
import com.example.futboldata.data.model.Jugador
import com.example.futboldata.databinding.ItemPlayerBinding
import androidx.core.graphics.toColorInt

class JugadoresAdapter : ListAdapter<Jugador, JugadoresAdapter.PlayerViewHolder>(JugadorDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PlayerViewHolder {
        Log.d("DEBUG_ADAPTER", "Creando ViewHolder")
        val binding = ItemPlayerBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return PlayerViewHolder(binding)
    }

    override fun onBindViewHolder(holder: PlayerViewHolder, position: Int) {
        Log.d("DEBUG_ADAPTER", "Vinculando posición $position")
        holder.bind(getItem(position))
    }

    override fun getItemCount(): Int {
        val count = super.getItemCount()
        Log.d("DEBUG_ADAPTER", "Número de items: $count")
        return count
    }

    inner class PlayerViewHolder(private val binding: ItemPlayerBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(player: Jugador) {
            Log.d("DEBUG_ADAPTER", "Mostrando jugador: ${player.nombre}")
            binding.tvNombre.text = player.nombre
            binding.tvPosicion.text = player.posicion.toString()

            // Verificación visual temporal
            binding.root.setBackgroundColor("#220000FF".toColorInt())
        }
    }
}