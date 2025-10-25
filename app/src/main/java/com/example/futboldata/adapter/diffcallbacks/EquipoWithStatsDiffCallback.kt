package com.example.futboldata.adapter.diffcallbacks

import androidx.recyclerview.widget.DiffUtil
import com.example.futboldata.data.model.EquipoWithStats

object EquipoWithStatsDiffCallback : DiffUtil.ItemCallback<EquipoWithStats>() {
    override fun areItemsTheSame(oldItem: EquipoWithStats, newItem: EquipoWithStats): Boolean {
        return oldItem.equipo.id == newItem.equipo.id
    }

    override fun areContentsTheSame(oldItem: EquipoWithStats, newItem: EquipoWithStats): Boolean {
        return oldItem == newItem
    }
}