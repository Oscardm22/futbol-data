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
import com.example.futboldata.databinding.ItemEquipoBinding
import kotlinx.coroutines.Job

class EquiposAdapter(
    private var equipos: List<Pair<String, Equipo>>,
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
        val (id, equipo) = equipos[position]
        holder.binding.apply {
            tvNombre.text = equipo.nombre

            // Cancelar cualquier carga previa para esta ImageView
            imageJobs[ivTeamLogo]?.cancel()

            if (equipo.imagenBase64.isNotEmpty()) {
                loadImageFromBase64(equipo.imagenBase64, ivTeamLogo)
            } else {
                ivTeamLogo.setImageResource(R.drawable.ic_default_team_placeholder)
            }

            root.setOnClickListener { onItemClick(id) }
            btnDelete.setOnClickListener { onDeleteClick(id) }
        }
    }

    private fun loadImageFromBase64(base64: String, imageView: ImageView) {
        try {
            val decodedBytes = android.util.Base64.decode(base64, android.util.Base64.DEFAULT)
            val bitmap = BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.size)
            imageView.setImageBitmap(bitmap)
        } catch (e: Exception) {
            Log.e("EquiposAdapter", "Error al cargar imagen Base64", e)
            imageView.setImageResource(R.drawable.ic_default_team_placeholder)
        }
    }

    override fun onViewRecycled(holder: EquipoViewHolder) {
        super.onViewRecycled(holder)
        // Cancelar la carga de imagen cuando el ViewHolder se recicla
        imageJobs.remove(holder.binding.ivTeamLogo)?.cancel()
    }

    override fun getItemCount() = equipos.size

    @SuppressLint("NotifyDataSetChanged")
    fun updateList(newEquipos: List<Pair<String, Equipo>>) {
        equipos = newEquipos
        notifyDataSetChanged()
    }
}