package com.example.futboldata.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.futboldata.R
import com.example.futboldata.adapter.diffcallbacks.JugadorDiffCallback
import com.example.futboldata.data.model.Jugador
import com.example.futboldata.data.model.Posicion
import com.example.futboldata.databinding.ItemPlayerBinding

class JugadoresAdapter(
    private val onDeleteClick: (Jugador) -> Unit
) : ListAdapter<Jugador, JugadoresAdapter.PlayerViewHolder>(JugadorDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PlayerViewHolder {
        val binding = ItemPlayerBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return PlayerViewHolder(binding, onDeleteClick)
    }

    override fun onBindViewHolder(holder: PlayerViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class PlayerViewHolder(
        private val binding: ItemPlayerBinding,
        private val onDeleteClick: (Jugador) -> Unit
    ) : RecyclerView.ViewHolder(binding.root) {
        fun bind(player: Jugador) {
            binding.tvNombre.text = player.nombre
            binding.tvPosicion.text = player.posicion.displayName

            val iconRes = when(player.posicion) {
                Posicion.PO -> R.drawable.ic_portero
                Posicion.DFC, Posicion.LD, Posicion.LI -> R.drawable.ic_defensa
                Posicion.MCD, Posicion.MC, Posicion.MCO, Posicion.MI, Posicion.MD -> R.drawable.ic_mediocampista
                Posicion.ED, Posicion.EI, Posicion.DC -> R.drawable.ic_delantero
            }

            binding.ivPositionIcon.setImageResource(iconRes)

            binding.ivDelete.setOnClickListener {
                onDeleteClick(player)
            }
        }
    }
}