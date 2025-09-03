package com.example.futboldata.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.futboldata.R
import com.example.futboldata.data.model.Posicion
import com.example.futboldata.databinding.ItemJugadorSimpleBinding

class JugadorSimpleAdapter(
    jugadores: List<JugadorAlineacion>,
    private val nombresJugadores: Map<String, String>,
    private val posicionesJugadores: Map<String, Posicion>
) : RecyclerView.Adapter<JugadorSimpleAdapter.ViewHolder>() {

    // Lista ordenada de jugadores usando JugadorUtils
    private val jugadoresOrdenados = jugadores.sortedBy { jugador ->
        val posicion = posicionesJugadores[jugador.id]
        Posicion.entries.indexOf(posicion)
    }

    class ViewHolder(val binding: ItemJugadorSimpleBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemJugadorSimpleBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val jugador = jugadoresOrdenados[position]
        val nombre = nombresJugadores[jugador.id] ?: "Jugador desconocido"

        holder.binding.tvNombreJugador.text = nombre

        // Limpiar iconos anteriores
        holder.binding.containerIconos.removeAllViews()

        // Agregar iconos de goles
        repeat(jugador.cantidadGoles) {
            val imageView = createIconImageView(holder, R.drawable.ic_gol)
            holder.binding.containerIconos.addView(imageView)
        }

        // Agregar iconos de asistencias
        repeat(jugador.cantidadAsistencias) {
            val imageView = createIconImageView(holder, R.drawable.ic_asistencia)
            holder.binding.containerIconos.addView(imageView)
        }

        // Agregar icono de MVP - solo uno
        if (jugador.esMvp) {
            val imageView = createIconImageView(holder, R.drawable.ic_mvp_selected)
            holder.binding.containerIconos.addView(imageView)
        }
    }

    private fun createIconImageView(holder: ViewHolder, drawableResId: Int): View {
        val imageView = androidx.appcompat.widget.AppCompatImageView(holder.itemView.context)
        val size = (16 * holder.itemView.context.resources.displayMetrics.density).toInt()
        val margin = (4 * holder.itemView.context.resources.displayMetrics.density).toInt()

        val layoutParams = ViewGroup.MarginLayoutParams(size, size)
        layoutParams.marginEnd = margin

        imageView.layoutParams = layoutParams
        imageView.setImageResource(drawableResId)

        return imageView
    }

    override fun getItemCount(): Int = jugadoresOrdenados.size

    data class JugadorAlineacion(
        val id: String,
        val cantidadGoles: Int = 0,
        val cantidadAsistencias: Int = 0,
        val esMvp: Boolean = false,
        val esPorteroImbatido: Boolean = false
    )
}