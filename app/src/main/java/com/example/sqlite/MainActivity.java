package com.example.sqlite;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.card.MaterialCardView;

public class MainActivity extends AppCompatActivity {

    private SessionManager sessionManager;

    // Header Sesión
    private TextView lblNombreUsuario, lblRolUsuario;
    private Button btnLogout;

    // Panel Admin (RF-27)
    private MaterialCardView cardAdmin;
    private Button btnAdminRoles, btnAdminUsuarios;

    // Campos Registro Operativo
    private EditText txtNombre, txtApellido, txtEdad;
    private Button btnGuardar, btnVer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        sessionManager = new SessionManager(this);

        // Verificación de sesión (RF-01)
        if (!sessionManager.isLoggedIn()) {
            irALogin();
            return;
        }

        setContentView(R.layout.activity_main);

        // Header
        lblNombreUsuario = findViewById(R.id.lblNombreUsuario);
        lblRolUsuario = findViewById(R.id.lblRolUsuario);
        btnLogout = findViewById(R.id.btnLogout);

        // Admin (RF-27)
        cardAdmin = findViewById(R.id.cardAdmin);
        btnAdminRoles = findViewById(R.id.btnAdminRoles);
        btnAdminUsuarios = findViewById(R.id.btnAdminUsuarios);

        // Operativo
        txtNombre = findViewById(R.id.txtNombre);
        txtApellido = findViewById(R.id.txtApellido);
        txtEdad = findViewById(R.id.txtEdad);
        btnGuardar = findViewById(R.id.btnGuardar);
        btnVer = findViewById(R.id.btnVer);

        // Mostrar datos de sesión activa
        lblNombreUsuario.setText("Usuario: " + sessionManager.getNombreCompleto() + " (" + sessionManager.getUsername() + ")");
        lblRolUsuario.setText("Rol: " + sessionManager.getNombreRol());

        // RF-02: Cerrar Sesión
        btnLogout.setOnClickListener(v -> cerrarSesion());

        // Visibilidad según rol
        if (sessionManager.isAdmin()) {
            cardAdmin.setVisibility(View.VISIBLE);
            btnAdminRoles.setOnClickListener(v -> startActivity(new Intent(MainActivity.this, AdminRolesActivity.class)));
            btnAdminUsuarios.setOnClickListener(v -> startActivity(new Intent(MainActivity.this, AdminUsuariosActivity.class)));
        } else {
            cardAdmin.setVisibility(View.GONE);
        }

        // Operaciones de personas existentes
        btnGuardar.setOnClickListener(v -> guardarPersona());
        btnVer.setOnClickListener(v -> startActivity(new Intent(MainActivity.this, Leer.class)));
    }

    private void guardarPersona() {
        String nombre = txtNombre.getText().toString().trim();
        String apellido = txtApellido.getText().toString().trim();
        String edad = txtEdad.getText().toString().trim();

        if (nombre.isEmpty() || apellido.isEmpty() || edad.isEmpty()) {
            Toast.makeText(MainActivity.this, "Por favor complete todos los campos.", Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            DBHelper dbHelper = new DBHelper(MainActivity.this);
            SQLiteDatabase db = dbHelper.getWritableDatabase();

            db.execSQL("INSERT INTO persona (nombre, apellido, edad) VALUES (?, ?, ?)",
                    new Object[]{nombre, apellido, Integer.parseInt(edad)});

            db.close();

            Toast.makeText(MainActivity.this, "Registro guardado correctamente.", Toast.LENGTH_SHORT).show();

            txtNombre.setText("");
            txtApellido.setText("");
            txtEdad.setText("");
            txtNombre.requestFocus();

        } catch (Exception e) {
            Toast.makeText(MainActivity.this, "Error al guardar: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    // RF-02: Cerrar Sesión (limpia SharedPreferences y redirige a Login)
    private void cerrarSesion() {
        sessionManager.logout();
        Toast.makeText(this, "Sesión cerrada correctamente.", Toast.LENGTH_SHORT).show();
        irALogin();
    }

    private void irALogin() {
        Intent intent = new Intent(MainActivity.this, LoginActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}
