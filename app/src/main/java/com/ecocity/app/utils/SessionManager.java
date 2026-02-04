package com.ecocity.app.utils;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import com.ecocity.app.ui.LoginActivity;
import java.util.HashMap;

public class SessionManager {

    SharedPreferences pref;
    SharedPreferences.Editor editor;
    Context _context;

    int PRIVATE_MODE = 0;
    private static final String PREF_NAME = "EcoCityPref";
    private static final String IS_LOGIN = "IsLoggedIn";
    public static final String KEY_NAME = "name";
    public static final String KEY_EMAIL = "email";
    
    // Simulated Database Keys
    private static final String KEY_REGISTERED_EMAIL = "sim_email";
    private static final String KEY_REGISTERED_PASS = "sim_pass";
    private static final String KEY_REGISTERED_NAME = "sim_name";

    public SessionManager(Context context) {
        this._context = context;
        pref = _context.getSharedPreferences(PREF_NAME, PRIVATE_MODE);
        editor = pref.edit();
    }

    public void createLoginSession(String name, String email) {
        editor.putBoolean(IS_LOGIN, true);
        editor.putString(KEY_NAME, name);
        editor.putString(KEY_EMAIL, email);
        editor.apply(); // apply is async, better than commit
    }
    
    public void saveRegisteredUser(String name, String email, String password) {
        editor.putString(KEY_REGISTERED_NAME, name);
        editor.putString(KEY_REGISTERED_EMAIL, email);
        editor.putString(KEY_REGISTERED_PASS, password);
        editor.apply();
    }
    
    public HashMap<String, String> getRegisteredUser() {
        HashMap<String, String> user = new HashMap<>();
        user.put(KEY_REGISTERED_NAME, pref.getString(KEY_REGISTERED_NAME, ""));
        user.put(KEY_REGISTERED_EMAIL, pref.getString(KEY_REGISTERED_EMAIL, ""));
        user.put(KEY_REGISTERED_PASS, pref.getString(KEY_REGISTERED_PASS, ""));
        return user;
    }

    public void checkLogin() {
        if (!this.isLoggedIn()) {
            Intent i = new Intent(_context, LoginActivity.class);
            i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            _context.startActivity(i);
        }
    }

    public HashMap<String, String> getUserDetails() {
        HashMap<String, String> user = new HashMap<String, String>();
        user.put(KEY_NAME, pref.getString(KEY_NAME, null));
        user.put(KEY_EMAIL, pref.getString(KEY_EMAIL, null));
        return user;
    }

    public void logoutUser() {
        // Only clear session data, NOT the "database" (registered user)
        editor.remove(IS_LOGIN);
        editor.remove(KEY_NAME);
        editor.remove(KEY_EMAIL);
        editor.apply();
        
        Intent i = new Intent(_context, LoginActivity.class);
        i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        _context.startActivity(i);
    }

    public boolean isLoggedIn() {
        return pref.getBoolean(IS_LOGIN, false);
    }
}
