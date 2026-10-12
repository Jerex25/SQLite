package com.example.sqlite;

import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import java.util.ArrayList;

public class Leer extends AppCompatActivity {

    // UI + estructuras de datos que alimentan el ListView
    private ListView lst1;
    private Button btnNuevo;
    private ArrayAdapter<String> arrayAdapter; // Adaptador para pintar 'arreglo' en la lista
    private ArrayList<String> arreglo;         // Texto que se muestra en cada fila
    private ArrayList<Persona> lista;          // Modelo real (para mandar Persona a Editar)

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view); // Carga el layout con el ListView (id: lst1)

        lst1 = findViewById(R.id.lst1);
        btnNuevo = findViewById(R.id.btn_nuevo_registro);

        arreglo = new ArrayList<>();
        lista = new ArrayList<>();

        // Adaptador simple de Android para mostrar Strings en el ListView
        arrayAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, arreglo);
        if (lst1 != null) {
            lst1.setAdapter(arrayAdapter);

            // Click en fila: abre Editar y pasa el objeto seleccionado por extras
            lst1.setOnItemClickListener((AdapterView<?> adapterView, android.view.View view, int position, long id) -> {
                Persona persona = lista.get(position);
                Intent i = new Intent(getApplicationContext(), Editar.class);
                i.putExtra("id", persona.id);
                i.putExtra("nombre", persona.nombre);
                i.putExtra("apellido", persona.apellido);
                i.putExtra("edad", persona.edad);
                startActivity(i);
            });
        }

        if (btnNuevo != null) {
            btnNuevo.setOnClickListener(v -> {
                Intent intent = new Intent(this, MainActivity.class);
                startActivity(intent);
            });
        }

        cargarDatos();
    }

    @Override
    protected void onResume() {
        super.onResume();
        cargarDatos(); // Recarga los datos al volver a la pantalla
    }

    private void cargarDatos() {
        // Siempre usa DBHelper para abrir la MISMA BD (nombre/versión/esquema)
        DBHelper dbHelper = new DBHelper(this);
        SQLiteDatabase db = dbHelper.getReadableDatabase(); // Solo lectura para listar

        try (Cursor c = db.rawQuery("SELECT * FROM persona", null)) {
            arreglo.clear();
            lista.clear();

            if (c.moveToFirst()) {
                int idIndex = c.getColumnIndexOrThrow("id");
                int nombreIndex = c.getColumnIndexOrThrow("nombre");
                int apellidoIndex = c.getColumnIndexOrThrow("apellido");
                int edadIndex = c.getColumnIndexOrThrow("edad");

                do {
                    Persona persona = new Persona();
                    persona.id = c.getString(idIndex);
                    persona.nombre = c.getString(nombreIndex);
                    persona.apellido = c.getString(apellidoIndex);
                    persona.edad = c.getString(edadIndex);

                    lista.add(persona);
                    arreglo.add(persona.id + " - " + persona.nombre + " " + persona.apellido + " (" + persona.edad + ")");
                } while (c.moveToNext());
            }

            if (arrayAdapter != null) {
                arrayAdapter.notifyDataSetChanged();
            }
        } catch (Exception e) {
            Toast.makeText(this, "Error al leer: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        } finally {
            if (db != null && db.isOpen()) db.close();
        }
    }
}
