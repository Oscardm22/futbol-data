package com.example.futboldata.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.futboldata.data.model.Jugador
import com.example.futboldata.databinding.ItemJugadorMvpBinding

data class JugadorWithSelection(
    val jugador: Jugador,
    val isSelected: Boolean
)

object JugadorWithSelectionDiffCallback : androidx.recyclerview.widget.DiffUtil.ItemCallback<JugadorWithSelection>() {
    override fun areItemsTheSame(oldItem: JugadorWithSelection, newItem: JugadorWithSelection): Boolean {
        return oldItem.jugador.id == newItem.jugador.id
    }

    override fun areContentsTheSame(oldItem: JugadorWithSelection, newItem: JugadorWithSelection): Boolean {
        return oldItem == newItem
    }
}

class MVPAdapter(
    private val onJugadorSelected: (String?) -> Unit
) : ListAdapter<JugadorWithSelection, MVPAdapter.JugadorViewHolder>(JugadorWithSelectionDiffCallback) {

    inner class JugadorViewHolder(private val binding: ItemJugadorMvpBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(jugadorWithSelection: JugadorWithSelection) {
            val jugador = jugadorWithSelection.jugador
            val isSelected = jugadorWithSelection.isSelected

            binding.apply {
                tvNombre.text = jugador.nombre
                tvPosicion.text = jugador.posicion.name

                // Mostrar/ocultar el badge de MVP según la selección
                ivSelected.visibility = if (isSelected) View.VISIBLE else View.INVISIBLE

                root.setOnClickListener {
                    // Notificar la selección
                    if (isSelected) {
                        onJugadorSelected(null) // Deseleccionar
                    } else {
                        onJugadorSelected(jugador.id) // Seleccionar
                    }
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
        holder.bind(getItem(position))
    }

    fun submitJugadoresList(jugadores: List<Jugador>, selectedJugadorId: String?) {
        val jugadoresWithSelection = jugadores.map { jugador ->
            JugadorWithSelection(
                jugador = jugador,
                isSelected = jugador.id == selectedJugadorId
            )
        }
        submitList(jugadoresWithSelection)
    }

    // Actualizar solo la selección
    fun updateSelection(selectedJugadorId: String?) {
        val currentList = currentList.map { jugadorWithSelection ->
            jugadorWithSelection.copy(
                isSelected = jugadorWithSelection.jugador.id == selectedJugadorId
            )
        }
        submitList(currentList)
    }
}