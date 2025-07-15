package com.example.futboldata.adapter

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.futboldata.data.model.Equipo
import com.example.futboldata.databinding.ItemEquipoBinding

class EquiposAdapter(
    private var equipos: List<Pair<String, Equipo>>,
    private val onItemClick: (String) -> Unit,
    private val onDeleteClick: (String) -> Unit
) : RecyclerView.Adapter<EquiposAdapter.EquipoViewHolder>() {

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
            root.setOnClickListener { onItemClick(id) }
            btnDelete.setOnClickListener { onDeleteClick(id) }
        }
    }

    override fun getItemCount() = equipos.size

    @SuppressLint("NotifyDataSetChanged")
    fun updateList(newEquipos: List<Pair<String, Equipo>>) {
        equipos = newEquipos
        notifyDataSetChanged()
    }
}