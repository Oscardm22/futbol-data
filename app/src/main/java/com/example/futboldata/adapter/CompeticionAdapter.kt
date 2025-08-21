package com.example.futboldata.adapter

import android.annotation.SuppressLint
import android.graphics.BitmapFactory
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.example.futboldata.R
import com.example.futboldata.data.model.Competicion
import com.example.futboldata.data.model.toDisplayName
import com.example.futboldata.databinding.ItemCompeticionBinding
import kotlinx.coroutines.Job

class CompeticionAdapter(
    private var competiciones: List<Competicion>,
    private val onItemClick: (Competicion) -> Unit,
    private val onDeleteClick: (Competicion) -> Unit,
    private val modoFiltro: Boolean = false // Nuevo parámetro para modo filtro
) : RecyclerView.Adapter<CompeticionAdapter.CompeticionViewHolder>() {

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
        val competicion = competiciones[position]
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

            // Carga del logo
            if (competicion.imagenBase64.isNotEmpty()) {
                try {
                    val decodedBytes = android.util.Base64.decode(competicion.imagenBase64, android.util.Base64.DEFAULT)
                    val bitmap = BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.size)
                    ivLogo.setImageBitmap(bitmap)
                } catch (e: Exception) {
                    ivLogo.setImageResource(R.drawable.ic_default_trophy)
                }
            } else {
                ivLogo.setImageResource(R.drawable.ic_default_trophy)
            }

            root.setOnClickListener { onItemClick(competicion) }
        }
    }

    private fun loadImageFromBase64(base64: String, imageView: ImageView) {
        try {
            val decodedBytes = android.util.Base64.decode(base64, android.util.Base64.DEFAULT)

            // Calcular el tamaño de muestra óptimo
            val options = BitmapFactory.Options().apply {
                inJustDecodeBounds = true
            }
            BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.size, options)

            options.inSampleSize = calculateInSampleSize(options, 200, 200)
            options.inJustDecodeBounds = false

            val bitmap = BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.size, options)
            imageView.setImageBitmap(bitmap)
        } catch (e: Exception) {
            Log.e("CompeticionAdapter", "Error al cargar imagen Base64", e)
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

    override fun getItemCount() = competiciones.size

    @SuppressLint("NotifyDataSetChanged")
    fun updateList(newCompeticiones: List<Competicion>) {
        competiciones = newCompeticiones
        notifyDataSetChanged()
    }
}