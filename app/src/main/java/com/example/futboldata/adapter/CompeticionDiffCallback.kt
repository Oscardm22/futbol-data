package com.example.futboldata.adapter

import androidx.recyclerview.widget.DiffUtil
import com.example.futboldata.data.model.Competicion

class CompeticionDiffCallback : DiffUtil.ItemCallback<Competicion>() {
    override fun areItemsTheSame(oldItem: Competicion, newItem: Competicion): Boolean {
        return oldItem.id == newItem.id
    }

    override fun areContentsTheSame(oldItem: Competicion, newItem: Competicion): Boolean {
        return oldItem == newItem
    }
}