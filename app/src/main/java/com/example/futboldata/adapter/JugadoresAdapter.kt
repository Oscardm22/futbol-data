package com.example.futboldata.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.futboldata.R
import com.example.futboldata.adapter.diffcallbacks.JugadorDiffCallback
import com.example.futboldata.data.model.Jugador
import com.example.futboldata.data.model.Posicion
import com.example.futboldata.databinding.ItemPlayerBinding

class JugadoresAdapter(
    private val onDeleteClick: (Jugador) -> Unit,
    private val onPlayerClick: (Jugador) -> Unit
) : ListAdapter<Jugador, JugadoresAdapter.PlayerViewHolder>(JugadorDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PlayerViewHolder {
        val binding = ItemPlayerBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return PlayerViewHolder(binding, onDeleteClick, onPlayerClick)
    }

    override fun onBindViewHolder(holder: PlayerViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class PlayerViewHolder(
        private val binding: ItemPlayerBinding,
        private val onDeleteClick: (Jugador) -> Unit,
        private val onPlayerClick: (Jugador) -> Unit
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(player: Jugador) {
            // Información básica
            binding.tvNombre.text = player.nombre
            binding.tvPosicion.text = player.posicion.toString()

            // Estadísticas
            binding.tvGoles.text = player.goles.toString()
            binding.tvAsistencias.text = player.asistencias.toString()
            binding.tvMVP.text = player.mvp.toString()
            binding.tvPartidosJugados.text = player.partidosJugados.toString()

            // Reducir tamaño de iconos de estadísticas
            reducirIconoEstadistica(binding.tvGoles, R.drawable.ic_gol)
            reducirIconoEstadistica(binding.tvAsistencias, R.drawable.ic_asistencia)
            reducirIconoEstadistica(binding.tvMVP, R.drawable.ic_mvp_selected)
            reducirIconoEstadistica(binding.tvPartidosJugados, R.drawable.ic_games)

            // Icono de posición
            val iconRes = when(player.posicion) {
                Posicion.PO -> R.drawable.ic_portero
                Posicion.DFC, Posicion.LD, Posicion.LI -> R.drawable.ic_defensa
                Posicion.MCD, Posicion.MC, Posicion.MCO, Posicion.MI, Posicion.MD -> R.drawable.ic_mediocampista
                Posicion.ED, Posicion.EI, Posicion.DC -> R.drawable.ic_delantero
            }
            binding.ivPositionIcon.setImageResource(iconRes)

            binding.ivDelete.setOnClickListener {
                onDeleteClick(player)
            }

            binding.root.setOnClickListener {
                onPlayerClick(player)
            }
        }

        private fun reducirIconoEstadistica(textView: TextView, drawableRes: Int) {
            val drawable = ContextCompat.getDrawable(textView.context, drawableRes)
            val density = textView.context.resources.displayMetrics.density

            val baseSizePx = (18 * density).toInt()

            when (drawableRes) {
                R.drawable.ic_games -> {
                    val width = (baseSizePx * 0.75f).toInt()
                    val height = baseSizePx
                    val leftPadding = (baseSizePx - width) / 2
                    drawable?.setBounds(leftPadding, 0, leftPadding + width, height)
                }
                R.drawable.ic_asistencia -> {
                    val width = baseSizePx
                    val height = (baseSizePx * 0.5f).toInt()
                    val topPadding = (baseSizePx - height) / 2
                    drawable?.setBounds(0, topPadding, width, topPadding + height)
                }
                R.drawable.ic_gol -> {
                    drawable?.setBounds(0, 0, baseSizePx, baseSizePx)
                }
                R.drawable.ic_mvp_selected -> {
                    drawable?.setBounds(0, 0, baseSizePx, baseSizePx)
                }
            }

            textView.setCompoundDrawables(drawable, null, null, null)
            textView.compoundDrawablePadding = (2 * density).toInt()
        }
    }
}