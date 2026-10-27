package com.example.pm01e10002.db

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import com.example.pm01e10002.models.Contacto

class SQLiteConexion(private val context: Context) : SQLiteOpenHelper(
    context,
    Transacciones.DB_NAME,
    null,
    Transacciones.DB_VERSION
) {

    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL(Transacciones.CREATE_TABLE_CONTACTOS)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL(Transacciones.DROP_TABLE_CONTACTOS)
        onCreate(db)
    }

    private fun canAccessDb(): Boolean {
        return try {
            if (context.javaClass.name.contains("Layoutlib") || context.javaClass.name.contains("Bridge")) {
                return false
            }
            val dbPath = context.getDatabasePath(Transacciones.DB_NAME)
            dbPath != null && dbPath.parentFile != null
        } catch (_: Throwable) {
            false
        }
    }

    fun insertarContacto(contacto: Contacto): Long {
        if (!canAccessDb()) return -1L
        return try {
            val db = writableDatabase
            val values = ContentValues().apply {
                put(Transacciones.COL_PAIS, contacto.pais)
                put(Transacciones.COL_NOMBRE, contacto.nombre)
                put(Transacciones.COL_TELEFONO, contacto.telefono)
                put(Transacciones.COL_NOTA, contacto.nota)
                put(Transacciones.COL_FOTO, contacto.foto)
            }
            val id = db.insert(Transacciones.TABLE_CONTACTOS, null, values)
            db.close()
            id
        } catch (_: Throwable) {
            -1L
        }
    }

    fun actualizarContacto(contacto: Contacto): Int {
        if (!canAccessDb()) return 0
        return try {
            val db = writableDatabase
            val values = ContentValues().apply {
                put(Transacciones.COL_PAIS, contacto.pais)
                put(Transacciones.COL_NOMBRE, contacto.nombre)
                put(Transacciones.COL_TELEFONO, contacto.telefono)
                put(Transacciones.COL_NOTA, contacto.nota)
                put(Transacciones.COL_FOTO, contacto.foto)
            }
            val rows = db.update(
                Transacciones.TABLE_CONTACTOS,
                values,
                "${Transacciones.COL_ID} = ?",
                arrayOf(contacto.id.toString())
            )
            db.close()
            rows
        } catch (_: Throwable) {
            0
        }
    }

    fun eliminarContacto(id: Int): Int {
        if (!canAccessDb()) return 0
        return try {
            val db = writableDatabase
            val rows = db.delete(
                Transacciones.TABLE_CONTACTOS,
                "${Transacciones.COL_ID} = ?",
                arrayOf(id.toString())
            )
            db.close()
            rows
        } catch (_: Throwable) {
            0
        }
    }

    fun obtenerContactos(query: String = ""): List<Contacto> {
        if (!canAccessDb()) return emptyList()
        val lista = mutableListOf<Contacto>()
        val db = try {
            readableDatabase
        } catch (_: Throwable) {
            return emptyList()
        }

        val selection = if (query.isNotBlank()) {
            "${Transacciones.COL_NOMBRE} LIKE ? OR ${Transacciones.COL_TELEFONO} LIKE ?"
        } else null

        val selectionArgs = if (query.isNotBlank()) {
            arrayOf("%$query%", "%$query%")
        } else null

        try {
            val cursor = db.query(
                Transacciones.TABLE_CONTACTOS,
                null,
                selection,
                selectionArgs,
                null,
                null,
                "${Transacciones.COL_NOMBRE} ASC"
            )

            cursor.use { c ->
                val idIndex = c.getColumnIndexOrThrow(Transacciones.COL_ID)
                val paisIndex = c.getColumnIndexOrThrow(Transacciones.COL_PAIS)
                val nombreIndex = c.getColumnIndexOrThrow(Transacciones.COL_NOMBRE)
                val telefonoIndex = c.getColumnIndexOrThrow(Transacciones.COL_TELEFONO)
                val notaIndex = c.getColumnIndexOrThrow(Transacciones.COL_NOTA)
                val fotoIndex = c.getColumnIndexOrThrow(Transacciones.COL_FOTO)

                while (c.moveToNext()) {
                    val id = c.getInt(idIndex)
                    val pais = c.getString(paisIndex)
                    val nombre = c.getString(nombreIndex)
                    val telefono = c.getString(telefonoIndex)
                    val nota = c.getString(notaIndex)
                    val foto = c.getBlob(fotoIndex)

                    lista.add(
                        Contacto(
                            id = id,
                            pais = pais,
                            nombre = nombre,
                            telefono = telefono,
                            nota = nota,
                            foto = foto
                        )
                    )
                }
            }
        } catch (_: Throwable) {
            // catch layoutlib preview error
        } finally {
            try { db.close() } catch (_: Throwable) {}
        }

        return lista
    }
}
