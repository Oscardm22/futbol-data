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

class DestacadosAdapter(
    private val onJugadorClick: (String) -> Unit
) : ListAdapter<EstadisticaDestacada, RecyclerView.ViewHolder>(DestacadosDiffCallback()) {

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

    inner class ItemViewHolder(
        private val binding: ItemEstadisticaDestacadaBinding,
        private val onJugadorClick: (String) -> Unit
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(estadistica: EstadisticaDestacada) {
            binding.tvTipo.text = estadistica.tipo
            binding.tvJugador.text = estadistica.jugador
            binding.tvValor.text = estadistica.valor
            binding.ivIcono.visibility = View.GONE

            // Agregar click listener solo si es un item de jugador (no header)
            if (!estadistica.esHeader && estadistica.jugador.isNotBlank()) {
                binding.root.setOnClickListener {
                    onJugadorClick(estadistica.jugador)
                }
                binding.root.isClickable = true
            } else {
                binding.root.isClickable = false
            }
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
                ItemViewHolder(binding, onJugadorClick) // ← Pasa el callback al ItemViewHolder
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