package com.example.sqlite;

import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.materialswitch.MaterialSwitch;

import java.util.ArrayList;
import java.util.List;

public class AdminUsuariosActivity extends AppCompatActivity {

    private EditText txtUsernameReg, txtNombreCompletoReg, txtPasswordReg;
    private Spinner spnRoles;
    private MaterialSwitch swActivo;
    private Button btnGuardarUsuario;
    private ListView lstUsuarios;

    private DBHelper dbHelper;
    private List<Rol> listaRolesObj;
    private List<Usuario> listaUsuariosObj;
    private ArrayAdapter<String> adapterUsuarios;
    private ArrayList<String> listaUsuariosString;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_usuarios);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Gestión de Usuarios (RF-27)");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        dbHelper = new DBHelper(this);

        txtUsernameReg = findViewById(R.id.txtUsernameReg);
        txtNombreCompletoReg = findViewById(R.id.txtNombreCompletoReg);
        txtPasswordReg = findViewById(R.id.txtPasswordReg);
        spnRoles = findViewById(R.id.spnRoles);
        swActivo = findViewById(R.id.swActivo);
        btnGuardarUsuario = findViewById(R.id.btnGuardarUsuario);
        lstUsuarios = findViewById(R.id.lstUsuarios);

        listaUsuariosString = new ArrayList<>();
        adapterUsuarios = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, listaUsuariosString);
        lstUsuarios.setAdapter(adapterUsuarios);

        btnGuardarUsuario.setOnClickListener(v -> guardarUsuario());

        lstUsuarios.setOnItemClickListener((parent, view, position, id) -> {
            Usuario u = listaUsuariosObj.get(position);
            int nuevoEstado = u.isActivo() ? 0 : 1;
            String accion = (nuevoEstado == 1) ? "activar" : "desactivar";

            new AlertDialog.Builder(this)
                    .setTitle("Cambiar estado de usuario")
                    .setMessage("¿Desea " + accion + " al usuario '" + u.getUsername() + "'?")
                    .setPositiveButton("Sí", (dialog, which) -> {
                        boolean ok = dbHelper.cambiarEstadoUsuario(u.getIdUsuario(), nuevoEstado);
                        if (ok) {
                            Toast.makeText(this, "Estado actualizado correctamente.", Toast.LENGTH_SHORT).show();
                            cargarUsuarios();
                        } else {
                            Toast.makeText(this, "Error al actualizar estado.", Toast.LENGTH_SHORT).show();
                        }
                    })
                    .setNegativeButton("Cancelar", null)
                    .show();
        });

        cargarSpinnerRoles();
        cargarUsuarios();
    }

    private void cargarSpinnerRoles() {
        listaRolesObj = dbHelper.obtenerRoles();
        List<String> nombresRoles = new ArrayList<>();
        for (Rol r : listaRolesObj) {
            nombresRoles.add(r.getNombreRol());
        }
        ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, nombresRoles);
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spnRoles.setAdapter(spinnerAdapter);
    }

    private void guardarUsuario() {
        String username = txtUsernameReg.getText().toString().trim();
        String nombre = txtNombreCompletoReg.getText().toString().trim();
        String password = txtPasswordReg.getText().toString().trim();

        if (username.isEmpty() || nombre.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Por favor complete todos los campos obligatorios.", Toast.LENGTH_SHORT).show();
            return;
        }

        if (spnRoles.getSelectedItemPosition() < 0 || listaRolesObj.isEmpty()) {
            Toast.makeText(this, "Debe seleccionar un rol para el usuario.", Toast.LENGTH_SHORT).show();
            return;
        }

        Rol rolSeleccionado = listaRolesObj.get(spnRoles.getSelectedItemPosition());
        int activo = swActivo.isChecked() ? 1 : 0;

        try {
            // RF-27: Registro con username único (UK)
            dbHelper.registrarUsuario(username, password, nombre, rolSeleccionado.getIdRol(), activo);
            Toast.makeText(this, "Usuario registrado exitosamente.", Toast.LENGTH_SHORT).show();

            txtUsernameReg.setText("");
            txtNombreCompletoReg.setText("");
            txtPasswordReg.setText("");
            swActivo.setChecked(true);

            cargarUsuarios();
        } catch (Exception e) {
            Toast.makeText(this, e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    private void cargarUsuarios() {
        listaUsuariosString.clear();
        listaUsuariosObj = dbHelper.obtenerUsuarios();

        for (Usuario u : listaUsuariosObj) {
            String estadoStr = u.isActivo() ? "ACTIVO (1)" : "INACTIVO (0)";
            listaUsuariosString.add("ID: " + u.getIdUsuario() + " | Username: " + u.getUsername() +
                    "\nNombre: " + u.getNombreCompleto() +
                    "\nRol: " + u.getNombreRol() + " | Estado: " + estadoStr);
        }
        adapterUsuarios.notifyDataSetChanged();
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }
}
