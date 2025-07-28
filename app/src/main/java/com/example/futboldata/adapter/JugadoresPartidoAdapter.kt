package com.example.futboldata.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.futboldata.data.model.Jugador
import com.example.futboldata.data.model.ParticipacionJugador
import com.example.futboldata.databinding.ItemJugadorPartidoBinding

class JugadoresPartidoAdapter(
    private val onParticipacionChange: (Jugador, ParticipacionJugador) -> Unit
) : ListAdapter<Jugador, JugadoresPartidoAdapter.ViewHolder>(DiffCallback()) {

    inner class ViewHolder(val binding: ItemJugadorPartidoBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(jugador: Jugador) {
            binding.apply {
                tvNombre.text = jugador.nombre
                tvPosicion.text = jugador.posicion.displayName

                swTitular.setOnCheckedChangeListener { _, isChecked ->
                    onParticipacionChange(jugador, ParticipacionJugador(
                        jugadorId = jugador.id,
                        titular = isChecked,
                        minutosJugados = if (isChecked) 90 else 0
                    ))
                }

                etGoles.setOnEditorActionListener { _, _, _ ->
                    onParticipacionChange(jugador, ParticipacionJugador(
                        jugadorId = jugador.id,
                        goles = etGoles.text.toString().toIntOrNull() ?: 0
                    ))
                    false
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemJugadorPartidoBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class DiffCallback : DiffUtil.ItemCallback<Jugador>() {
        override fun areItemsTheSame(oldItem: Jugador, newItem: Jugador): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Jugador, newItem: Jugador): Boolean {
            return oldItem == newItem
        }
    }
}