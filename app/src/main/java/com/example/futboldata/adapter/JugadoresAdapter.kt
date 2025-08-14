package com.example.futboldata.adapter

import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.futboldata.R
import com.example.futboldata.adapter.diffcallbacks.JugadorDiffCallback
import com.example.futboldata.data.model.Jugador
import com.example.futboldata.data.model.Posicion
import com.example.futboldata.databinding.ItemPlayerBinding

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

    inner class PlayerViewHolder(private val binding: ItemPlayerBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(player: Jugador) {
            Log.d("DEBUG_ADAPTER", "Mostrando jugador: ${player.nombre}")
            binding.tvNombre.text = player.nombre
            binding.tvPosicion.text = player.posicion.displayName

            // Asignar icono según posición
            val iconRes = when(player.posicion) {
                Posicion.PO -> R.drawable.ic_portero
                Posicion.DFC, Posicion.LD, Posicion.LI -> R.drawable.ic_defensa
                Posicion.MCD, Posicion.MC, Posicion.MCO, Posicion.MI, Posicion.MD -> R.drawable.ic_mediocampista
                Posicion.ED, Posicion.EI, Posicion.DC -> R.drawable.ic_delantero
            }

            binding.ivPositionIcon.setImageResource(iconRes)
        }
    }
}