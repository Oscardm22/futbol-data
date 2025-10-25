package com.example.futboldata.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.futboldata.adapter.diffcallbacks.JugadorDiffCallback
import com.example.futboldata.data.model.Jugador
import com.example.futboldata.databinding.ItemJugadorAlineacionBinding

class JugadoresAlineacionAdapter(
    private val onSelectionChanged: (Jugador, Boolean) -> Unit
) : ListAdapter<Jugador, JugadoresAlineacionAdapter.ViewHolder>(JugadorDiffCallback()) {

    // Mapa para mantener el estado de selección de cada jugador (jugadorId -> esTitular)
    private val seleccionadosMap = mutableMapOf<String, Boolean>()

    // Establecer selecciones iniciales usando la alineación del partido
    fun setSeleccionesIniciales(alineacionIds: List<String>) {
        seleccionadosMap.clear()
        currentList.forEach { jugador ->
            seleccionadosMap[jugador.id] = alineacionIds.contains(jugador.id)
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

            with(binding) {
                tvNombre.text = jugador.nombre
                tvPosicion.text = jugador.posicion.toString()

                // Eliminar listener anterior para evitar múltiples llamadas
                switchTitular.setOnCheckedChangeListener(null)

                // Establecer el estado actual del Switch
                switchTitular.isChecked = seleccionadosMap[jugador.id] == true

                // Configurar nuevo listener
                switchTitular.setOnCheckedChangeListener { _, isChecked ->
                    jugador.id.let { jugadorId ->
                        seleccionadosMap[jugadorId] = isChecked
                        onSelectionChanged(jugador, isChecked)
                    }
                }
            }
        }
    }
}