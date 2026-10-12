package com.example.sqlite;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class LoginActivity extends AppCompatActivity {

    private EditText txtUsername, txtPassword;
    private Button btnLogin;
    private SessionManager sessionManager;
    private DBHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        sessionManager = new SessionManager(this);

        // Si ya hay una sesión activa, redirige directamente al menú principal
        if (sessionManager.isLoggedIn()) {
            irAMainActivity();
            return;
        }

        setContentView(R.layout.activity_login);

        dbHelper = new DBHelper(this);

        txtUsername = findViewById(R.id.txtUsername);
        txtPassword = findViewById(R.id.txtPassword);
        btnLogin = findViewById(R.id.btnLogin);

        btnLogin.setOnClickListener(v -> realizarLogin());
    }

    private void realizarLogin() {
        String username = txtUsername.getText().toString().trim();
        String password = txtPassword.getText().toString().trim();

        if (username.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Por favor ingrese su usuario y contraseña.", Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            // RF-01: Verifica concordancia de credenciales y que activo = 1
            Usuario usuario = dbHelper.autenticarUsuario(username, password);

            // Guarda la sesión asociando el id_rol y nombre_rol
            sessionManager.createSession(
                    usuario.getIdUsuario(),
                    usuario.getUsername(),
                    usuario.getNombreCompleto(),
                    usuario.getIdRol(),
                    usuario.getNombreRol()
            );

            Toast.makeText(this, "¡Bienvenido " + usuario.getNombreCompleto() + "!\nRol: " + usuario.getNombreRol(), Toast.LENGTH_LONG).show();

            irAMainActivity();
        } catch (Exception e) {
            // Muestra mensaje específico de validación (ej. inactivo, clave errónea, etc.)
            Toast.makeText(this, e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    private void irAMainActivity() {
        Intent intent = new Intent(LoginActivity.this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}
