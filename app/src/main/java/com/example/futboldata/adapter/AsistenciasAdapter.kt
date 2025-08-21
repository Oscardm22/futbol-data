package com.example.futboldata.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.example.futboldata.R
import com.example.futboldata.data.model.Jugador
import com.example.futboldata.databinding.ItemAsistenciaBinding

class AsistenciasAdapter(
    private val onAsistenciaAdded: (String, String) -> Unit,
    private val onAsistenciaRemoved: (String, String) -> Unit
) : RecyclerView.Adapter<AsistenciasAdapter.JugadorViewHolder>() {

    private var jugadores: List<Jugador> = emptyList()
    private var asistenciasMap: Map<String, Int> = emptyMap()

    inner class JugadorViewHolder(private val binding: ItemAsistenciaBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(jugador: Jugador) {
            binding.apply {
                tvNombre.text = jugador.nombre

                val numAsistencias = asistenciasMap[jugador.id] ?: 0
                tvAsistencias.text = numAsistencias.toString()

                // Botón para añadir asistencia
                btnAddAsistencia.setOnClickListener {
                    onAsistenciaAdded(jugador.id, jugador.nombre)
                }

                // Botón para quitar asistencia
                btnRemoveAsistencia.setOnClickListener {
                    if (numAsistencias > 0) {
                        onAsistenciaRemoved(jugador.id, jugador.nombre)
                    }
                }

                // Deshabilitar botón de quitar si no hay asistencias
                btnRemoveAsistencia.isEnabled = numAsistencias > 0

                // Cambiar color del botón de quitar según si está habilitado
                val context = binding.root.context
                if (numAsistencias > 0) {
                    btnRemoveAsistencia.setIconTintResource(R.color.botones_negativos)
                    btnRemoveAsistencia.strokeColor = ContextCompat.getColorStateList(context, R.color.botones_negativos)
                } else {
                    btnRemoveAsistencia.setIconTintResource(R.color.gray)
                    btnRemoveAsistencia.strokeColor = ContextCompat.getColorStateList(context, R.color.gray)
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): JugadorViewHolder {
        val binding = ItemAsistenciaBinding.inflate(
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

    fun updateAsistencias(nuevasAsistencias: Map<String, Int>) {
        this.asistenciasMap = nuevasAsistencias
        notifyItemRangeChanged(0, itemCount)
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