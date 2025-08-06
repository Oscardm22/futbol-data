package com.example.futboldata.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import com.example.futboldata.adapter.diffcallbacks.JugadorDiffCallback
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.futboldata.data.model.Jugador
import com.example.futboldata.databinding.ItemJugadorAlineacionBinding

class JugadoresAlineacionAdapter(
    private val onSelectionChanged: (Jugador, Boolean) -> Unit
) : ListAdapter<Jugador, JugadoresAlineacionAdapter.ViewHolder>(JugadorDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemJugadorAlineacionBinding.inflate(
            LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class ViewHolder(private val binding: ItemJugadorAlineacionBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(jugador: Jugador) {
            binding.tvNombre.text = jugador.nombre
            binding.tvPosicion.text = jugador.posicion.toString()

            binding.switchTitular.setOnCheckedChangeListener { _, isChecked ->
                onSelectionChanged(jugador, isChecked)
            }
        }
    }
}