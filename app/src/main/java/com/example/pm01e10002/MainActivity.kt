package com.example.pm01e10002

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.provider.MediaStore
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import java.io.ByteArrayOutputStream

class MainActivity : AppCompatActivity() {

    private val REQUEST_IMAGE_CAPTURE = 101
    private lateinit var imgFoto: ImageView
    private lateinit var spPais: Spinner
    private lateinit var txtNombre: EditText
    private lateinit var txtTelefono: EditText
    private lateinit var txtNota: EditText
    private lateinit var btnSalvar: Button
    private lateinit var btnContactosSalvados: Button

    private var imagenBytes: ByteArray? = null
    private var idContactoEditar = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        spPais = findViewById(R.id.spPais)
        txtNombre = findViewById(R.id.txtNombre)
        txtTelefono = findViewById(R.id.txtTelefono)
        txtNota = findViewById(R.id.txtNota)
        imgFoto = findViewById(R.id.imgFoto)
        btnSalvar = findViewById(R.id.btnSalvar)
        btnContactosSalvados = findViewById(R.id.btnContactosSalvados)

        val paises = arrayOf("Honduras (504)", "Costa Rica (506)", "Guatemala (502)", "El Salvador (503)")
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, paises)
        spPais.adapter = adapter

        imgFoto.setOnClickListener { abrirCamara() }
        btnSalvar.setOnClickListener { guardarContacto() }
        btnContactosSalvados.setOnClickListener {
            val intent = Intent(this, ListActivity::class.java)
            startActivity(intent)
        }

        if (intent.hasExtra("contacto")) {
            val c = intent.getSerializableExtra("contacto") as? Contacto
            c?.let {
                idContactoEditar = it.getId()
                txtNombre.setText(it.getNombre())
                txtTelefono.setText(it.getTelefono())
                txtNota.setText(it.getNota())
                imagenBytes = it.getImagen()
                if (imagenBytes != null && imagenBytes!!.isNotEmpty()) {
                    val bmp = BitmapFactory.decodeByteArray(imagenBytes, 0, imagenBytes!!.size)
                    imgFoto.setImageBitmap(bmp)
                }
                btnSalvar.text = "Actualizar Contacto"
            }
        }
    }

    private fun abrirCamara() {
        val takePictureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        try {
            startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE)
        } catch (e: Exception) {
            Toast.makeText(this, "No se pudo abrir la cámara: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK && data != null) {
            val extras = data.extras
            val imageBitmap = extras?.get("data") as? Bitmap
            imageBitmap?.let {
                imgFoto.setImageBitmap(it)

                val stream = ByteArrayOutputStream()
                it.compress(Bitmap.CompressFormat.PNG, 100, stream)
                imagenBytes = stream.toByteArray()
            }
        }
    }

    private fun guardarContacto() {
        val nombre = txtNombre.text.toString().trim()
        val telefono = txtTelefono.text.toString().trim()
        val nota = txtNota.text.toString().trim()
        val pais = spPais.selectedItem.toString()

        if (nombre.isEmpty()) {
            mostrarAlerta("Alerta", "debe escribir un nombre")
            return
        }

        if (!nombre.matches(Regex("^[a-zA-ZáéíóúÁÉÍÓÚñÑ\\s]+$"))) {
            mostrarAlerta("Alerta", "El nombre solo debe contener letras")
            return
        }

        if (telefono.isEmpty()) {
            mostrarAlerta("Alerta", "debe escribir un telefono")
            return
        }

        if (!telefono.matches(Regex("^[0-9]+$"))) {
            mostrarAlerta("Alerta", "El teléfono solo debe contener números")
            return
        }

        if (nota.isEmpty()) {
            mostrarAlerta("Alerta", "debe escribir una nota")
            return
        }

        val db = SQLiteConexion(this)
        val c = Contacto(idContactoEditar, pais, nombre, telefono, nota, imagenBytes)

        if (idContactoEditar == -1) {
            val res = db.insertarContacto(c)
            if (res > 0) {
                Toast.makeText(this, "Contacto guardado exitosamente", Toast.LENGTH_SHORT).show()
                limpiarCampos()
            }
        } else {
            val res = db.actualizarContacto(c)
            if (res > 0) {
                Toast.makeText(this, "Contacto actualizado exitosamente", Toast.LENGTH_SHORT).show()
                finish()
            }
        }
    }

    private fun mostrarAlerta(titulo: String, mensaje: String) {
        AlertDialog.Builder(this)
            .setTitle(titulo)
            .setMessage(mensaje)
            .setPositiveButton("OK", null)
            .show()
    }

    private fun limpiarCampos() {
        txtNombre.setText("")
        txtTelefono.setText("")
        txtNota.setText("")
        imgFoto.setImageResource(R.drawable.ic_launcher_background)
        imagenBytes = null
        idContactoEditar = -1
        btnSalvar.text = "Guardar Contacto"
    }
}
