package com.example.futboldata.adapter

import android.graphics.BitmapFactory
import android.graphics.drawable.Drawable
import android.util.Base64
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.futboldata.R
import com.example.futboldata.data.model.Partido
import java.text.SimpleDateFormat
import java.util.*
import androidx.core.graphics.drawable.toDrawable

class PartidosAdapter(
    private val matches: List<Partido>,
    private val competitionImages: Map<String, String>,
    private val teamNames: Map<String, String>
) : RecyclerView.Adapter<PartidosAdapter.MatchViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MatchViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_match, parent, false)
        return MatchViewHolder(view, teamNames)
    }

    override fun onBindViewHolder(holder: MatchViewHolder, position: Int) {
        holder.bind(matches[position], competitionImages[matches[position].competicionId])
    }

    override fun getItemCount(): Int = matches.size

    class MatchViewHolder(
        itemView: View,
        private val teamNames: Map<String, String>
    ) : RecyclerView.ViewHolder(itemView) {
        private val fechaTextView: TextView = itemView.findViewById(R.id.textViewFecha)
        private val rivalTextView: TextView = itemView.findViewById(R.id.textViewRival)
        private val homeScoreTextView: TextView = itemView.findViewById(R.id.textViewHomeScore)
        private val awayScoreTextView: TextView = itemView.findViewById(R.id.textViewAwayScore)
        private val miEquipoTextView: TextView = itemView.findViewById(R.id.textViewMiEquipo)


        fun bind(match: Partido, competitionImage: String?) {
            // Formatear fecha
            val dateFormat = SimpleDateFormat("EEE dd MMM - HH:mm", Locale.getDefault())
            fechaTextView.text = dateFormat.format(match.fecha)

            // Obtener nombre del equipo desde el mapa
            val nombreEquipo = teamNames[match.equipoId] ?: "Mi Equipo"
            miEquipoTextView.text = nombreEquipo.toString() // Asegurar que es String

            // Asignar nombre del rival y resultado
            rivalTextView.text = match.rival.toString() // Asegurar que es String
            homeScoreTextView.text = match.golesEquipo.toString()
            awayScoreTextView.text = match.golesRival.toString()

            // Resto del código sin cambios...
            setCompetitionImage(competitionImage)

            val homeScoreColor = if (match.esLocal) {
                ContextCompat.getColor(itemView.context, R.color.botones_positivos)
            } else {
                val typedValue = android.util.TypedValue()
                itemView.context.theme.resolveAttribute(
                    android.R.attr.textColorPrimary,
                    typedValue,
                    true
                )
                typedValue.data
            }
            homeScoreTextView.setTextColor(homeScoreColor)
        }

        private fun setCompetitionImage(imageBase64: String?) {
            val drawable = if (!imageBase64.isNullOrEmpty()) {
                try {
                    // 1. Decodificar Base64 a Bitmap
                    val imageBytes = Base64.decode(imageBase64, Base64.DEFAULT)

                    // 2. Calcular tamaño deseado en píxeles
                    val sizeInPx = (24 * itemView.context.resources.displayMetrics.density).toInt()

                    // 3. Opciones para reducir el tamaño de carga
                    val options = BitmapFactory.Options().apply {
                        inJustDecodeBounds = true
                    }
                    BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size, options)

                    // 4. Calcular factor de escala
                    val scale = calculateInSampleSize(options, sizeInPx, sizeInPx)

                    // 5. Decodificar con el factor de escala
                    val finalOptions = BitmapFactory.Options().apply {
                        inSampleSize = scale
                    }
                    val bitmap = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size, finalOptions)

                    // 6. Crear drawable con tamaño fijo
                    bitmap.toDrawable(itemView.resources).apply {
                        setBounds(0, 0, sizeInPx, sizeInPx)
                    }
                } catch (e: Exception) {
                    // En caso de error, usar icono por defecto
                    createDefaultDrawable()
                }
            } else {
                createDefaultDrawable()
            }

            fechaTextView.setCompoundDrawablesWithIntrinsicBounds(
                null,
                null,
                drawable,
                null
            )
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

        private fun createDefaultDrawable(): Drawable? {
            return ContextCompat.getDrawable(itemView.context, R.drawable.ic_liga)?.apply {
                val sizeInPx = (48 * itemView.context.resources.displayMetrics.density).toInt()
                setBounds(0, 0, sizeInPx, sizeInPx)
            }
        }
    }
}