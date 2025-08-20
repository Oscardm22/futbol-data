package com.example.futboldata.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.futboldata.adapter.diffcallbacks.DestacadosDiffCallback
import com.example.futboldata.databinding.ItemEstadisticaDestacadaBinding
import com.example.futboldata.databinding.ItemHeaderDestacadoBinding
import com.example.futboldata.ui.equipos.fragments.EstadisticaDestacada

class DestacadosAdapter : ListAdapter<EstadisticaDestacada, RecyclerView.ViewHolder>(DestacadosDiffCallback()) {

    companion object {
        private const val TYPE_HEADER = 0
        private const val TYPE_ITEM = 1
    }

    inner class HeaderViewHolder(private val binding: ItemHeaderDestacadoBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(estadistica: EstadisticaDestacada) {
            binding.tvTitulo.text = estadistica.tipo
            val drawable = ContextCompat.getDrawable(binding.root.context, estadistica.icono)
            binding.tvTitulo.setCompoundDrawablesRelativeWithIntrinsicBounds(
                drawable, null, null, null
            )
        }
    }

    inner class ItemViewHolder(private val binding: ItemEstadisticaDestacadaBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(estadistica: EstadisticaDestacada) {
            binding.tvTipo.text = estadistica.tipo
            binding.tvJugador.text = estadistica.jugador
            binding.tvValor.text = estadistica.valor
            binding.ivIcono.visibility = View.GONE
        }
    }

    override fun getItemViewType(position: Int): Int {
        return if (getItem(position).esHeader) TYPE_HEADER else TYPE_ITEM
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            TYPE_HEADER -> {
                val binding = ItemHeaderDestacadoBinding.inflate(
                    LayoutInflater.from(parent.context),
                    parent,
                    false
                )
                HeaderViewHolder(binding)
            }
            else -> {
                val binding = ItemEstadisticaDestacadaBinding.inflate(
                    LayoutInflater.from(parent.context),
                    parent,
                    false
                )
                ItemViewHolder(binding)
            }
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val item = getItem(position)
        when (holder) {
            is HeaderViewHolder -> holder.bind(item)
            is ItemViewHolder -> holder.bind(item)
        }
    }
}