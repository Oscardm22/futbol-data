package com.example.futboldata.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.futboldata.data.model.Competicion
import com.example.futboldata.databinding.ItemCompeticionBinding

class CompeticionAdapter(
    private val onItemClick: (Competicion) -> Unit
) : ListAdapter<Competicion, CompeticionAdapter.ViewHolder>(CompeticionDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemCompeticionBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class ViewHolder(private val binding: ItemCompeticionBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(competicion: Competicion) {
            binding.tvNombre.text = competicion.nombre
            binding.tvTemporada.text = competicion.temporada
            // Cargar logo con Glide/Picasso si es necesario

            binding.root.setOnClickListener { onItemClick(competicion) }
        }
    }
}