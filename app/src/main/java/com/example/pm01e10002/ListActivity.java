package com.example.pm01e10002;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.List;

public class ListActivity extends AppCompatActivity {

    private ListView listView;
    private EditText txtBuscar;
    private Button btnCompartir, btnVerImagen, btnEliminar, btnActualizar, btnAtras;

    private List<Contacto> listaContactos;
    private List<Contacto> listaFiltrada;
    private ArrayAdapter<Contacto> adapter;
    private Contacto contactoSeleccionado = null;
    private SQLiteConexion db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list);

        listView = findViewById(R.id.listView);
        txtBuscar = findViewById(R.id.txtBuscar);
        btnCompartir = findViewById(R.id.btnCompartir);
        btnVerImagen = findViewById(R.id.btnVerImagen);
        btnEliminar = findViewById(R.id.btnEliminar);
        btnActualizar = findViewById(R.id.btnActualizar);
        btnAtras = findViewById(R.id.btnAtras);

        db = new SQLiteConexion(this);

        cargarContactos();

        btnAtras.setOnClickListener(v -> finish());

        txtBuscar.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filtrarListado(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        listView.setOnItemClickListener((parent, view, position, id) -> {
            contactoSeleccionado = listaFiltrada.get(position);
            mostrarDialogoLlamar(contactoSeleccionado);
        });

        btnCompartir.setOnClickListener(v -> {
            if (contactoSeleccionado != null) {
                Intent sendIntent = new Intent();
                sendIntent.setAction(Intent.ACTION_SEND);
                sendIntent.putExtra(Intent.EXTRA_TEXT, "Contacto: " + contactoSeleccionado.getNombre() + " - Tel: " + contactoSeleccionado.getTelefono());
                sendIntent.setType("text/plain");
                startActivity(Intent.createChooser(sendIntent, "Compartir contacto"));
            } else {
                Toast.makeText(this, "Seleccione un contacto primero", Toast.LENGTH_SHORT).show();
            }
        });

        btnVerImagen.setOnClickListener(v -> {
            if (contactoSeleccionado != null && contactoSeleccionado.getImagen() != null) {
                Intent intent = new Intent(ListActivity.this, ImageViewActivity.class);
                intent.putExtra("imagen", contactoSeleccionado.getImagen());
                startActivity(intent);
            } else {
                Toast.makeText(this, "El contacto no tiene imagen", Toast.LENGTH_SHORT).show();
            }
        });

        btnEliminar.setOnClickListener(v -> {
            if (contactoSeleccionado != null) {
                db.eliminarContacto(contactoSeleccionado.getId());
                Toast.makeText(this, "Contacto eliminado", Toast.LENGTH_SHORT).show();
                contactoSeleccionado = null;
                cargarContactos();
            } else {
                Toast.makeText(this, "Seleccione un contacto a eliminar", Toast.LENGTH_SHORT).show();
            }
        });

        btnActualizar.setOnClickListener(v -> {
            if (contactoSeleccionado != null) {
                Intent i = new Intent(ListActivity.this, MainActivity.class);
                i.putExtra("contacto", contactoSeleccionado);
                startActivity(i);
            } else {
                Toast.makeText(this, "Seleccione un contacto a actualizar", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        cargarContactos();
    }

    private void cargarContactos() {
        listaContactos = db.obtenerContactos();
        listaFiltrada = new ArrayList<>(listaContactos);
        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_single_choice, listaFiltrada);
        listView.setAdapter(adapter);
        listView.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
    }

    private void filtrarListado(String texto) {
        listaFiltrada.clear();
        for (Contacto c : listaContactos) {
            if (c.getNombre().toLowerCase().contains(texto.toLowerCase()) ||
                    c.getTelefono().contains(texto)) {
                listaFiltrada.add(c);
            }
        }
        adapter.notifyDataSetChanged();
    }

    private void mostrarDialogoLlamar(Contacto c) {
        new AlertDialog.Builder(this)
                .setTitle("Acción")
                .setMessage("¿Desea llamar a " + c.getNombre() + "?")
                .setPositiveButton("Yes", (dialog, which) -> {
                    Intent intent = new Intent(ListActivity.this, CallActivity.class);
                    intent.putExtra("telefono", c.getTelefono());
                    startActivity(intent);
                })
                .setNegativeButton("No", null)
                .show();
    }
}
