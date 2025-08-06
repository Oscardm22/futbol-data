package com.example.futboldata.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.example.futboldata.data.model.Jugador
import com.example.futboldata.databinding.ItemPlayerBinding

class AsistenciasAdapter(
    private val onAsistenciaClick: (jugadorId: String, jugadorNombre: String) -> Unit
) : RecyclerView.Adapter<AsistenciasAdapter.JugadorViewHolder>() {

    private var jugadores: List<Jugador> = emptyList()

    inner class JugadorViewHolder(private val binding: ItemPlayerBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(jugador: Jugador) {
            binding.apply {
                tvNombre.text = jugador.nombre
                tvPosicion.text = jugador.posicion.name
                root.setOnClickListener {
                    onAsistenciaClick(jugador.id, jugador.nombre)
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): JugadorViewHolder {
        val binding = ItemPlayerBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return JugadorViewHolder(binding)
    }

    override fun onBindViewHolder(holder: JugadorViewHolder, position: Int) {
        holder.bind(jugadores[position])
    }

    override fun getItemCount(): Int = jugadores.size

    fun submitList(newList: List<Jugador>) {
        val diffCallback = JugadorDiffCallback(jugadores, newList)
        val diffResult = DiffUtil.calculateDiff(diffCallback)

        jugadores = newList
        diffResult.dispatchUpdatesTo(this)
    }

    private class JugadorDiffCallback(
        private val oldList: List<Jugador>,
        private val newList: List<Jugador>
    ) : DiffUtil.Callback() {

        override fun getOldListSize(): Int = oldList.size

        override fun getNewListSize(): Int = newList.size

        override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
            return oldList[oldItemPosition].id == newList[newItemPosition].id
        }

        override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
            return oldList[oldItemPosition] == newList[newItemPosition]
        }
    }
}