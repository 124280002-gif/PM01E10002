package com.example.pm01e10002

import com.example.pm01e10002.R
import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.widget.Button
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

class CallActivity : AppCompatActivity() {

    private val REQUEST_CALL_PERMISSION = 102
    private lateinit var txtNumeroLlamar: TextView
    private lateinit var btnLlamar: ImageButton
    private lateinit var btnAtras: Button
    private var numeroTelefono: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_call)

        txtNumeroLlamar = findViewById(R.id.txtNumeroLlamar)
        btnLlamar = findViewById(R.id.btnLlamar)
        btnAtras = findViewById(R.id.btnAtras)

        if (intent.hasExtra("telefono")) {
            numeroTelefono = intent.getStringExtra("telefono") ?: ""
            txtNumeroLlamar.text = numeroTelefono
        }

        btnAtras.setOnClickListener { finish() }

        btnLlamar.setOnClickListener { efectuarLlamada() }
    }

    private fun efectuarLlamada() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.CALL_PHONE), REQUEST_CALL_PERMISSION)
        } else {
            val callIntent = Intent(Intent.ACTION_CALL)
            callIntent.data = Uri.parse("tel:$numeroTelefono")
            startActivity(callIntent)
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_CALL_PERMISSION) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                efectuarLlamada()
            } else {
                Toast.makeText(this, "Permisos de llamada denegados", Toast.LENGTH_SHORT).show()
            }
        }
    }
}