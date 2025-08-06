package com.example.futboldata.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import com.example.futboldata.adapter.diffcallbacks.JugadorDiffCallback
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.futboldata.data.model.Jugador
import com.example.futboldata.databinding.ItemJugadorGoleadorBinding

class GoleadoresAdapter(
    private val onGolClicked: (String, String) -> Unit
) : ListAdapter<Jugador, GoleadoresAdapter.ViewHolder>(JugadorDiffCallback()) {

    inner class ViewHolder(private val binding: ItemJugadorGoleadorBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(jugador: Jugador) {
            binding.tvNombre.text = jugador.nombre
            binding.btnAddGol.setOnClickListener {
                onGolClicked(jugador.id, jugador.nombre)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemJugadorGoleadorBinding.inflate(
            LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }
}