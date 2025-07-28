package com.example.futboldata.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.futboldata.R
import com.example.futboldata.data.model.Partido

class PartidosAdapter(private val matches: List<Partido>) : RecyclerView.Adapter<PartidosAdapter.MatchViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MatchViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_match, parent, false)
        return MatchViewHolder(view)
    }

    override fun onBindViewHolder(holder: MatchViewHolder, position: Int) {
        holder.bind(matches[position])
    }

    override fun getItemCount(): Int = matches.size

    class MatchViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        fun bind(match: Partido) {
            itemView.findViewById<TextView>(R.id.textViewFecha).text = match.fecha.toString()
            itemView.findViewById<TextView>(R.id.textViewRival).text = match.rival
            itemView.findViewById<TextView>(R.id.textViewResultado).text = match.resultado
        }
    }
}