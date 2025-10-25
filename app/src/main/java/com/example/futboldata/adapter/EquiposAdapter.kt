package com.example.futboldata.adapter

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.example.futboldata.R
import com.example.futboldata.data.model.Equipo
import com.example.futboldata.data.model.Estadisticas
import com.example.futboldata.databinding.ItemEquipoBinding
import com.example.futboldata.utils.ImageLoader
import kotlinx.coroutines.Job

class EquiposAdapter(
    private var equiposConStats: List<Pair<Equipo, Estadisticas>>,
    private val onItemClick: (String) -> Unit,
    private val onDeleteClick: (String) -> Unit
) : RecyclerView.Adapter<EquiposAdapter.EquipoViewHolder>() {

    private val imageJobs = mutableMapOf<ImageView, Job>()

    inner class EquipoViewHolder(val binding: ItemEquipoBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EquipoViewHolder {
        val binding = ItemEquipoBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return EquipoViewHolder(binding)
    }

    override fun onBindViewHolder(holder: EquipoViewHolder, position: Int) {
        val (equipo, stats) = equiposConStats[position]
        holder.binding.apply {
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

    override fun onViewRecycled(holder: EquipoViewHolder) {
        super.onViewRecycled(holder)
        // Cancelar la carga de imagen cuando el ViewHolder se recicla
        imageJobs.remove(holder.binding.ivTeamLogo)?.cancel()
    }

    override fun getItemCount() = equiposConStats.size

    @SuppressLint("NotifyDataSetChanged")
    fun updateList(newEquiposConStats: List<Pair<Equipo, Estadisticas>>) {
        equiposConStats = newEquiposConStats
        notifyDataSetChanged()
    }
}