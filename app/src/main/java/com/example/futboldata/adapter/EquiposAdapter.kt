package com.example.futboldata.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.futboldata.R
import com.example.futboldata.data.model.EquipoWithStats
import com.example.futboldata.databinding.ItemEquipoBinding
import com.example.futboldata.adapter.diffcallbacks.EquipoWithStatsDiffCallback
import com.example.futboldata.utils.ImageLoader
import kotlinx.coroutines.Job

class EquiposAdapter(
    private val onItemClick: (String) -> Unit,
    private val onDeleteClick: (String) -> Unit
) : ListAdapter<EquipoWithStats, EquiposAdapter.EquipoViewHolder>(EquipoWithStatsDiffCallback) {

    private val imageJobs = mutableMapOf<ImageView, Job>()

    inner class EquipoViewHolder(internal val binding: ItemEquipoBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(equipoWithStats: EquipoWithStats) {
            val equipo = equipoWithStats.equipo
            val stats = equipoWithStats.estadisticas

            binding.apply {
                tvNombre.text = equipo.nombre
                tvPartidos.text = root.context.getString(R.string.partidos_jugados_format, stats.partidosJugados)
                tvVictorias.text = root.context.getString(R.string.victorias_format, stats.victorias)

                imageJobs[ivTeamLogo]?.cancel()
                ImageLoader.loadBase64Image(
                    base64 = equipo.imagenBase64,
                    imageView = ivTeamLogo,
                    defaultDrawable = R.drawable.ic_default_team_placeholder
                )

                root.setOnClickListener { onItemClick(equipo.id) }
                btnDelete.setOnClickListener { onDeleteClick(equipo.id) }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EquipoViewHolder {
        val binding = ItemEquipoBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return EquipoViewHolder(binding)
    }

    override fun onBindViewHolder(holder: EquipoViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    override fun onViewRecycled(holder: EquipoViewHolder) {
        super.onViewRecycled(holder)
        imageJobs.remove(holder.binding.ivTeamLogo)?.cancel() // ← Ahora funciona
    }

    // Helper para mantener compatibilidad
    fun submitEquiposList(equiposConStats: List<Pair<com.example.futboldata.data.model.Equipo, com.example.futboldata.data.model.Estadisticas>>) {
        val equipoWithStatsList = equiposConStats.map { (equipo, stats) ->
            EquipoWithStats(equipo, stats)
        }
        submitList(equipoWithStatsList)
    }
}