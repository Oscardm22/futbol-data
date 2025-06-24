package com.example.futboldata.adapter

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.futboldata.data.model.Equipo
import com.example.futboldata.databinding.ItemEquipoBinding

class EquiposAdapter(
    private var equipos: List<Equipo>,
    private val onItemClick: (Equipo) -> Unit,
    private val onDeleteClick: (Equipo) -> Unit
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
        val equipo = equipos[position]
        holder.binding.apply {
            tvNombre.text = equipo.nombre
            root.setOnClickListener { onItemClick(equipo) }
            btnDelete.setOnClickListener { onDeleteClick(equipo) }
        }
    }

    override fun getItemCount() = equipos.size

    @SuppressLint("NotifyDataSetChanged")
    fun updateList(newEquipos: List<Equipo>) {
        equipos = newEquipos
        notifyDataSetChanged()
    }
}