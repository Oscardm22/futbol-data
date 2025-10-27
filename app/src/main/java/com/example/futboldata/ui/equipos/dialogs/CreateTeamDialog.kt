package com.example.futboldata.ui.equipos.dialogs

import android.app.Dialog
import android.net.Uri
import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.fragment.app.DialogFragment
import com.example.futboldata.R
import com.example.futboldata.data.model.Equipo
import com.example.futboldata.databinding.DialogNuevoEquipoBinding
import com.example.futboldata.ui.equipos.EquiposActivity
import com.example.futboldata.utils.TeamValidator
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import java.util.*

class CreateTeamDialog : DialogFragment() {

    private var _binding: DialogNuevoEquipoBinding? = null
    private val binding get() = _binding!!

    private var onTeamCreated: ((Equipo) -> Unit)? = null
    private var onImageSelectRequest: (() -> Unit)? = null

    private var teamPhotoUri: Uri? = null

    fun setOnTeamCreatedListener(listener: (Equipo) -> Unit) {
        onTeamCreated = listener
    }

    fun setOnImageSelectRequestListener(listener: () -> Unit) {
        onImageSelectRequest = listener
    }

    fun updateTeamImage(uri: Uri) {
        teamPhotoUri = uri
        binding.ivTeamPhoto.setImageURI(uri)
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        _binding = DialogNuevoEquipoBinding.inflate(layoutInflater)

        setupUI()

        return MaterialAlertDialogBuilder(requireContext())
            .setTitle("Nuevo Equipo")
            .setView(binding.root)
            .setPositiveButton("Guardar", null)
            .setNegativeButton("Cancelar", null)
            .create().apply {
                setOnShowListener {
                    customizeDialogAppearance()
                    setupPositiveButton()
                }
            }
    }

    private fun setupUI() {
        binding.fabAddPhoto.setOnClickListener {
            onImageSelectRequest?.invoke()
        }

        binding.etNombreEquipo.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus) binding.tilNombreEquipo.error = null
        }
    }

    private fun customizeDialogAppearance() {
        val alertDialog = dialog as? AlertDialog ?: return

        val textView = alertDialog.findViewById<TextView>(android.R.id.title)
        textView?.setTextColor(ContextCompat.getColor(requireContext(), R.color.Fondo))

        alertDialog.getButton(AlertDialog.BUTTON_POSITIVE)?.setTextColor(
            ContextCompat.getColor(requireContext(), R.color.botones_positivos))

        alertDialog.getButton(AlertDialog.BUTTON_NEGATIVE)?.setTextColor(
            ContextCompat.getColor(requireContext(), R.color.Fondo))
    }

    private fun setupPositiveButton() {
        val alertDialog = dialog as? AlertDialog ?: return

        alertDialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener {
            if (validateForm()) {
                createTeam()
                dismiss()
            }
        }
    }

    private fun validateForm(): Boolean {
        val nombre = binding.etNombreEquipo.text.toString().trim()

        when (val result = TeamValidator.validateTeamName(nombre)) {
            is TeamValidator.ValidationResult.Valid -> {
                binding.tilNombreEquipo.error = null
                return true
            }
            is TeamValidator.ValidationResult.Invalid -> {
                binding.tilNombreEquipo.error = result.errorMessage
                return false
            }
        }
    }

    private fun createTeam() {
        val nombre = binding.etNombreEquipo.text.toString().trim()
        val imagenBase64 = teamPhotoUri?.let { uri ->
            (requireActivity() as? EquiposActivity)?.convertImageToBase64(uri)
        } ?: ""

        val equipo = Equipo(
            nombre = nombre,
            fechaCreacion = Date(),
            imagenBase64 = imagenBase64
        )

        onTeamCreated?.invoke(equipo)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        fun newInstance(): CreateTeamDialog {
            return CreateTeamDialog()
        }
    }
}