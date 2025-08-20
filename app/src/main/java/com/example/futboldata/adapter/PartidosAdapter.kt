package com.example.futboldata.adapter

import android.graphics.BitmapFactory
import android.graphics.drawable.Drawable
import android.util.Base64
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.futboldata.R
import com.example.futboldata.data.model.Partido
import com.example.futboldata.data.model.TipoCompeticion
import java.text.SimpleDateFormat
import java.util.*
import androidx.core.graphics.drawable.toDrawable
import com.example.futboldata.databinding.ItemMatchBinding

class PartidosAdapter(
    private val matches: List<Partido>,
    private val competitionImages: Map<String, String>,
    private val teamNames: Map<String, String>,
    private val competitionNames: Map<String, String>,
    private val competitionTypes: Map<String, TipoCompeticion>,
    private val onPartidoClickListener: (Partido) -> Unit
) : RecyclerView.Adapter<PartidosAdapter.MatchViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MatchViewHolder {
        val binding = ItemMatchBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return MatchViewHolder(binding, teamNames, competitionNames, competitionTypes, onPartidoClickListener)
    }

    override fun onBindViewHolder(holder: MatchViewHolder, position: Int) {
        holder.bind(matches[position], competitionImages[matches[position].competicionId])
    }

    override fun getItemCount(): Int = matches.size

    class MatchViewHolder(
        private val binding: ItemMatchBinding,
        private val teamNames: Map<String, String>,
        private val competitionNames: Map<String, String>,
        private val competitionTypes: Map<String, TipoCompeticion>,
        private val onPartidoClickListener: (Partido) -> Unit
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(match: Partido, competitionImage: String?) {

            binding.root.setOnClickListener {
                onPartidoClickListener(match)
            }
            // Formatear fecha
            val dateFormat = SimpleDateFormat("dd MMM yyyy - hh:mm a", Locale.getDefault())
            binding.textViewFecha.text = dateFormat.format(match.fecha)

            // Obtener nombre del equipo desde el mapa
            val nombreEquipo = teamNames[match.equipoId] ?: "Mi Equipo"

            // Determinar qué equipo es local y cuál visitante según esLocal
            if (match.esLocal) {
                // Tu equipo es local, rival es visitante
                binding.textViewMiEquipo.text = nombreEquipo
                binding.textViewRival.text = match.rival
                binding.textViewHomeScore.text = match.golesEquipo.toString()
                binding.textViewAwayScore.text = match.golesRival.toString()
            } else {
                // Tu equipo es visitante, rival es local
                binding.textViewMiEquipo.text = match.rival
                binding.textViewRival.text = nombreEquipo
                binding.textViewHomeScore.text = match.golesRival.toString() // Goles del rival (local)
                binding.textViewAwayScore.text = match.golesEquipo.toString() // Tus goles (visitante)
            }

            // Obtener y establecer nombre de la competición
            val nombreCompeticion = competitionNames[match.competicionId] ?: "Competición"
            binding.textViewCompeticion.text = nombreCompeticion

            // Obtener tipo de competición y mostrar fase o jornada según corresponda
            val tipoCompeticion = competitionTypes[match.competicionId] ?: TipoCompeticion.LIGA

            when (tipoCompeticion) {
                TipoCompeticion.LIGA -> {
                    // Para ligas: mostrar jornada
                    match.jornada?.let {
                        binding.textViewJornada.text = itemView.context.getString(R.string.jornada_template, it)
                        binding.textViewJornada.visibility = View.VISIBLE
                    } ?: run {
                        binding.textViewJornada.visibility = View.GONE
                    }
                }
                TipoCompeticion.COPA_NACIONAL,
                TipoCompeticion.COPA_INTERNACIONAL,
                TipoCompeticion.SUPERCOPA -> {
                    // Para todas las copas: mostrar fase
                    match.fase?.let {
                        binding.textViewJornada.text = it
                        binding.textViewJornada.visibility = View.VISIBLE
                    } ?: run {
                        binding.textViewJornada.visibility = View.GONE
                    }
                }
            }

            // Establecer imagen de competición
            setCompetitionImage(competitionImage)

            // Cambiar color del marcador según el resultado
            val resultadoColor = when {
                match.golesEquipo > match.golesRival -> ContextCompat.getColor(itemView.context, R.color.win_color)
                match.golesEquipo < match.golesRival -> ContextCompat.getColor(itemView.context, R.color.lose_color)
                else -> ContextCompat.getColor(itemView.context, R.color.draw_color)
            }

            // Aplicar el color al marcador correcto según si es local o visitante
            if (match.esLocal) {
                binding.textViewHomeScore.setTextColor(resultadoColor) // Tu marcador (local)
                binding.textViewAwayScore.setTextColor(ContextCompat.getColor(itemView.context, android.R.color.black))
            } else {
                binding.textViewAwayScore.setTextColor(resultadoColor) // Tu marcador (visitante)
                binding.textViewHomeScore.setTextColor(ContextCompat.getColor(itemView.context, android.R.color.black))
            }
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

            binding.textViewCompeticion.setCompoundDrawablesRelativeWithIntrinsicBounds(
                drawable,
                null,
                null,
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
                val sizeInPx = (24 * itemView.context.resources.displayMetrics.density).toInt()
                setBounds(0, 0, sizeInPx, sizeInPx)
            }
        }
    }
}