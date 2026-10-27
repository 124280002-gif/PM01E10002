package com.example.pm01e10002.ui.screens

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.BitmapFactory
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.example.pm01e10002.db.SQLiteConexion
import com.example.pm01e10002.models.Contacto

@Composable
fun ContactListScreen(
    initialContactos: List<Contacto>? = null,
    onNavigateBack: () -> Unit,
    onEditContact: (Contacto) -> Unit
) {
    val context = LocalContext.current
    val isPreview = LocalInspectionMode.current || initialContactos != null
    val dbHelper = remember(context, isPreview) { if (!isPreview) SQLiteConexion(context) else null }

    var searchQuery by remember { mutableStateOf("") }

    val sampleContactos = remember(initialContactos) {
        initialContactos ?: listOf(
            Contacto(1, "Guatemala (502)", "Jose Escalante", "39976543", "Conocido del trabajo"),
            Contacto(2, "Honduras (504)", "Jose de Jesus Sarmiento", "99765645", "Conocido desde la secundaria"),
            Contacto(3, "Costa Rica (506)", "Maria Lopez", "33456711", "Amiga de la universidad")
        )
    }

    var contactos by remember {
        mutableStateOf(
            if (isPreview) sampleContactos else dbHelper?.obtenerContactos() ?: emptyList()
        )
    }
    var contactoSeleccionado by remember { mutableStateOf<Contacto?>(null) }

    var contactoParaLlamar by remember { mutableStateOf<Contacto?>(null) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var showImageDialog by remember { mutableStateOf(false) }

    fun refreshList() {
        if (!isPreview) {
            contactos = dbHelper?.obtenerContactos(searchQuery) ?: emptyList()
        } else {
            contactos = sampleContactos.filter {
                it.nombre.contains(searchQuery, ignoreCase = true) ||
                        it.telefono.contains(searchQuery)
            }
        }
    }

    val callPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted && contactoParaLlamar != null) {
            hacerLlamada(context, contactoParaLlamar!!)
            contactoParaLlamar = null
        } else {
            Toast.makeText(context, "Permiso de llamada denegado", Toast.LENGTH_SHORT).show()
        }
    }

    fun solicitarLlamada(contacto: Contacto) {
        if (isPreview) return
        val permissionState = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.CALL_PHONE
        )
        if (permissionState == PackageManager.PERMISSION_GRANTED) {
            hacerLlamada(context, contacto)
        } else {
            contactoParaLlamar = contacto
            callPermissionLauncher.launch(Manifest.permission.CALL_PHONE)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Top Navigation: Atras
        Button(
            onClick = onNavigateBack,
            colors = ButtonDefaults.buttonColors(containerColor = Color.LightGray),
            shape = RoundedCornerShape(4.dp),
            modifier = Modifier.border(1.dp, Color.Black, RoundedCornerShape(4.dp))
        ) {
            Text(text = "Atras", color = Color.Black, fontWeight = FontWeight.Bold)
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Search Bar: Buscar
        OutlinedTextField(
            value = searchQuery,
            onValueChange = {
                searchQuery = it
                refreshList()
            },
            placeholder = { Text("Buscar...") },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            singleLine = true,
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Color.Black,
                unfocusedBorderColor = Color.Black
            )
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Lista de Contactos
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .border(2.dp, Color.Black)
        ) {
            if (contactos.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = if (searchQuery.isBlank()) "No hay contactos salvados" else "No se encontraron contactos",
                        fontSize = 16.sp,
                        color = Color.Gray
                    )
                }
            } else {
                LazyColumn(modifier = Modifier.fillMaxSize()) {
                    itemsIndexed(contactos) { index, contacto ->
                        val isSelected = contactoSeleccionado?.id == contacto.id
                        val rowBgColor = when {
                            isSelected -> Color(0xFF1976D2)
                            index % 2 == 0 -> Color(0xFFE3F2FD)
                            else -> Color.White
                        }
                        val textColor = if (isSelected) Color.White else Color.Black

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(rowBgColor)
                                .clickable {
                                    contactoSeleccionado = contacto
                                    contactoParaLlamar = contacto
                                }
                                .padding(horizontal = 12.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            val bitmap = remember(contacto.foto) {
                                contacto.foto?.let {
                                    try {
                                        BitmapFactory.decodeByteArray(it, 0, it.size)?.asImageBitmap()
                                    } catch (_: Exception) {
                                        null
                                    }
                                }
                            }

                            if (bitmap != null) {
                                Image(
                                    bitmap = bitmap,
                                    contentDescription = "Foto",
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clip(CircleShape)
                                        .border(1.dp, Color.Black, CircleShape),
                                    contentScale = ContentScale.Crop
                                )
                            } else {
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clip(CircleShape)
                                        .background(Color.White)
                                        .border(1.dp, Color.Black, CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    SmileyAvatar(modifier = Modifier.size(28.dp))
                                }
                            }

                            Spacer(modifier = Modifier.width(12.dp))

                            Text(
                                text = "${contacto.nombre} | ${extraerCodigo(contacto.pais)}${contacto.telefono}",
                                color = textColor,
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp,
                                modifier = Modifier.weight(1f)
                            )
                        }
                        HorizontalDivider(color = Color.LightGray, thickness = 0.5.dp)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Bottom 4 Action Buttons
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Compartir Contacto
                Button(
                    onClick = {
                        contactoSeleccionado?.let { compartirContacto(context, it) }
                            ?: Toast.makeText(context, "Seleccione un contacto", Toast.LENGTH_SHORT).show()
                    },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(containerColor = Color.LightGray),
                    shape = RoundedCornerShape(4.dp),
                    border = BorderStroke(1.dp, Color.Black)
                ) {
                    Text("Compartir Contacto", color = Color.Black, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }

                // Ver Imagen
                Button(
                    onClick = {
                        if (contactoSeleccionado != null) {
                            showImageDialog = true
                        } else {
                            Toast.makeText(context, "Seleccione un contacto", Toast.LENGTH_SHORT).show()
                        }
                    },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(containerColor = Color.LightGray),
                    shape = RoundedCornerShape(4.dp),
                    border = BorderStroke(1.dp, Color.Black)
                ) {
                    Text("Ver Imagen", color = Color.Black, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Eliminar Contacto
                Button(
                    onClick = {
                        if (contactoSeleccionado != null) {
                            showDeleteDialog = true
                        } else {
                            Toast.makeText(context, "Seleccione un contacto", Toast.LENGTH_SHORT).show()
                        }
                    },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(containerColor = Color.LightGray),
                    shape = RoundedCornerShape(4.dp),
                    border = BorderStroke(1.dp, Color.Black)
                ) {
                    Text("Eliminar Contacto", color = Color.Black, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }

                // Actualizar Contacto
                Button(
                    onClick = {
                        contactoSeleccionado?.let { onEditContact(it) }
                            ?: Toast.makeText(context, "Seleccione un contacto", Toast.LENGTH_SHORT).show()
                    },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(containerColor = Color.LightGray),
                    shape = RoundedCornerShape(4.dp),
                    border = BorderStroke(1.dp, Color.Black)
                ) {
                    Text("Actualizar Contacto", color = Color.Black, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }

    // Call Action Dialog matching wireframe ("Acción: Desea llamar a [Nombre]")
    if (contactoParaLlamar != null) {
        AlertDialog(
            onDismissRequest = { contactoParaLlamar = null },
            title = { Text(text = "Acción", fontWeight = FontWeight.Bold) },
            text = { Text(text = "Desea llamar a ${contactoParaLlamar?.nombre}?") },
            confirmButton = {
                TextButton(onClick = {
                    val contacto = contactoParaLlamar
                    contactoParaLlamar = null
                    if (contacto != null) {
                        solicitarLlamada(contacto)
                    }
                }) {
                    Text("Yes", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { contactoParaLlamar = null }) {
                    Text("No", fontWeight = FontWeight.Bold)
                }
            }
        )
    }

    // Delete Confirmation Dialog
    if (showDeleteDialog && contactoSeleccionado != null) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Eliminar Contacto") },
            text = { Text("¿Está seguro de eliminar a ${contactoSeleccionado?.nombre}?") },
            confirmButton = {
                TextButton(onClick = {
                    showDeleteDialog = false
                    val rows = dbHelper?.eliminarContacto(contactoSeleccionado!!.id) ?: 0
                    if (rows > 0) {
                        Toast.makeText(context, "Contacto eliminado", Toast.LENGTH_SHORT).show()
                        contactoSeleccionado = null
                        refreshList()
                    } else {
                        Toast.makeText(context, "Error al eliminar", Toast.LENGTH_SHORT).show()
                    }
                }) {
                    Text("Sí", color = Color.Red, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text("Cancelar")
                }
            }
        )
    }

    // View Image Dialog
    if (showImageDialog && contactoSeleccionado != null) {
        val bitmap: ImageBitmap? = remember(contactoSeleccionado?.foto) {
            contactoSeleccionado?.foto?.let {
                try {
                    BitmapFactory.decodeByteArray(it, 0, it.size)?.asImageBitmap()
                } catch (_: Exception) {
                    null
                }
            }
        }

        AlertDialog(
            onDismissRequest = { showImageDialog = false },
            title = { Text(contactoSeleccionado?.nombre ?: "Imagen de Contacto") },
            text = {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(250.dp),
                    contentAlignment = Alignment.Center
                ) {
                    if (bitmap != null) {
                        Image(
                            bitmap = bitmap,
                            contentDescription = "Imagen de contacto",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Fit
                        )
                    } else {
                        SmileyAvatar(modifier = Modifier.size(180.dp))
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showImageDialog = false }) {
                    Text("Cerrar")
                }
            }
        )
    }
}

private fun extraerCodigo(paisStr: String): String {
    val regex = "\\((\\d+)\\)".toRegex()
    val match = regex.find(paisStr)
    return if (match != null) match.groupValues[1] else ""
}

private fun hacerLlamada(context: Context, contacto: Contacto) {
    val codigo = extraerCodigo(contacto.pais)
    val numLimpio = "$codigo${contacto.telefono}".replace(" ", "").replace("-", "")
    try {
        val intent = Intent(Intent.ACTION_CALL).apply {
            data = Uri.parse("tel:+$numLimpio")
        }
        context.startActivity(intent)
    } catch (_: Exception) {
        val dialIntent = Intent(Intent.ACTION_DIAL).apply {
            data = Uri.parse("tel:+$numLimpio")
        }
        context.startActivity(dialIntent)
    }
}

private fun compartirContacto(context: Context, contacto: Contacto) {
    val sendIntent = Intent().apply {
        action = Intent.ACTION_SEND
        putExtra(
            Intent.EXTRA_TEXT,
            "Contacto: ${contacto.nombre}\nPaís: ${contacto.pais}\nTeléfono: +${extraerCodigo(contacto.pais)}${contacto.telefono}\nNota: ${contacto.nota}"
        )
        type = "text/plain"
    }
    val shareIntent = Intent.createChooser(sendIntent, "Compartir Contacto")
    context.startActivity(shareIntent)
}
