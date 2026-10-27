package com.example.pm01e10002;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;
import java.util.List;

public class SQLiteConexion extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "ContactosDB";
    private static final int DATABASE_VERSION = 1;
    private static final String TABLE_CONTACTOS = "contactos";

    public SQLiteConexion(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String CREATE_TABLE = "CREATE TABLE " + TABLE_CONTACTOS + " (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "pais TEXT, " +
                "nombre TEXT, " +
                "telefono TEXT, " +
                "nota TEXT, " +
                "imagen BLOB)";
        db.execSQL(CREATE_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_CONTACTOS);
        onCreate(db);
    }

    public long insertarContacto(Contacto c) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("pais", c.getPais());
        values.put("nombre", c.getNombre());
        values.put("telefono", c.getTelefono());
        values.put("nota", c.getNota());
        values.put("imagen", c.getImagen());
        long id = db.insert(TABLE_CONTACTOS, null, values);
        db.close();
        return id;
    }

    public int actualizarContacto(Contacto c) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("pais", c.getPais());
        values.put("nombre", c.getNombre());
        values.put("telefono", c.getTelefono());
        values.put("nota", c.getNota());
        values.put("imagen", c.getImagen());
        int res = db.update(TABLE_CONTACTOS, values, "id = ?", new String[]{String.valueOf(c.getId())});
        db.close();
        return res;
    }

    public int eliminarContacto(int id) {
        SQLiteDatabase db = this.getWritableDatabase();
        int res = db.delete(TABLE_CONTACTOS, "id = ?", new String[]{String.valueOf(id)});
        db.close();
        return res;
    }

    public List<Contacto> obtenerContactos() {
        List<Contacto> lista = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM " + TABLE_CONTACTOS, null);

        if (cursor.moveToFirst()) {
            do {
                Contacto c = new Contacto();
                c.setId(cursor.getInt(0));
                c.setPais(cursor.getString(1));
                c.setNombre(cursor.getString(2));
                c.setTelefono(cursor.getString(3));
                c.setNota(cursor.getString(4));
                c.setImagen(cursor.getBlob(5));
                lista.add(c);
            } while (cursor.moveToNext());
        }
        cursor.close();
        db.close();
        return lista;
    }
}
