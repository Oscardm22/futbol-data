package com.example.futboldata.adapter

import android.graphics.BitmapFactory
import android.util.Log
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

            if (competicion.imagenBase64.isNotEmpty()) {
                loadImageFromBase64(competicion.imagenBase64, ivLogo)
            } else {
                ivLogo.setImageResource(R.drawable.ic_default_trophy)
            }

            root.setOnClickListener { onItemClick(competicion) }
        }
    }

    private fun loadImageFromBase64(base64: String, imageView: ImageView) {
        // Verificar si la cadena base64 está vacía primero
        if (base64.isEmpty()) {
            imageView.setImageResource(R.drawable.ic_default_trophy)
            return
        }

        try {
            val decodedBytes = android.util.Base64.decode(base64, android.util.Base64.DEFAULT)

            // Verificar si los bytes decodificados están vacíos
            if (decodedBytes.isEmpty()) {
                imageView.setImageResource(R.drawable.ic_default_trophy)
                return
            }

            // Calcular el tamaño de muestra óptimo
            val options = BitmapFactory.Options().apply {
                inJustDecodeBounds = true
            }
            BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.size, options)

            // Si no se pudieron obtener las dimensiones, usar icono por defecto
            if (options.outWidth <= 0 || options.outHeight <= 0) {
                imageView.setImageResource(R.drawable.ic_default_trophy)
                return
            }

            options.inSampleSize = calculateInSampleSize(options, 200, 200)
            options.inJustDecodeBounds = false

            val bitmap = BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.size, options)

            if (bitmap != null) {
                imageView.setImageBitmap(bitmap)
            } else {
                imageView.setImageResource(R.drawable.ic_default_trophy)
            }
        } catch (e: Exception) {
            Log.e("CompeticionAdapter", "Error al cargar imagen Base64: ${e.message}", e)
            imageView.setImageResource(R.drawable.ic_default_trophy)
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

    override fun onViewRecycled(holder: CompeticionViewHolder) {
        super.onViewRecycled(holder)
        imageJobs.remove(holder.binding.ivLogo)?.cancel()
    }

    fun updateList(newCompeticiones: List<Competicion>) {
        submitList(newCompeticiones)
    }
}