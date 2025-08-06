package com.example.futboldata.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.example.futboldata.data.model.Jugador
import com.example.futboldata.databinding.ItemJugadorMvpBinding

class MVPAdapter(
    private val onJugadorSelected: (String) -> Unit
) : RecyclerView.Adapter<MVPAdapter.JugadorViewHolder>() {

    private var jugadores: List<Jugador> = emptyList()
    private var selectedJugadorId: String? = null

    inner class JugadorViewHolder(private val binding: ItemJugadorMvpBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(jugador: Jugador) {
            binding.apply {
                tvNombre.text = jugador.nombre
                tvPosicion.text = jugador.posicion.name
                // Resaltar selección
                root.isSelected = jugador.id == selectedJugadorId

                root.setOnClickListener {
                    onJugadorSelected(jugador.id)
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): JugadorViewHolder {
        val binding = ItemJugadorMvpBinding.inflate(
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

    fun setSelectedJugador(jugadorId: String?) {
        val previousSelected = selectedJugadorId
        selectedJugadorId = jugadorId
        // Notificar cambios para actualizar la UI
        notifyItemChanged(jugadores.indexOfFirst { it.id == previousSelected })
        notifyItemChanged(jugadores.indexOfFirst { it.id == selectedJugadorId })
    }

    private class JugadorDiffCallback(
        private val oldList: List<Jugador>,
        private val newList: List<Jugador>
    ) : DiffUtil.Callback() {
        override fun getOldListSize(): Int = oldList.size
        override fun getNewListSize(): Int = newList.size
        override fun areItemsTheSame(oldPos: Int, newPos: Int): Boolean {
            return oldList[oldPos].id == newList[newPos].id
        }
        override fun areContentsTheSame(oldPos: Int, newPos: Int): Boolean {
            return oldList[oldPos] == newList[newPos]
        }
    }
}