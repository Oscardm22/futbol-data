package com.example.futboldata.ui.equipos

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.text.Spannable
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.futboldata.FutbolDataApp
import com.example.futboldata.R
import com.example.futboldata.adapter.EquiposAdapter
import com.example.futboldata.databinding.ActivityEquiposBinding
import com.example.futboldata.ui.auth.LoginActivity
import com.example.futboldata.ui.competiciones.CompeticionesActivity
import com.example.futboldata.utils.PermissionManager
import com.example.futboldata.utils.SessionManager
import com.example.futboldata.viewmodel.EquipoViewModel
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import androidx.core.view.size
import androidx.core.view.get
import com.example.futboldata.ui.equipos.dialogs.CreateTeamDialog
import com.example.futboldata.ui.equipos.dialogs.DeleteTeamDialog
import com.example.futboldata.utils.ImageHelper

class EquiposActivity : AppCompatActivity() {

    private lateinit var binding: ActivityEquiposBinding
    private lateinit var permissionManager: PermissionManager
    private lateinit var imageHelper: ImageHelper
    private val viewModel: EquipoViewModel by viewModels {
        (application as FutbolDataApp).viewModelFactory
    }

    private var teamPhotoUri: Uri? = null
    private var currentDialog: AlertDialog? = null

    private val pickImageLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == RESULT_OK) {
            result.data?.data?.let { uri ->
                teamPhotoUri = uri
                updateCreateTeamDialogWithImage(uri)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEquiposBinding.inflate(layoutInflater)
        setContentView(binding.root)

        permissionManager = PermissionManager.forActivity(this)
        imageHelper = ImageHelper(this)
        setupUI()
        setupObservers()
    }

    private fun setupUI() {
        setSupportActionBar(binding.toolbar)
        setupRecyclerView()
        setupFAB()
    }

    private fun setupRecyclerView() {
        binding.rvEquipos.apply {
            layoutManager = LinearLayoutManager(this@EquiposActivity)
            adapter = EquiposAdapter(
                onItemClick = { equipoId -> abrirDetalleEquipo(equipoId) },
                onDeleteClick = { equipoId -> mostrarDialogoEliminacion(equipoId) }
            )
        }
    }

    private fun setupFAB() {
        binding.fabAddEquipo.setOnClickListener {
            abrirDialogoCreacionEquipo()
        }
    }

    private fun setupObservers() {
        viewModel.equiposConStats.observe(this) { equiposConStats ->
            val adapter = binding.rvEquipos.adapter as? EquiposAdapter ?: run {
                EquiposAdapter(
                    onItemClick = { equipoId -> abrirDetalleEquipo(equipoId) },
                    onDeleteClick = { equipoId -> mostrarDialogoEliminacion(equipoId) }
                ).also {
                    binding.rvEquipos.adapter = it
                }
            }
            adapter.submitEquiposList(equiposConStats)
        }

        viewModel.equiposState.observe(this) { state ->
            when (state) {
                is EquipoViewModel.EquipoState.Loading -> {
                    binding.progressBar.visibility = View.VISIBLE
                }
                is EquipoViewModel.EquipoState.Success -> {
                    binding.progressBar.visibility = View.GONE
                }
                is EquipoViewModel.EquipoState.Error -> {
                    binding.progressBar.visibility = View.GONE
                    Toast.makeText(this, state.mensaje, Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)

        for (i in 0 until menu.size) {
            val menuItem = menu[i]
            val spanString = SpannableString(menuItem.title.toString())
            spanString.setSpan(
                ForegroundColorSpan(ContextCompat.getColor(this, R.color.Fondo)),
                0, spanString.length,
                Spannable.SPAN_INCLUSIVE_INCLUSIVE
            )
            menuItem.title = spanString
        }

        try {
            val method = menu.javaClass.getDeclaredMethod("setOptionalIconsVisible", Boolean::class.javaPrimitiveType)
            method.isAccessible = true
            method.invoke(menu, true)
        } catch (e: Exception) {
            e.printStackTrace()
        }

        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_competiciones -> {
                startActivity(Intent(this, CompeticionesActivity::class.java))
                true
            }
            R.id.action_logout -> {
                logout()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun logout() {
        Firebase.auth.signOut()
        SessionManager(this).clearUser()

        startActivity(Intent(this, LoginActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            putExtra("LOGOUT", true)
        })
        finish()
    }

    private fun abrirDialogoCreacionEquipo() {
        val dialog = CreateTeamDialog.newInstance()

        dialog.setOnTeamCreatedListener { equipo ->
            viewModel.guardarEquipo(equipo)
        }

        dialog.setOnImageSelectRequestListener {
            checkAndRequestPhotoPermissions()
        }

        dialog.show(supportFragmentManager, "CreateTeamDialog")
    }

    private fun updateCreateTeamDialogWithImage(uri: Uri) {
        val dialog = supportFragmentManager.findFragmentByTag("CreateTeamDialog") as? CreateTeamDialog
        dialog?.updateTeamImage(uri)
        teamPhotoUri = uri
    }

    private fun checkAndRequestPhotoPermissions() {
        permissionManager.checkAndRequestImagePermission(
            onGranted = { showImagePicker() },
            onDenied = {
                showPermissionDeniedToast()
            },
            onRationale = {
                showPermissionRationaleDialog()
            }
        )
    }

    private fun showPermissionRationaleDialog() {
        MaterialAlertDialogBuilder(this)
            .setTitle("Permiso necesario")
            .setMessage("Necesitamos acceso a tus fotos para que puedas agregar imágenes a los equipos")
            .setPositiveButton("Entendido") { _, _ ->
                permissionManager.requestImagePermission { isGranted ->
                    if (isGranted) {
                        showImagePicker()
                    } else {
                        showPermissionDeniedToast()
                    }
                }
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun showPermissionDeniedToast() {
        Toast.makeText(
            this,
            "Permiso denegado. No puedes seleccionar imágenes sin este permiso.",
            Toast.LENGTH_SHORT
        ).show()
    }

    private fun showImagePicker() {
        val intent = Intent(Intent.ACTION_GET_CONTENT).apply {
            type = "image/*"
        }
        pickImageLauncher.launch(intent)
    }

    internal fun convertImageToBase64(uri: Uri): String? {
        return imageHelper.convertUriToBase64(uri)
    }

    private fun abrirDetalleEquipo(equipoId: String) {
        val intent = Intent(this, EquipoDetailActivity::class.java).apply {
            putExtra("equipo_id", equipoId)
        }
        startActivity(intent)
    }

    private fun mostrarDialogoEliminacion(equipoId: String) {
        val dialog = DeleteTeamDialog()

        dialog.setOnConfirmListener {
            viewModel.eliminarEquipo(equipoId)
            deleteTeamPhotoFromStorage(equipoId)
        }

        dialog.show(supportFragmentManager, "DeleteTeamDialog")
    }

    private fun deleteTeamPhotoFromStorage(equipoId: String) {
        val storageRef = Firebase.storage.reference
        val teamPhotoRef = storageRef.child("team_photos/$equipoId.jpg")

        teamPhotoRef.delete().addOnFailureListener {
            // Error no crítico, se puede ignorar
        }
    }

    override fun onDestroy() {
        currentDialog?.dismiss()
        currentDialog = null
        super.onDestroy()
    }
}