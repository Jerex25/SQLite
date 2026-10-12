package com.example.sqlite;

import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.List;

public class AdminRolesActivity extends AppCompatActivity {

    private EditText txtNombreRol, txtDescripcionRol;
    private Button btnGuardarRol;
    private ListView lstRoles;

    private DBHelper dbHelper;
    private ArrayAdapter<String> adapter;
    private ArrayList<String> listaRolesString;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_roles);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Gestión de Roles (RF-27)");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        dbHelper = new DBHelper(this);

        txtNombreRol = findViewById(R.id.txtNombreRol);
        txtDescripcionRol = findViewById(R.id.txtDescripcionRol);
        btnGuardarRol = findViewById(R.id.btnGuardarRol);
        lstRoles = findViewById(R.id.lstRoles);

        listaRolesString = new ArrayList<>();
        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, listaRolesString);
        lstRoles.setAdapter(adapter);

        btnGuardarRol.setOnClickListener(v -> guardarRol());

        cargarRoles();
    }

    private void guardarRol() {
        String nombre = txtNombreRol.getText().toString().trim();
        String desc = txtDescripcionRol.getText().toString().trim();

        if (nombre.isEmpty()) {
            Toast.makeText(this, "El nombre del rol es obligatorio.", Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            dbHelper.registrarRol(nombre, desc);
            Toast.makeText(this, "Rol registrado con éxito.", Toast.LENGTH_SHORT).show();
            txtNombreRol.setText("");
            txtDescripcionRol.setText("");
            cargarRoles();
        } catch (Exception e) {
            Toast.makeText(this, e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    private void cargarRoles() {
        listaRolesString.clear();
        List<Rol> roles = dbHelper.obtenerRoles();
        for (Rol r : roles) {
            listaRolesString.add("ID: " + r.getIdRol() + " - " + r.getNombreRol() + "\nDescripción: " + (r.getDescripcion().isEmpty() ? "Sin descripción" : r.getDescripcion()));
        }
        adapter.notifyDataSetChanged();
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }
}
