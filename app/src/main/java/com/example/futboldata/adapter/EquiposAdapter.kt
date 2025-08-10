package com.example.futboldata.adapter

import android.annotation.SuppressLint
import android.graphics.BitmapFactory
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.example.futboldata.R
import com.example.futboldata.data.model.Equipo
import com.example.futboldata.data.model.Estadisticas
import com.example.futboldata.databinding.ItemEquipoBinding
import kotlinx.coroutines.Job

class EquiposAdapter(
    private var equiposConStats: List<Pair<Equipo, Estadisticas>>,
    private val onItemClick: (String) -> Unit,
    private val onDeleteClick: (String) -> Unit
) : RecyclerView.Adapter<EquiposAdapter.EquipoViewHolder>() {

    val currentList: List<Equipo>
        get() = equiposConStats.map { it.first }

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
            if (equipo.imagenBase64.isNotEmpty()) {
                loadImageFromBase64(equipo.imagenBase64, ivTeamLogo)
            } else {
                ivTeamLogo.setImageResource(R.drawable.ic_default_team_placeholder)
            }

            root.setOnClickListener { onItemClick(equipo.id) }
            btnDelete.setOnClickListener { onDeleteClick(equipo.id) }
        }
    }

    private fun loadImageFromBase64(base64: String, imageView: ImageView) {
        try {
            val decodedBytes = android.util.Base64.decode(base64, android.util.Base64.DEFAULT)

            // Calcular el tamaño de muestra óptimo para reducir memoria
            val options = BitmapFactory.Options().apply {
                inJustDecodeBounds = true
            }
            BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.size, options)

            // Calcular inSampleSize
            options.inSampleSize = calculateInSampleSize(options, 200, 200) // Tamaño objetivo 200x200

            // Decodificar con las nuevas opciones
            options.inJustDecodeBounds = false
            val bitmap = BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.size, options)

            imageView.setImageBitmap(bitmap)
        } catch (e: Exception) {
            Log.e("EquiposAdapter", "Error al cargar imagen Base64", e)
            imageView.setImageResource(R.drawable.ic_default_team_placeholder)
        }
    }

    private fun calculateInSampleSize(options: BitmapFactory.Options, reqWidth: Int, reqHeight: Int): Int {
        val (height: Int, width: Int) = options.run { outHeight to outWidth }
        var inSampleSize = 1

        if (height > reqHeight || width > reqWidth) {
            val halfHeight: Int = height / 2
            val halfWidth: Int = width / 2

            while (halfHeight / inSampleSize >= reqHeight && halfWidth / inSampleSize >= reqWidth) {
                inSampleSize *= 2
            }
        }
        return inSampleSize
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