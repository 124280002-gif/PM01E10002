package com.example.pm01e10002.ui.screens

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.pm01e10002.db.SQLiteConexion
import com.example.pm01e10002.models.Contacto
import com.example.pm01e10002.models.Pais
import java.io.ByteArrayOutputStream

val listaPaises = listOf(
    Pais("Honduras", "504"),
    Pais("Costa Rica", "506"),
    Pais("Guatemala", "502"),
    Pais("El Salvador", "503")
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddContactScreen(
    contactoEditar: Contacto? = null,
    onNavigateToSavedContacts: () -> Unit,
    onSavedSuccessfully: () -> Unit
) {
    val context = LocalContext.current
    val dbHelper = remember { SQLiteConexion(context) }

    var paisSeleccionado by remember {
        mutableStateOf(
            listaPaises.find { it.getDisplayName() == contactoEditar?.pais }
                ?: listaPaises.first()
        )
    }
    var dropdownExpanded by remember { mutableStateOf(false) }

    var nombre by remember { mutableStateOf(contactoEditar?.nombre ?: "") }
    var telefono by remember { mutableStateOf(contactoEditar?.telefono ?: "") }
    var nota by remember { mutableStateOf(contactoEditar?.nota ?: "") }
    var fotoBytes by remember { mutableStateOf<ByteArray?>(contactoEditar?.foto) }

    var alertMessage by remember { mutableStateOf<String?>(null) }
    var showImagePickerDialog by remember { mutableStateOf(false) }

    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            fotoBytes = uriToByteArray(context, it)
        }
    }

    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicturePreview()
    ) { bitmap: Bitmap? ->
        bitmap?.let {
            fotoBytes = bitmapToByteArray(it)
        }
    }

    val imageBitmap: ImageBitmap? = remember(fotoBytes) {
        fotoBytes?.let {
            try {
                BitmapFactory.decodeByteArray(it, 0, it.size)?.asImageBitmap()
            } catch (_: Exception) {
                null
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Top Photo Container
        Box(
            modifier = Modifier
                .width(200.dp)
                .height(200.dp),
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .border(2.dp, Color.Black, RoundedCornerShape(4.dp))
                    .background(Color.White, RoundedCornerShape(4.dp)),
                contentAlignment = Alignment.Center
            ) {
                if (imageBitmap != null) {
                    Image(
                        bitmap = imageBitmap,
                        contentDescription = "Foto del contacto",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    SmileyAvatar(modifier = Modifier.size(150.dp))
                }
            }

            // Plus (+) Button to pick image
            Box(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .offset(x = 12.dp, y = 12.dp)
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(Color.White)
                    .border(2.dp, Color.Black, CircleShape)
                    .clickable { showImagePickerDialog = true },
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "+",
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Campo: Pais
        Column(modifier = Modifier.fillMaxWidth()) {
            Text(
                text = "Pais",
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                color = Color.Black
            )
            Spacer(modifier = Modifier.height(4.dp))

            ExposedDropdownMenuBox(
                expanded = dropdownExpanded,
                onExpandedChange = { dropdownExpanded = !dropdownExpanded },
                modifier = Modifier.fillMaxWidth()
            ) {
                OutlinedTextField(
                    value = paisSeleccionado.getDisplayName(),
                    onValueChange = {},
                    readOnly = true,
                    trailingIcon = {
                        ExposedDropdownMenuDefaults.TrailingIcon(expanded = dropdownExpanded)
                    },
                    modifier = Modifier
                        .menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable)
                        .fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFF1976D2),
                        unfocusedBorderColor = Color.Gray
                    )
                )

                ExposedDropdownMenu(
                    expanded = dropdownExpanded,
                    onDismissRequest = { dropdownExpanded = false }
                ) {
                    listaPaises.forEach { pais ->
                        DropdownMenuItem(
                            text = { Text(pais.getDisplayName()) },
                            onClick = {
                                paisSeleccionado = pais
                                dropdownExpanded = false
                            }
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Campo: Nombre
        Column(modifier = Modifier.fillMaxWidth()) {
            Text(
                text = "Nombre",
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                color = Color.Black
            )
            Spacer(modifier = Modifier.height(4.dp))
            OutlinedTextField(
                value = nombre,
                onValueChange = { nombre = it },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color.Black,
                    unfocusedBorderColor = Color.Black
                )
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Campo: Telefono
        Column(modifier = Modifier.fillMaxWidth()) {
            Text(
                text = "Telefono",
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                color = Color.Black
            )
            Spacer(modifier = Modifier.height(4.dp))
            OutlinedTextField(
                value = telefono,
                onValueChange = { telefono = it },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color.Black,
                    unfocusedBorderColor = Color.Black
                )
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Campo: Nota
        Column(modifier = Modifier.fillMaxWidth()) {
            Text(
                text = "Nota",
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                color = Color.Black
            )
            Spacer(modifier = Modifier.height(4.dp))
            OutlinedTextField(
                value = nota,
                onValueChange = { nota = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(110.dp)
                    .background(Color(0xFFA5D6A7), RoundedCornerShape(16.dp)),
                shape = RoundedCornerShape(16.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color.Black,
                    unfocusedBorderColor = Color.Black,
                    focusedContainerColor = Color(0xFFA5D6A7),
                    unfocusedContainerColor = Color(0xFFA5D6A7)
                )
            )
        }

        Spacer(modifier = Modifier.height(28.dp))

        // Boton: Salvar Contacto (o Actualizar Contacto)
        Button(
            onClick = {
                when {
                    nombre.isBlank() -> alertMessage = "debe escribir un nombre"
                    telefono.isBlank() -> alertMessage = "debe escribir un telefono"
                    nota.isBlank() -> alertMessage = "debe escribir una nota"
                    else -> {
                        val nuevoContacto = Contacto(
                            id = contactoEditar?.id ?: 0,
                            pais = paisSeleccionado.getDisplayName(),
                            nombre = nombre.trim(),
                            telefono = telefono.trim(),
                            nota = nota.trim(),
                            foto = fotoBytes
                        )

                        if (contactoEditar == null) {
                            val id = dbHelper.insertarContacto(nuevoContacto)
                            if (id > 0) {
                                Toast.makeText(context, "Contacto guardado exitosamente", Toast.LENGTH_SHORT).show()
                                nombre = ""
                                telefono = ""
                                nota = ""
                                fotoBytes = null
                                onSavedSuccessfully()
                            } else {
                                Toast.makeText(context, "Error al guardar contacto", Toast.LENGTH_SHORT).show()
                            }
                        } else {
                            val rows = dbHelper.actualizarContacto(nuevoContacto)
                            if (rows > 0) {
                                Toast.makeText(context, "Contacto actualizado correctamente", Toast.LENGTH_SHORT).show()
                                onSavedSuccessfully()
                            } else {
                                Toast.makeText(context, "Error al actualizar contacto", Toast.LENGTH_SHORT).show()
                            }
                        }
                    }
                }
            },
            modifier = Modifier
                .width(220.dp)
                .height(48.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF008000)),
            shape = RoundedCornerShape(6.dp)
        ) {
            Text(
                text = if (contactoEditar == null) "Guardar Contacto" else "Actualizar Contacto",
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Boton: Contactos Salvados
        Button(
            onClick = onNavigateToSavedContacts,
            modifier = Modifier
                .width(220.dp)
                .height(48.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0000FF)),
            shape = RoundedCornerShape(6.dp)
        ) {
            Text(
                text = "Contactos Salvados",
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp
            )
        }
    }

    // Validation Alert Dialog matching wireframe
    if (alertMessage != null) {
        AlertDialog(
            onDismissRequest = { alertMessage = null },
            title = { Text(text = "Alerta", fontWeight = FontWeight.Bold) },
            text = { Text(text = alertMessage ?: "") },
            confirmButton = {
                TextButton(onClick = { alertMessage = null }) {
                    Text("OK", fontWeight = FontWeight.Bold)
                }
            }
        )
    }

    // Image Picker Option Dialog (Camera or Gallery)
    if (showImagePickerDialog) {
        AlertDialog(
            onDismissRequest = { showImagePickerDialog = false },
            title = { Text("Seleccionar Imagen") },
            text = { Text("Elija una opción para establecer la foto del contacto") },
            confirmButton = {
                TextButton(onClick = {
                    showImagePickerDialog = false
                    cameraLauncher.launch(null)
                }) {
                    Text("Cámara")
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    showImagePickerDialog = false
                    galleryLauncher.launch("image/*")
                }) {
                    Text("Galería")
                }
            }
        )
    }
}

@Composable
fun SmileyAvatar(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        val radius = size.minDimension / 2.8f
        val centerPoint = center

        drawCircle(
            color = Color.Black,
            radius = radius / 2f,
            center = centerPoint.copy(y = centerPoint.y - radius / 2.5f),
            style = Stroke(width = 4.dp.toPx())
        )

        val eyeRadius = 4.dp.toPx()
        drawCircle(
            color = Color.Black,
            radius = eyeRadius,
            center = centerPoint.copy(
                x = centerPoint.x - radius / 5f,
                y = centerPoint.y - radius / 1.8f
            )
        )
        drawCircle(
            color = Color.Black,
            radius = eyeRadius,
            center = centerPoint.copy(
                x = centerPoint.x + radius / 5f,
                y = centerPoint.y - radius / 1.8f
            )
        )

        drawArc(
            color = Color.Black,
            startAngle = 20f,
            sweepAngle = 140f,
            useCenter = false,
            topLeft = Offset(
                centerPoint.x - radius / 3.5f,
                centerPoint.y - radius / 1.5f
            ),
            size = Size(radius / 1.75f, radius / 2.5f),
            style = Stroke(width = 3.dp.toPx())
        )

        drawArc(
            color = Color.Black,
            startAngle = 180f,
            sweepAngle = 180f,
            useCenter = false,
            topLeft = Offset(
                centerPoint.x - radius,
                centerPoint.y
            ),
            size = Size(radius * 2f, radius * 1.5f),
            style = Stroke(width = 4.dp.toPx())
        )
    }
}

private fun uriToByteArray(context: Context, uri: Uri): ByteArray? {
    return try {
        val inputStream = context.contentResolver.openInputStream(uri)
        val bitmap = BitmapFactory.decodeStream(inputStream)
        val stream = ByteArrayOutputStream()
        bitmap?.compress(Bitmap.CompressFormat.JPEG, 80, stream)
        stream.toByteArray()
    } catch (_: Exception) {
        null
    }
}

private fun bitmapToByteArray(bitmap: Bitmap): ByteArray {
    val stream = ByteArrayOutputStream()
    bitmap.compress(Bitmap.CompressFormat.JPEG, 80, stream)
    return stream.toByteArray()
}
