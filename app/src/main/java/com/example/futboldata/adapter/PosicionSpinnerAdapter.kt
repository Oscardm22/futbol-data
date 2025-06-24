package com.example.futboldata.adapter

import android.content.Context
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView
import com.example.futboldata.data.model.Posicion

class PosicionSpinnerAdapter(
    context: Context,
    posiciones: List<Posicion>
) : ArrayAdapter<Posicion>(context, android.R.layout.simple_spinner_item, posiciones) {

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val view = super.getView(position, convertView, parent) as TextView
        view.text = getItem(position)?.displayName
        return view
    }

    override fun getDropDownView(position: Int, convertView: View?, parent: ViewGroup): View {
        val view = super.getDropDownView(position, convertView, parent) as TextView
        view.text = getItem(position)?.displayName
        return view
    }
}