package com.example.futboldata.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.futboldata.R
import com.example.futboldata.adapter.diffcallbacks.JugadorDiffCallback
import com.example.futboldata.data.model.Jugador
import com.example.futboldata.databinding.ItemJugadorGoleadorBinding

class GoleadoresAdapter(
    private val onGolAdded: (String, String) -> Unit,
    private val onGolRemoved: (String, String) -> Unit
) : ListAdapter<Jugador, GoleadoresAdapter.ViewHolder>(JugadorDiffCallback()) {

    private var golesMap: Map<String, Int> = emptyMap()

    fun updateGoles(nuevosGoles: Map<String, Int>) {
        this.golesMap = nuevosGoles
        notifyItemRangeChanged(0, itemCount)
    }

    inner class ViewHolder(private val binding: ItemJugadorGoleadorBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(jugador: Jugador) {
            binding.tvNombre.text = jugador.nombre

            val golesJugador = golesMap[jugador.id] ?: 0
            binding.tvGoles.text = golesJugador.toString()

            // Botón para añadir gol
            binding.btnAddGol.setOnClickListener {
                onGolAdded(jugador.id, jugador.nombre)
            }

            // Botón para quitar gol
            binding.btnRemoveGol.setOnClickListener {
                if (golesJugador > 0) {
                    onGolRemoved(jugador.id, jugador.nombre)
                }
            }

            // Deshabilitar botón de quitar si no hay goles
            binding.btnRemoveGol.isEnabled = golesJugador > 0

            // Cambiar color del botón de quitar según si está habilitado
            if (golesJugador > 0) {
                binding.btnRemoveGol.setIconTintResource(R.color.botones_negativos)
                binding.btnRemoveGol.strokeColor = binding.root.resources.getColorStateList(R.color.botones_negativos, null)
            } else {
                binding.btnRemoveGol.setIconTintResource(R.color.gray)
                binding.btnRemoveGol.strokeColor = binding.root.resources.getColorStateList(R.color.gray, null)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemJugadorGoleadorBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }
}