package com.example.futboldata.ui.competiciones

import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.Base64
import android.view.View
import android.widget.ArrayAdapter
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.futboldata.R
import com.example.futboldata.adapter.CompeticionAdapter
import com.example.futboldata.data.model.Competicion
import com.example.futboldata.data.model.TipoCompeticion
import com.example.futboldata.data.repository.impl.AuthRepositoryImpl
import com.example.futboldata.data.repository.impl.CompeticionRepositoryImpl
import com.example.futboldata.data.repository.impl.EquipoRepositoryImpl
import com.example.futboldata.data.repository.impl.JugadorRepositoryImpl
import com.example.futboldata.data.repository.impl.PartidoRepositoryImpl
import com.example.futboldata.databinding.ActivityCompeticionesBinding
import com.example.futboldata.databinding.DialogAddCompeticionBinding
import com.example.futboldata.utils.StatsCalculator
import com.example.futboldata.viewmodel.CompeticionViewModel
import com.example.futboldata.viewmodel.SharedViewModelFactory
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import android.Manifest
import android.provider.Settings
import com.example.futboldata.data.model.toDisplayName

class CompeticionesActivity : AppCompatActivity() {

    private lateinit var binding: ActivityCompeticionesBinding
    private val viewModel: CompeticionViewModel by viewModels {
        SharedViewModelFactory(
            equipoRepository = EquipoRepositoryImpl(FirebaseFirestore.getInstance(), StatsCalculator),
            authRepository = AuthRepositoryImpl(FirebaseAuth.getInstance()),
            partidoRepository = PartidoRepositoryImpl(FirebaseFirestore.getInstance()),
            jugadorRepository = JugadorRepositoryImpl(FirebaseFirestore.getInstance()),
            competicionRepository = CompeticionRepositoryImpl(FirebaseFirestore.getInstance())
        )
    }

    private lateinit var adapter: CompeticionAdapter

    private var competicionPhotoUri: Uri? = null
    private var currentDialog: AlertDialog? = null

    private val pickImageLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == RESULT_OK) {
            result.data?.data?.let { uri ->
                competicionPhotoUri = uri
                updateCurrentDialogWithImage(uri)
            }
        }
    }

    companion object {
        private const val REQUEST_READ_STORAGE_PERMISSION = 1001
        private const val REQUEST_MEDIA_IMAGES_PERMISSION = 1002
        private const val REQUEST_SELECTED_PHOTOS_ACCESS = 1003
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCompeticionesBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupToolbar()
        setupRecyclerView()
        setupObservers()
        setupFab()
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        fun handlePermissionResult(granted: Boolean, permission: String) {
            if (granted) {
                showImagePicker()
            } else {
                if (ActivityCompat.shouldShowRequestPermissionRationale(this, permission)) {
                    showPermissionRationaleDialog(permission)
                } else {
                    showPermissionSettingsDialog()
                }
            }
        }

        when (requestCode) {
            REQUEST_READ_STORAGE_PERMISSION -> {
                if (grantResults.isNotEmpty()) {
                    handlePermissionResult(
                        grantResults[0] == PackageManager.PERMISSION_GRANTED,
                        Manifest.permission.READ_EXTERNAL_STORAGE
                    )
                }
            }
            REQUEST_MEDIA_IMAGES_PERMISSION -> {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    if (grantResults.isNotEmpty()) {
                        handlePermissionResult(
                            grantResults[0] == PackageManager.PERMISSION_GRANTED,
                            Manifest.permission.READ_MEDIA_IMAGES
                        )
                    }
                }
            }
            REQUEST_SELECTED_PHOTOS_ACCESS -> {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
                    if (grantResults.isNotEmpty()) {
                        handlePermissionResult(
                            grantResults[0] == PackageManager.PERMISSION_GRANTED,
                            Manifest.permission.READ_MEDIA_VISUAL_USER_SELECTED
                        )
                    }
                }
            }
        }
    }

    private fun showPermissionRationaleDialog(permission: String) {
        val message = when (permission) {
            Manifest.permission.READ_MEDIA_VISUAL_USER_SELECTED ->
                "Para seleccionar imágenes de tu galería, necesitamos acceso a las fotos seleccionadas."
            Manifest.permission.READ_MEDIA_IMAGES ->
                "Para seleccionar imágenes de tu galería, necesitamos acceso a tus fotos."
            else ->
                "Para seleccionar imágenes de tu galería, necesitamos acceso al almacenamiento."
        }

        MaterialAlertDialogBuilder(this)
            .setTitle("Permiso requerido")
            .setMessage(message)
            .setPositiveButton("Entendido") { _, _ ->
                checkAndRequestPhotoPermissions()
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun showPermissionSettingsDialog() {
        MaterialAlertDialogBuilder(this)
            .setTitle("Permiso requerido")
            .setMessage("Has denegado el permiso permanentemente. Por favor, habilita el permiso manualmente en Configuración.")
            .setPositiveButton("Abrir Configuración") { _, _ ->
                openAppSettings()
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun openAppSettings() {
        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
            data = Uri.fromParts("package", packageName, null)
        }
        startActivity(intent)
    }

    private fun setupToolbar() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(false)
            setDisplayShowHomeEnabled(false)
            title = getString(R.string.competiciones_title)
        }
    }

    private fun setupRecyclerView() {
        adapter = CompeticionAdapter(
            emptyList(),
            onItemClick = { navigateToDetail(it) },
            onDeleteClick = { viewModel.eliminarCompeticion(it.id) }
        )

        binding.rvCompeticiones.apply {
            layoutManager = LinearLayoutManager(this@CompeticionesActivity)
            adapter = this@CompeticionesActivity.adapter
            setHasFixedSize(true)
        }
    }

    private fun setupObservers() {
        // Observador para la lista de competiciones
        viewModel.competiciones.observe(this) { competiciones ->
            competiciones?.let {
                adapter.updateList(it)
            }
        }

        // Observador para los estados de operación
        viewModel.operacionState.observe(this) { state ->
            when (state) {
                is CompeticionViewModel.OperacionState.Loading -> {
                    // Mostrar ProgressBar
                    binding.progressBar.visibility = View.VISIBLE
                }
                is CompeticionViewModel.OperacionState.Success -> {
                    // Ocultar ProgressBar
                    binding.progressBar.visibility = View.GONE
                    // Mostrar mensaje de éxito si es necesario
                    Snackbar.make(binding.root, state.mensaje, Snackbar.LENGTH_SHORT).show()
                }
                is CompeticionViewModel.OperacionState.Error -> {
                    // Ocultar ProgressBar
                    binding.progressBar.visibility = View.GONE
                    // Mostrar mensaje de error
                    Snackbar.make(binding.root, state.mensaje, Snackbar.LENGTH_LONG).show()
                }
                else -> {
                    // Ocultar ProgressBar por defecto
                    binding.progressBar.visibility = View.GONE
                }
            }
        }
    }

    private fun setupFab() {
        binding.fabAddCompeticion.setOnClickListener {
            showAddCompeticionDialog()
        }
    }

    private fun showAddCompeticionDialog() {
        val dialogBinding = DialogAddCompeticionBinding.inflate(layoutInflater)
        val ivCompeticionPhoto = dialogBinding.ivCompeticionPhoto
        val fabAddPhoto = dialogBinding.fabAddPhoto
        val autoCompleteTextView = dialogBinding.dialogTipo

        competicionPhotoUri = null
        ivCompeticionPhoto.setImageResource(R.drawable.ic_default_trophy)

        // Configurar el AutoCompleteTextView
        val tipos = TipoCompeticion.entries
        val arrayAdapter = ArrayAdapter(
            this,
            R.layout.item_dropdown,
            tipos.map { it.toDisplayName() }
        )

        autoCompleteTextView.setDropDownBackgroundResource(R.drawable.dropdown_background)

        autoCompleteTextView.setAdapter(arrayAdapter)

        var tipoSeleccionado: TipoCompeticion? = null

        autoCompleteTextView.setOnClickListener {
            autoCompleteTextView.showDropDown()
        }

        autoCompleteTextView.setOnItemClickListener { _, _, position, _ ->
            tipoSeleccionado = tipos[position]
            autoCompleteTextView.setText(tipos[position].toDisplayName(), false)
            dialogBinding.tilTipo.error = null
        }

        fabAddPhoto.setOnClickListener {
            checkAndRequestPhotoPermissions()
        }

        val dialog = MaterialAlertDialogBuilder(this)
            .setTitle("Nueva Competición")
            .setView(dialogBinding.root)
            .setPositiveButton("Guardar", null)
            .setNegativeButton("Cancelar", null)
            .create()

        currentDialog = dialog

        dialog.setOnShowListener {
            val textView = dialog.findViewById<TextView>(android.R.id.title)
            textView?.setTextColor(ContextCompat.getColor(this, R.color.Fondo))

            dialog.getButton(AlertDialog.BUTTON_POSITIVE)?.setTextColor(
                ContextCompat.getColor(this, R.color.botones_positivos))
            dialog.getButton(AlertDialog.BUTTON_NEGATIVE)?.setTextColor(
                ContextCompat.getColor(this, R.color.Fondo))

            dialog.getButton(AlertDialog.BUTTON_POSITIVE)?.setOnClickListener {
                val nombre = dialogBinding.dialogNombre.text.toString().trim()

                if (nombre.isNotEmpty() && tipoSeleccionado != null) {
                    try {
                        val imagenBase64 = if (competicionPhotoUri != null) {
                            convertImageToBase64(competicionPhotoUri!!)
                        } else {
                            ""
                        }

                        val nuevaCompeticion = Competicion(
                            nombre = nombre,
                            tipo = tipoSeleccionado,
                            imagenBase64 = imagenBase64.toString()
                        )

                        viewModel.crearCompeticion(nuevaCompeticion)
                        dialog.dismiss()
                    } catch (e: Exception) {
                        dialogBinding.tilTipo.error = "Error al guardar la competición"
                    }
                } else {
                    if (nombre.isEmpty()) {
                        dialogBinding.tilNombre.error = "Nombre requerido"
                    }
                    if (tipoSeleccionado == null) {
                        dialogBinding.tilTipo.error = "Tipo requerido"
                    }
                }
            }
        }

        dialogBinding.dialogNombre.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus) dialogBinding.tilNombre.error = null
        }

        dialogBinding.dialogTipo.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus) dialogBinding.tilTipo.error = null
        }

        dialog.show()
    }

    private fun checkAndRequestPhotoPermissions() {
        when {
            // Android 14+ (API 34+)
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE -> {
                if (ContextCompat.checkSelfPermission(
                        this,
                        Manifest.permission.READ_MEDIA_VISUAL_USER_SELECTED
                    ) == PackageManager.PERMISSION_GRANTED ||
                    ContextCompat.checkSelfPermission(
                        this,
                        Manifest.permission.READ_MEDIA_IMAGES
                    ) == PackageManager.PERMISSION_GRANTED
                ) {
                    showImagePicker()
                } else {
                    ActivityCompat.requestPermissions(
                        this,
                        arrayOf(Manifest.permission.READ_MEDIA_VISUAL_USER_SELECTED),
                        REQUEST_SELECTED_PHOTOS_ACCESS
                    )
                }
            }
            // Android 13 (API 33)
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU -> {
                if (ContextCompat.checkSelfPermission(
                        this,
                        Manifest.permission.READ_MEDIA_IMAGES
                    ) == PackageManager.PERMISSION_GRANTED
                ) {
                    showImagePicker()
                } else {
                    ActivityCompat.requestPermissions(
                        this,
                        arrayOf(Manifest.permission.READ_MEDIA_IMAGES),
                        REQUEST_MEDIA_IMAGES_PERMISSION
                    )
                }
            }
            // Android 10-12 (API 29-32)
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q -> {
                if (ContextCompat.checkSelfPermission(
                        this,
                        Manifest.permission.READ_EXTERNAL_STORAGE
                    ) == PackageManager.PERMISSION_GRANTED
                ) {
                    showImagePicker()
                } else {
                    ActivityCompat.requestPermissions(
                        this,
                        arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),
                        REQUEST_READ_STORAGE_PERMISSION
                    )
                }
            }
            // Android < 10 (API < 29)
            else -> {
                showImagePicker()
            }
        }
    }

    private fun showImagePicker() {
        val intent = Intent(Intent.ACTION_GET_CONTENT).apply {
            type = "image/*"
        }
        pickImageLauncher.launch(intent)
    }

    private fun updateCurrentDialogWithImage(uri: Uri) {
        currentDialog?.findViewById<ImageView>(R.id.ivCompeticionPhoto)?.let { imageView ->
            try {
                val inputStream = contentResolver.openInputStream(uri)
                val bitmap = BitmapFactory.decodeStream(inputStream)
                inputStream?.close()
                imageView.setImageBitmap(bitmap)
            } catch (e: Exception) {
                e.printStackTrace()
                imageView.setImageResource(R.drawable.ic_default_trophy)
            }
        }
    }

    private fun convertImageToBase64(uri: Uri): String? {
        return try {
            val inputStream = contentResolver.openInputStream(uri)
            val bytes = inputStream?.readBytes()
            Base64.encodeToString(bytes, Base64.DEFAULT)
        } catch (e: Exception) {
            Snackbar.make(binding.root, "Error al procesar la imagen", Snackbar.LENGTH_SHORT).show()
            null
        }
    }

    private fun navigateToDetail(competicion: Competicion) {
        // Implementa la navegación a la actividad de detalle
        // val intent = Intent(this, CompeticionDetailActivity::class.java).apply {
        //     putExtra("COMPETICION_ID", competicion.id)
        // }
        // startActivity(intent)
    }
}