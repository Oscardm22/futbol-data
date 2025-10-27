package com.example.futboldata.ui.equipos.dialogs

import android.app.Dialog
import android.os.Bundle
import androidx.fragment.app.DialogFragment
import com.google.android.material.dialog.MaterialAlertDialogBuilder

class DeleteTeamDialog : DialogFragment() {

    private var onConfirm: (() -> Unit)? = null

    fun setOnConfirmListener(listener: () -> Unit) {
        onConfirm = listener
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return MaterialAlertDialogBuilder(requireContext())
            .setTitle("Eliminar equipo")
            .setMessage("¿Seguro que quieres eliminar este equipo? Se eliminará también su foto.")
            .setPositiveButton("Eliminar") { _, _ ->
                onConfirm?.invoke()
            }
            .setNegativeButton("Cancelar", null)
            .create().apply {
                setOnShowListener {
                    getButton(android.app.AlertDialog.BUTTON_POSITIVE)?.setTextColor(
                        requireContext().getColor(com.example.futboldata.R.color.error_color))

                    getButton(android.app.AlertDialog.BUTTON_NEGATIVE)?.setTextColor(
                        requireContext().getColor(com.example.futboldata.R.color.Fondo))
                }
            }
    }
}