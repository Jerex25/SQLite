package com.example.sqlite;

import android.content.Context;
import android.content.SharedPreferences;

public class SessionManager {
    private static final String PREF_NAME = "UserSessionPref";
    private static final String KEY_IS_LOGGED_IN = "isLoggedIn";
    private static final String KEY_USER_ID = "userId";
    private static final String KEY_USERNAME = "username";
    private static final String KEY_NOMBRE_COMPLETO = "nombreCompleto";
    private static final String KEY_ID_ROL = "idRol";
    private static final String KEY_NOMBRE_ROL = "nombreRol";

    private final SharedPreferences pref;
    private final SharedPreferences.Editor editor;

    public SessionManager(Context context) {
        pref = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        editor = pref.edit();
    }

    public void createSession(int userId, String username, String nombreCompleto, int idRol, String nombreRol) {
        editor.putBoolean(KEY_IS_LOGGED_IN, true);
        editor.putInt(KEY_USER_ID, userId);
        editor.putString(KEY_USERNAME, username);
        editor.putString(KEY_NOMBRE_COMPLETO, nombreCompleto);
        editor.putInt(KEY_ID_ROL, idRol);
        editor.putString(KEY_NOMBRE_ROL, nombreRol);
        editor.apply();
    }

    public boolean isLoggedIn() {
        return pref.getBoolean(KEY_IS_LOGGED_IN, false);
    }

    public int getUserId() {
        return pref.getInt(KEY_USER_ID, -1);
    }

    public String getUsername() {
        return pref.getString(KEY_USERNAME, "");
    }

    public String getNombreCompleto() {
        return pref.getString(KEY_NOMBRE_COMPLETO, "");
    }

    public int getIdRol() {
        return pref.getInt(KEY_ID_ROL, -1);
    }

    public String getNombreRol() {
        return pref.getString(KEY_NOMBRE_ROL, "");
    }

    public boolean isAdmin() {
        String role = getNombreRol();
        return role != null && (role.equalsIgnoreCase("Administrador de Bodega") || role.equalsIgnoreCase("Admin"));
    }

    public void logout() {
        editor.clear();
        editor.apply();
    }
}
