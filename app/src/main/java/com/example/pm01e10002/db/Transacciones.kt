package com.example.pm01e10002.db

object Transacciones {
    const val DB_NAME = "PM01E10002.db"
    const val DB_VERSION = 1

    const val TABLE_CONTACTOS = "contactos"
    const val COL_ID = "id"
    const val COL_PAIS = "pais"
    const val COL_NOMBRE = "nombre"
    const val COL_TELEFONO = "telefono"
    const val COL_NOTA = "nota"
    const val COL_FOTO = "foto"

    const val CREATE_TABLE_CONTACTOS = """
        CREATE TABLE $TABLE_CONTACTOS (
            $COL_ID INTEGER PRIMARY KEY AUTOINCREMENT,
            $COL_PAIS TEXT NOT NULL,
            $COL_NOMBRE TEXT NOT NULL,
            $COL_TELEFONO TEXT NOT NULL,
            $COL_NOTA TEXT NOT NULL,
            $COL_FOTO BLOB
        )
    """

    const val DROP_TABLE_CONTACTOS = "DROP TABLE IF EXISTS $TABLE_CONTACTOS"
}
