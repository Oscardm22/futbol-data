package com.example.futboldata.adapter

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
import com.example.futboldata.databinding.ItemMatchBinding
import com.example.futboldata.utils.ImageLoader

class PartidosAdapter(
    private val matches: List<Partido>,
    private val competitionImages: Map<String, String>,
    private val teamNames: Map<String, String>,
    private val competitionNames: Map<String, String>,
    private val competitionTypes: Map<String, TipoCompeticion>,
    private val onPartidoClickListener: (Partido) -> Unit,
    private val onPartidoDeleteListener: (Partido) -> Unit
) : RecyclerView.Adapter<PartidosAdapter.MatchViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MatchViewHolder {
        val binding = ItemMatchBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return MatchViewHolder(binding, teamNames, competitionNames, competitionTypes, onPartidoClickListener, onPartidoDeleteListener)
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
        private val onPartidoClickListener: (Partido) -> Unit,
        private val onPartidoDeleteListener: (Partido) -> Unit
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(match: Partido, competitionImage: String?) {

            binding.root.setOnClickListener {
                onPartidoClickListener(match)
            }

            binding.root.setOnLongClickListener {
                onPartidoDeleteListener(match)
                true
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
            val drawable = ImageLoader.loadBase64AsDrawable(
                base64 = competitionImage ?: "",
                context = itemView.context,
                defaultDrawable = R.drawable.ic_liga,
                targetSize = 72
            )

            binding.textViewCompeticion.setCompoundDrawablesRelativeWithIntrinsicBounds(
                drawable,
                null,
                null,
                null
            )

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
    }
}