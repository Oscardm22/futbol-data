package com.example.futboldata.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.futboldata.data.model.Jugador
import com.example.futboldata.databinding.ItemJugadorPartidoBinding

class JugadoresPartidoAdapter(
    private val onTitularChange: (String, Boolean) -> Unit,
    private val onGolesChange: (String, Int) -> Unit
) : ListAdapter<Jugador, JugadoresPartidoAdapter.ViewHolder>(DiffCallback()) {

    inner class ViewHolder(val binding: ItemJugadorPartidoBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(jugador: Jugador) {
            binding.apply {
                tvNombre.text = jugador.nombre
                tvPosicion.text = jugador.posicion.displayName

                // Listener para cambios en titularidad
                swTitular.setOnCheckedChangeListener { _, isChecked ->
                    onTitularChange(jugador.id, isChecked)
                }

                // Listener para cambios en goles
                etGoles.setOnEditorActionListener { _, _, _ ->
                    val goles = etGoles.text.toString().toIntOrNull() ?: 0
                    onGolesChange(jugador.id, goles)
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