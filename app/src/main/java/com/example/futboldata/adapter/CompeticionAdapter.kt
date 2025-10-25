package com.example.futboldata.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.futboldata.R
import com.example.futboldata.data.model.Competicion
import com.example.futboldata.data.model.toDisplayName
import com.example.futboldata.adapter.diffcallbacks.CompeticionDiffCallback
import com.example.futboldata.databinding.ItemCompeticionBinding
import com.example.futboldata.utils.ImageLoader
import kotlinx.coroutines.Job

class CompeticionAdapter(
    private val onItemClick: (Competicion) -> Unit,
    private val onDeleteClick: (Competicion) -> Unit,
    private val modoFiltro: Boolean = false
) : ListAdapter<Competicion, CompeticionAdapter.CompeticionViewHolder>(CompeticionDiffCallback()) {

    private val imageJobs = mutableMapOf<ImageView, Job>()

    inner class CompeticionViewHolder(val binding: ItemCompeticionBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CompeticionViewHolder {
        val binding = ItemCompeticionBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return CompeticionViewHolder(binding)
    }

    override fun onBindViewHolder(holder: CompeticionViewHolder, position: Int) {
        val competicion = getItem(position)
        holder.binding.apply {
            tvNombre.text = competicion.nombre
            tvTipo.text = competicion.tipo.toDisplayName()

            // Ocultar botón de eliminar si está en modo filtro
            if (modoFiltro) {
                btnDelete.visibility = View.GONE
            } else {
                btnDelete.visibility = View.VISIBLE
                btnDelete.setOnClickListener { onDeleteClick(competicion) }
            }

            ImageLoader.loadBase64Image(
                base64 = competicion.imagenBase64,
                imageView = ivLogo,
                defaultDrawable = R.drawable.ic_default_trophy
            )

            root.setOnClickListener { onItemClick(competicion) }
        }
    }

    override fun onViewRecycled(holder: CompeticionViewHolder) {
        super.onViewRecycled(holder)
        imageJobs.remove(holder.binding.ivLogo)?.cancel()
    }

    fun updateList(newCompeticiones: List<Competicion>) {
        submitList(newCompeticiones)
    }
}