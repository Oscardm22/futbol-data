package com.example.futboldata.ui.equipos

import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.futboldata.R
import com.example.futboldata.adapter.EquiposAdapter
import com.example.futboldata.databinding.ActivityEquiposBinding
import com.example.futboldata.ui.auth.LoginActivity
import com.example.futboldata.viewmodel.EquipoViewModel
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.futboldata.FutbolDataApp
import com.example.futboldata.data.model.Equipo
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import java.util.*
import android.Manifest
import android.os.Build
import android.text.Spannable
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import android.util.Base64
import com.example.futboldata.ui.competiciones.CompeticionesActivity
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlin.jvm.java
import androidx.core.view.size
import androidx.core.view.get

class EquiposActivity : AppCompatActivity() {

    private lateinit var binding: ActivityEquiposBinding
    private lateinit var auth: FirebaseAuth
    private val viewModel: EquipoViewModel by viewModels {
        (application as FutbolDataApp).viewModelFactory
    }
    private var teamPhotoUri: Uri? = null
    private var currentDialog: AlertDialog? = null
    private val pickImageLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == RESULT_OK) {
            result.data?.data?.let { uri ->
                teamPhotoUri = uri
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
        binding = ActivityEquiposBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)
        auth = Firebase.auth

        setupRecyclerView()
        setupObservers()
        setupFAB()
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
        auth.signOut()
        startActivity(Intent(this, LoginActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        })
        finish()
    }

    private fun setupRecyclerView() {
        binding.rvEquipos.apply {
            layoutManager = LinearLayoutManager(this@EquiposActivity)
            adapter = EquiposAdapter(emptyList(),
                { equipoId -> abrirDetalleEquipo(equipoId) },
                { equipoId -> mostrarDialogoEliminacion(equipoId) }
            )
        }
    }

    private fun setupObservers() {
        viewModel.equiposState.observe(this) { state ->
            when (state) {
                is EquipoViewModel.EquipoState.Loading -> {
                    binding.progressBar.visibility = View.VISIBLE
                }
                is EquipoViewModel.EquipoState.Success -> {
                    binding.progressBar.visibility = View.GONE
                    (binding.rvEquipos.adapter as EquiposAdapter).updateList(state.equipos)
                }
                is EquipoViewModel.EquipoState.Error -> {
                    binding.progressBar.visibility = View.GONE
                    Toast.makeText(this, state.mensaje, Toast.LENGTH_SHORT).show()
                }
            }
        }

        viewModel.operacionState.observe(this) { state ->
            when (state) {
                is EquipoViewModel.OperacionState.Loading -> {
                    binding.progressBar.visibility = View.VISIBLE
                }
                is EquipoViewModel.OperacionState.Success -> {
                    binding.progressBar.visibility = View.GONE
                    Toast.makeText(this, state.mensaje, Toast.LENGTH_SHORT).show()
                }
                is EquipoViewModel.OperacionState.Error -> {
                    binding.progressBar.visibility = View.GONE
                    Toast.makeText(this, state.mensaje, Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun setupFAB() {
        binding.fabAddEquipo.setOnClickListener {
            abrirDialogoCreacionEquipo()
        }
    }

    private fun abrirDialogoCreacionEquipo() {
        val dialogView = layoutInflater.inflate(R.layout.dialog_nuevo_equipo, null)
        val textInputLayout = dialogView.findViewById<TextInputLayout>(R.id.tilNombreEquipo)
        val editText = dialogView.findViewById<TextInputEditText>(R.id.etNombreEquipo)
        val ivTeamPhoto = dialogView.findViewById<ImageView>(R.id.ivTeamPhoto)
        val fabAddPhoto = dialogView.findViewById<FloatingActionButton>(R.id.fabAddPhoto)

        teamPhotoUri = null
        ivTeamPhoto.setImageResource(R.drawable.ic_default_team_placeholder)

        fabAddPhoto.setOnClickListener {
            checkAndRequestPhotoPermissions()
        }

        val dialog = MaterialAlertDialogBuilder(this)
            .setTitle("Nuevo Equipo")
            .setView(dialogView)
            .setPositiveButton("Guardar", null)
            .setNegativeButton("Cancelar", null)
            .create()

        currentDialog = dialog

        dialog.setOnShowListener {
            // Personalizar color del título
            val textView = dialog.findViewById<TextView>(android.R.id.title)
            textView?.setTextColor(ContextCompat.getColor(this, R.color.Fondo))

            // Personalizar botones
            dialog.getButton(AlertDialog.BUTTON_POSITIVE)?.setTextColor(
                ContextCompat.getColor(this, R.color.botones_positivos))

            dialog.getButton(AlertDialog.BUTTON_NEGATIVE)?.setTextColor(
                ContextCompat.getColor(this, R.color.Fondo))

            dialog.getButton(AlertDialog.BUTTON_POSITIVE)?.setOnClickListener {
                val nombre = editText.text.toString().trim()
                val (esValido, mensajeError) = validarNombreEquipo(nombre)

                if (esValido) {
                    val imagenBase64 = if (teamPhotoUri != null) {
                        convertImageToBase64(teamPhotoUri!!)
                    } else {
                        ""
                    }

                    val nuevoEquipo = Equipo(
                        nombre = nombre,
                        fechaCreacion = Date(),
                        imagenBase64 = imagenBase64 ?: ""
                    )

                    viewModel.guardarEquipo(nuevoEquipo)
                    dialog.dismiss()
                } else {
                    textInputLayout.error = mensajeError
                }
            }
        }

        editText.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus) textInputLayout.error = null
        }

        dialog.show()
    }

    private fun convertImageToBase64(uri: Uri): String? {
        return try {
            val inputStream = contentResolver.openInputStream(uri)
            val bytes = inputStream?.readBytes()
            Base64.encodeToString(bytes, Base64.DEFAULT)
        } catch (e: Exception) {
            Toast.makeText(this, "Error al procesar la imagen: ${e.message}", Toast.LENGTH_SHORT).show()
            null
        }
    }

    private fun checkAndRequestPhotoPermissions() {
        when {
            // Android 14+ (API 34+)
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE -> {
                if (ContextCompat.checkSelfPermission(
                        this,
                        Manifest.permission.READ_MEDIA_VISUAL_USER_SELECTED
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
            // Android 10 a 12 (API 29-32)
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

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            REQUEST_READ_STORAGE_PERMISSION,
            REQUEST_MEDIA_IMAGES_PERMISSION,
            REQUEST_SELECTED_PHOTOS_ACCESS -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    showImagePicker()
                } else {
                    Toast.makeText(
                        this,
                        "Permiso denegado. No puedes seleccionar imágenes sin este permiso.",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }
    }

    private fun updateCurrentDialogWithImage(uri: Uri) {
        currentDialog?.findViewById<ImageView>(R.id.ivTeamPhoto)?.let { imageView ->
            try {
                // First decode with inJustDecodeBounds=true to check dimensions
                val inputStream = contentResolver.openInputStream(uri)
                val options = BitmapFactory.Options().apply {
                    inJustDecodeBounds = true
                }
                BitmapFactory.decodeStream(inputStream, null, options)
                inputStream?.close()

                // Calculate inSampleSize
                options.inSampleSize = calculateInSampleSize(options, 400, 400)

                // Decode bitmap with inSampleSize set
                options.inJustDecodeBounds = false
                val newInputStream = contentResolver.openInputStream(uri)
                val bitmap = BitmapFactory.decodeStream(newInputStream, null, options)
                newInputStream?.close()

                imageView.setImageBitmap(bitmap)
            } catch (e: Exception) {
                e.printStackTrace()
                imageView.setImageResource(R.drawable.ic_default_team_placeholder)
            }
        }
    }

    private fun calculateInSampleSize(options: BitmapFactory.Options, reqWidth: Int, reqHeight: Int): Int {
        // Raw height and width of image
        val height = options.outHeight
        val width = options.outWidth
        var inSampleSize = 1

        if (height > reqHeight || width > reqWidth) {
            val halfHeight = height / 2
            val halfWidth = width / 2

            // Calculate the largest inSampleSize value that is a power of 2 and keeps both
            // height and width larger than the requested height and width.
            while (halfHeight / inSampleSize >= reqHeight && halfWidth / inSampleSize >= reqWidth) {
                inSampleSize *= 2
            }
        }

        return inSampleSize
    }

    private fun validarNombreEquipo(nombre: String): Pair<Boolean, String> {
        return when {
            nombre.isEmpty() -> false to "El nombre no puede estar vacío"
            nombre.length < 3 -> false to "El nombre debe tener al menos 3 caracteres"
            nombre.length > 30 -> false to "El nombre no puede exceder 30 caracteres"
            else -> true to ""
        }
    }

    private fun abrirDetalleEquipo(equipoId: String) {
        val intent = Intent(this, EquipoDetailActivity::class.java).apply {
            putExtra("equipo_id", equipoId)
        }
        startActivity(intent)
    }

    private fun mostrarDialogoEliminacion(equipoId: String) {
        val dialog = MaterialAlertDialogBuilder(this)
            .setTitle("Eliminar equipo")
            .setMessage("¿Seguro que quieres eliminar este equipo? Se eliminará también su foto.")
            .setPositiveButton("Eliminar") { _, _ ->
                viewModel.eliminarEquipo(equipoId)
                deleteTeamPhotoFromStorage(equipoId)
            }
            .setNegativeButton("Cancelar", null)
            .create()

        dialog.setOnShowListener {
            dialog.getButton(AlertDialog.BUTTON_POSITIVE)?.setTextColor(
                ContextCompat.getColor(this, R.color.error_color))

            dialog.getButton(AlertDialog.BUTTON_NEGATIVE)?.setTextColor(
                ContextCompat.getColor(this, R.color.Fondo))
        }

        dialog.show()
    }

    private fun deleteTeamPhotoFromStorage(equipoId: String) {
        val storageRef = Firebase.storage.reference
        val teamPhotoRef = storageRef.child("team_photos/$equipoId.jpg")

        teamPhotoRef.delete()
            .addOnFailureListener {
                // No es crítico si falla, podemos ignorar el error
            }
    }
}