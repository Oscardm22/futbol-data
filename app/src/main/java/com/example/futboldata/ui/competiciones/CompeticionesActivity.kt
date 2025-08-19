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
            partidoRepository = PartidoRepositoryImpl(db = FirebaseFirestore.getInstance(), jugadorRepository = JugadorRepositoryImpl(FirebaseFirestore.getInstance())),
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
            onItemClick = { showEditCompeticionDialog(it) },
            onDeleteClick = { competicion ->
                showDeleteConfirmationDialog(competicion)
            }        )

        binding.rvCompeticiones.apply {
            layoutManager = LinearLayoutManager(this@CompeticionesActivity)
            adapter = this@CompeticionesActivity.adapter
            setHasFixedSize(true)
        }
    }

    private fun showEditCompeticionDialog(competicion: Competicion) {
        val dialogBinding = DialogAddCompeticionBinding.inflate(layoutInflater)

        // Prellenar datos existentes
        dialogBinding.dialogNombre.setText(competicion.nombre)
        dialogBinding.dialogTipo.setText(competicion.tipo.toDisplayName())

        // Configurar el AutoCompleteTextView para que muestre el desplegable al tocar
        dialogBinding.dialogTipo.setOnClickListener {
            dialogBinding.dialogTipo.showDropDown()
        }

        // Cargar imagen si existe
        if (competicion.imagenBase64.isNotEmpty()) {
            try {
                val decodedBytes = Base64.decode(competicion.imagenBase64, Base64.DEFAULT)
                val bitmap = BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.size)
                dialogBinding.ivCompeticionPhoto.setImageBitmap(bitmap)
            } catch (e: Exception) {
                dialogBinding.ivCompeticionPhoto.setImageResource(R.drawable.ic_default_trophy)
            }
        }

        val dialog = MaterialAlertDialogBuilder(this)
            .setTitle("Editar competición")
            .setView(dialogBinding.root)
            .setPositiveButton("Guardar", null)
            .setNegativeButton("Cancelar", null)
            .create()

        currentDialog = dialog

        dialog.setOnShowListener {
            // Configura colores de botones
            dialog.getButton(AlertDialog.BUTTON_POSITIVE)?.setTextColor(
                ContextCompat.getColor(this, R.color.botones_positivos)
            )
            dialog.getButton(AlertDialog.BUTTON_NEGATIVE)?.setTextColor(
                ContextCompat.getColor(this, R.color.Fondo)
            )

            // Configuración del AutoCompleteTextView
            val tipos = TipoCompeticion.entries
            val arrayAdapter = ArrayAdapter(
                this,
                R.layout.item_dropdown,
                tipos.map { it.toDisplayName() }
            )
            dialogBinding.dialogTipo.setAdapter(arrayAdapter)

            var tipoSeleccionado: TipoCompeticion? = competicion.tipo

            dialogBinding.dialogTipo.setOnItemClickListener { _, _, position, _ ->
                tipoSeleccionado = tipos[position]
                dialogBinding.tilTipo.error = null
            }

            // Configuración del botón Guardar
            dialog.getButton(AlertDialog.BUTTON_POSITIVE)?.setOnClickListener {
                val nombre = dialogBinding.dialogNombre.text.toString().trim()

                // Validaciones
                when {
                    nombre.isEmpty() -> {
                        dialogBinding.tilNombre.error = getString(R.string.error_empty_name)
                        return@setOnClickListener
                    }
                    nombre.length < 3 -> {
                        dialogBinding.tilNombre.error = getString(R.string.error_name_too_short)
                        return@setOnClickListener
                    }
                    nombre.length > 50 -> {
                        dialogBinding.tilNombre.error = getString(R.string.error_name_too_long)
                        return@setOnClickListener
                    }
                    !nombre.matches(Regex("^[a-zA-Z0-9 áéíóúÁÉÍÓÚñÑ-]+$")) -> {
                        dialogBinding.tilNombre.error = getString(R.string.error_invalid_chars)
                        return@setOnClickListener
                    }
                }

                val competicionActualizada = competicion.copy(
                    nombre = nombre,
                    tipo = tipoSeleccionado ?: competicion.tipo,
                    imagenBase64 = competicionPhotoUri?.let { convertImageToBase64(it) } ?: competicion.imagenBase64
                )

                viewModel.actualizarCompeticion(competicionActualizada)
                dialog.dismiss()
            }
        }

        dialog.show()
    }

    private fun showDeleteConfirmationDialog(competicion: Competicion) {
        val dialog = MaterialAlertDialogBuilder(this)
            .setTitle(getString(R.string.delete_dialog_title))
            .setMessage(getString(R.string.delete_dialog_message, competicion.nombre))
            .setPositiveButton(getString(R.string.delete_button)) { _, _ ->
                viewModel.eliminarCompeticion(competicion.id)
            }
            .setNegativeButton(getString(R.string.cancel_button), null)
            .create()

        dialog.setOnShowListener {
            dialog.getButton(AlertDialog.BUTTON_POSITIVE)?.setTextColor(
                ContextCompat.getColor(this, R.color.error_color)
            )

            dialog.getButton(AlertDialog.BUTTON_NEGATIVE)?.setTextColor(
                ContextCompat.getColor(this, R.color.Fondo)
            )
        }

        dialog.show()
    }

    private fun setupObservers() {
        // Observador para la lista de competiciones
        viewModel.competiciones.observe(this) { competiciones ->
            competiciones?.let {
                adapter.updateList(it)
                binding.progressBar.visibility = View.GONE
            }
        }

        // Observador para estados de operación (sin Snackbar)
        viewModel.operacionState.observe(this) { state ->
            when (state) {
                is CompeticionViewModel.OperacionState.Loading -> {
                    binding.progressBar.visibility = View.VISIBLE
                }
                is CompeticionViewModel.OperacionState.Error -> {
                    binding.progressBar.visibility = View.GONE
                    // Sin Snackbar (solo oculta ProgressBar en error)
                }
                is CompeticionViewModel.OperacionState.Success -> {
                    binding.progressBar.visibility = View.GONE
                    // Sin Snackbar
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

                // Validaciones de nombre
                when {
                    nombre.isEmpty() -> {
                        dialogBinding.tilNombre.error = getString(R.string.error_empty_name)
                        return@setOnClickListener
                    }
                    nombre.length < 3 -> {
                        dialogBinding.tilNombre.error = getString(R.string.error_name_too_short)
                        return@setOnClickListener
                    }
                    nombre.length > 50 -> {
                        dialogBinding.tilNombre.error = getString(R.string.error_name_too_long)
                        return@setOnClickListener
                    }
                    !nombre.matches(Regex("^[a-zA-Z0-9 áéíóúÁÉÍÓÚñÑ-]+$")) -> {
                        dialogBinding.tilNombre.error = getString(R.string.error_invalid_chars)
                        return@setOnClickListener
                    }
                }

                // Validación de tipo
                val tipoSeleccionado = tipoSeleccionado ?: run {
                    dialogBinding.tilTipo.error = getString(R.string.error_no_competition_type)
                    return@setOnClickListener
                }

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
                    Snackbar.make(dialogBinding.root, "Error al guardar: ${e.message}", Snackbar.LENGTH_SHORT).show()
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
}