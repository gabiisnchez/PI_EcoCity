package com.ecocity.app.utils;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import com.ecocity.app.ui.LoginActivity;
import java.util.HashMap;

/**
 * <h1>SessionManager</h1>
 * <p>
 * Clase utilitaria para gestionar la sesión del usuario.
 * Utiliza {@link SharedPreferences} para persistir el estado de logueo y los
 * datos básicos del usuario
 * (Nombre y Email) de forma local en el dispositivo.
 * </p>
 */
public class SessionManager {

    // Referencias a SharedPreferences
    SharedPreferences pref;
    SharedPreferences.Editor editor;
    Context _context;

    // Modo de acceso privado (Solo esta app puede acceder)
    int PRIVATE_MODE = 0;

    // Constantes para nombres de archivo y claves
    private static final String PREF_NAME = "EcoCityPref";
    private static final String IS_LOGIN = "IsLoggedIn";
    public static final String KEY_NAME = "name";
    public static final String KEY_EMAIL = "email";

    /**
     * Constructor.
     * 
     * @param context Contexto de la aplicación.
     */
    public SessionManager(Context context) {
        this._context = context;
        pref = _context.getSharedPreferences(PREF_NAME, PRIVATE_MODE);
        editor = pref.edit();
    }

    /**
     * Crea una sesión de inicio de sesión.
     * Guarda el booleano IS_LOGIN a true y almacena los datos del usuario.
     * 
     * @param name  Nombre del usuario.
     * @param email Email del usuario.
     */
    public void createLoginSession(String name, String email) {
        editor.putBoolean(IS_LOGIN, true);
        editor.putString(KEY_NAME, name);
        editor.putString(KEY_EMAIL, email);
        editor.apply(); // Guardar cambios asíncronamente
    }

    /**
     * Verifica si el usuario está logueado.
     * Si no lo está, redirige a la LoginActivity.
     */
    public void checkLogin() {
        if (!this.isLoggedIn()) {
            // Usuario no logueado -> Redirigir a Login
            Intent i = new Intent(_context, LoginActivity.class);

            // Cerrar todas las actividades de la pila
            i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

            // Añadir flag de nueva tarea para iniciar activity
            i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

            _context.startActivity(i);
        }
    }

    /**
     * Obtiene los datos del usuario almacenados en sesión.
     * 
     * @return HashMap con nombre y email.
     */
    public HashMap<String, String> getUserDetails() {
        HashMap<String, String> user = new HashMap<String, String>();
        user.put(KEY_NAME, pref.getString(KEY_NAME, null));
        user.put(KEY_EMAIL, pref.getString(KEY_EMAIL, null));
        return user;
    }

    /**
     * Cierra la sesión del usuario.
     * Borra los datos de SharedPreferences y redirige al Login.
     */
    public void logoutUser() {
        // Borrar datos de sesión
        editor.remove(IS_LOGIN);
        editor.remove(KEY_NAME);
        editor.remove(KEY_EMAIL);
        editor.apply();

        // Redirigir a Login
        Intent i = new Intent(_context, LoginActivity.class);
        i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        _context.startActivity(i);
    }

    /**
     * Verifica el estado de login.
     * 
     * @return true si el usuario está logueado, false en caso contrario.
     */
    public boolean isLoggedIn() {
        return pref.getBoolean(IS_LOGIN, false);
    }
}
