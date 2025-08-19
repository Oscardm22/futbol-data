package com.example.futboldata.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import com.example.futboldata.adapter.diffcallbacks.JugadorDiffCallback
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.futboldata.data.model.Jugador
import com.example.futboldata.data.model.ParticipacionJugador
import com.example.futboldata.databinding.ItemJugadorAlineacionBinding

class JugadoresAlineacionAdapter(
    private val onSelectionChanged: (Jugador, Boolean) -> Unit
) : ListAdapter<Jugador, JugadoresAlineacionAdapter.ViewHolder>(JugadorDiffCallback()) {

    // Mapa para mantener el estado de selección de cada jugador
    private val seleccionadosMap = mutableMapOf<String, Boolean>()

    fun setSelecciones(selecciones: List<ParticipacionJugador>) {
        seleccionadosMap.clear()
        selecciones.forEach {
            seleccionadosMap[it.jugadorId] = it.esTitular
        }
    }

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

        private var currentJugador: Jugador? = null

        fun bind(jugador: Jugador) {
            currentJugador = jugador

            binding.tvNombre.text = jugador.nombre
            binding.tvPosicion.text = jugador.posicion.toString()

            // Eliminar listener anterior para evitar múltiples llamadas
            binding.switchTitular.setOnCheckedChangeListener(null)

            // Establecer el estado actual del Switch
            binding.switchTitular.isChecked = seleccionadosMap[jugador.id] == true

            // Configurar nuevo listener
            binding.switchTitular.setOnCheckedChangeListener { _, isChecked ->
                currentJugador?.let { jugador ->
                    seleccionadosMap[jugador.id] = isChecked
                    onSelectionChanged(jugador, isChecked)
                }
            }
        }
    }
}