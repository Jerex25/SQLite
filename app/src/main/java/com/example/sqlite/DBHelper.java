package com.example.sqlite;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;
import java.util.List;

public class DBHelper extends SQLiteOpenHelper {

    private static final String DB_NAME = "BD_EJEMPLO.db";
    private static final int DB_VERSION = 2;

    public DBHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // Tabla persona existente
        db.execSQL("CREATE TABLE persona (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "nombre TEXT NOT NULL, " +
                "apellido TEXT NOT NULL, " +
                "edad INTEGER NOT NULL)");

        // Tabla ROL (RF-27: nombre_rol debe ser único UK)
        db.execSQL("CREATE TABLE ROL (" +
                "id_rol INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "nombre_rol TEXT NOT NULL UNIQUE, " +
                "descripcion TEXT)");

        // Tabla USUARIO (RF-01, RF-27: username debe ser único UK)
        db.execSQL("CREATE TABLE USUARIO (" +
                "id_usuario INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "username TEXT NOT NULL UNIQUE, " +
                "password_hash TEXT NOT NULL, " +
                "nombre_completo TEXT NOT NULL, " +
                "id_rol INTEGER NOT NULL, " +
                "activo INTEGER NOT NULL DEFAULT 1, " +
                "FOREIGN KEY (id_rol) REFERENCES ROL(id_rol))");

        // Datos semilla de roles
        db.execSQL("INSERT INTO ROL (nombre_rol, descripcion) VALUES " +
                "('Administrador de Bodega', 'Privilegios de gestión de usuarios, roles y configuración'), " +
                "('Operador / Bodeguero', 'Permisos operativos para consultar productos y movimientos')");

        // Datos semilla de usuarios por defecto (claves: admin123 y operador123)
        String adminHash = SecurityUtils.hashPassword("admin123");
        String operadorHash = SecurityUtils.hashPassword("operador123");

        db.execSQL("INSERT INTO USUARIO (username, password_hash, nombre_completo, id_rol, activo) VALUES " +
                "('admin', '" + adminHash + "', 'Administrador de Bodega', 1, 1), " +
                "('operador', '" + operadorHash + "', 'Operador Bodeguero', 2, 1)");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (oldVersion < 2) {
            db.execSQL("CREATE TABLE IF NOT EXISTS ROL (" +
                    "id_rol INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "nombre_rol TEXT NOT NULL UNIQUE, " +
                    "descripcion TEXT)");

            db.execSQL("CREATE TABLE IF NOT EXISTS USUARIO (" +
                    "id_usuario INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "username TEXT NOT NULL UNIQUE, " +
                    "password_hash TEXT NOT NULL, " +
                    "nombre_completo TEXT NOT NULL, " +
                    "id_rol INTEGER NOT NULL, " +
                    "activo INTEGER NOT NULL DEFAULT 1, " +
                    "FOREIGN KEY (id_rol) REFERENCES ROL(id_rol))");

            db.execSQL("INSERT OR IGNORE INTO ROL (id_rol, nombre_rol, descripcion) VALUES " +
                    "(1, 'Administrador de Bodega', 'Privilegios de gestión de usuarios, roles y configuración'), " +
                    "(2, 'Operador / Bodeguero', 'Permisos operativos para consultar productos y movimientos')");

            String adminHash = SecurityUtils.hashPassword("admin123");
            String operadorHash = SecurityUtils.hashPassword("operador123");

            db.execSQL("INSERT OR IGNORE INTO USUARIO (username, password_hash, nombre_completo, id_rol, activo) VALUES " +
                    "('admin', '" + adminHash + "', 'Administrador de Bodega', 1, 1), " +
                    "('operador', '" + operadorHash + "', 'Operador Bodeguero', 2, 1)");
        }
    }

    // --- MÉTODOS RF-01: Autenticación ---
    public Usuario autenticarUsuario(String username, String password) throws Exception {
        SQLiteDatabase db = this.getReadableDatabase();
        String sql = "SELECT u.id_usuario, u.username, u.password_hash, u.nombre_completo, u.id_rol, r.nombre_rol, u.activo " +
                "FROM USUARIO u " +
                "INNER JOIN ROL r ON u.id_rol = r.id_rol " +
                "WHERE u.username = ?";

        try (Cursor cursor = db.rawQuery(sql, new String[]{username.trim()})) {
            if (cursor.moveToFirst()) {
                int activo = cursor.getInt(cursor.getColumnIndexOrThrow("activo"));

                // Validación Criterio RF-01: Solo pueden ingresar usuarios activos (activo = 1)
                if (activo != 1) {
                    throw new Exception("El usuario está inactivo (activo = 0). Acceso denegado.");
                }

                String hashEnBD = cursor.getString(cursor.getColumnIndexOrThrow("password_hash"));
                String hashIngresado = SecurityUtils.hashPassword(password.trim());

                if (!hashEnBD.equals(hashIngresado)) {
                    throw new Exception("Contraseña incorrecta.");
                }

                Usuario usuario = new Usuario();
                usuario.setIdUsuario(cursor.getInt(cursor.getColumnIndexOrThrow("id_usuario")));
                usuario.setUsername(cursor.getString(cursor.getColumnIndexOrThrow("username")));
                usuario.setNombreCompleto(cursor.getString(cursor.getColumnIndexOrThrow("nombre_completo")));
                usuario.setIdRol(cursor.getInt(cursor.getColumnIndexOrThrow("id_rol")));
                usuario.setNombreRol(cursor.getString(cursor.getColumnIndexOrThrow("nombre_rol")));
                usuario.setActivo(activo);

                return usuario;
            } else {
                throw new Exception("El nombre de usuario no existe.");
            }
        }
    }

    // --- MÉTODOS RF-27: Administración de Roles ---
    public boolean registrarRol(String nombreRol, String descripcion) throws Exception {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put("nombre_rol", nombreRol.trim());
        cv.put("descripcion", descripcion.trim());

        long result = db.insert("ROL", null, cv);
        if (result == -1) {
            throw new Exception("No se pudo registrar el rol. Verifique que el nombre de rol sea único.");
        }
        return true;
    }

    public List<Rol> obtenerRoles() {
        List<Rol> lista = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        String sql = "SELECT id_rol, nombre_rol, descripcion FROM ROL ORDER BY id_rol ASC";

        try (Cursor c = db.rawQuery(sql, null)) {
            if (c.moveToFirst()) {
                do {
                    Rol r = new Rol();
                    r.setIdRol(c.getInt(c.getColumnIndexOrThrow("id_rol")));
                    r.setNombreRol(c.getString(c.getColumnIndexOrThrow("nombre_rol")));
                    r.setDescripcion(c.getString(c.getColumnIndexOrThrow("descripcion")));
                    lista.add(r);
                } while (c.moveToNext());
            }
        }
        return lista;
    }

    // --- MÉTODOS RF-27: Administración de Usuarios ---
    public boolean registrarUsuario(String username, String password, String nombreCompleto, int idRol, int activo) throws Exception {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put("username", username.trim());
        cv.put("password_hash", SecurityUtils.hashPassword(password.trim()));
        cv.put("nombre_completo", nombreCompleto.trim());
        cv.put("id_rol", idRol);
        cv.put("activo", activo);

        long result = db.insert("USUARIO", null, cv);
        if (result == -1) {
            throw new Exception("No se pudo registrar el usuario. Verifique que el username sea único.");
        }
        return true;
    }

    public List<Usuario> obtenerUsuarios() {
        List<Usuario> lista = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        String sql = "SELECT u.id_usuario, u.username, u.password_hash, u.nombre_completo, u.id_rol, r.nombre_rol, u.activo " +
                "FROM USUARIO u " +
                "INNER JOIN ROL r ON u.id_rol = r.id_rol " +
                "ORDER BY u.id_usuario ASC";

        try (Cursor c = db.rawQuery(sql, null)) {
            if (c.moveToFirst()) {
                do {
                    Usuario u = new Usuario();
                    u.setIdUsuario(c.getInt(c.getColumnIndexOrThrow("id_usuario")));
                    u.setUsername(c.getString(c.getColumnIndexOrThrow("username")));
                    u.setPasswordHash(c.getString(c.getColumnIndexOrThrow("password_hash")));
                    u.setNombreCompleto(c.getString(c.getColumnIndexOrThrow("nombre_completo")));
                    u.setIdRol(c.getInt(c.getColumnIndexOrThrow("id_rol")));
                    u.setNombreRol(c.getString(c.getColumnIndexOrThrow("nombre_rol")));
                    u.setActivo(c.getInt(c.getColumnIndexOrThrow("activo")));
                    lista.add(u);
                } while (c.moveToNext());
            }
        }
        return lista;
    }

    public boolean cambiarEstadoUsuario(int idUsuario, int nuevoEstado) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put("activo", nuevoEstado);

        int rows = db.update("USUARIO", cv, "id_usuario = ?", new String[]{String.valueOf(idUsuario)});
        return rows > 0;
    }
}
