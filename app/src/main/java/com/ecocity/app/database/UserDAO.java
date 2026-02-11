package com.ecocity.app.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.ecocity.app.model.User;

/**
 * Data Access Object (DAO) para la gestión de Usuarios.
 * Maneja el Registro y Login (Autenticación) contra la base de datos local.
 */
public class UserDAO {
    private SQLiteDatabase database;
    private DbHelper dbHelper;

    /**
     * Constructor del UserDAO.
     * 
     * @param context Contexto de la aplicación.
     */
    public UserDAO(Context context) {
        dbHelper = new DbHelper(context);
    }

    /**
     * Abre conexión a BD en escritura.
     */
    public void open() {
        database = dbHelper.getWritableDatabase();
    }

    /**
     * Cierra conexión.
     */
    public void close() {
        dbHelper.close();
    }

    /**
     * REGISTRO: Crea un nuevo usuario en el sistema.
     * 
     * @param user Objeto User con los datos (Nombre, Email, Password).
     * @return El ID del nuevo usuario o -1 si falla.
     */
    public long registerUser(User user) {
        ContentValues values = new ContentValues();
        values.put(DbHelper.COLUMN_NAME, user.getName());
        values.put(DbHelper.COLUMN_EMAIL, user.getEmail());
        values.put(DbHelper.COLUMN_PASSWORD, user.getPassword());

        return database.insert(DbHelper.TABLE_USERS, null, values);
    }

    /**
     * LOGIN: Verifica credenciales y devuelve el usuario si son correctas.
     * Busca una fila que coincida EXACTAMENTE con el email y la contraseña
     * proporcionados.
     * 
     * @param email    Correo electrónico.
     * @param password Contraseña.
     * @return Objeto User con sus datos si el login es correcto, 'null' si falla.
     */
    public User login(String email, String password) {
        // SELECT * FROM users WHERE email=? AND password=?
        Cursor cursor = database.query(DbHelper.TABLE_USERS,
                new String[] { DbHelper.COLUMN_USER_ID, DbHelper.COLUMN_NAME, DbHelper.COLUMN_EMAIL,
                        DbHelper.COLUMN_PASSWORD },
                DbHelper.COLUMN_EMAIL + "=? AND " + DbHelper.COLUMN_PASSWORD + "=?",
                new String[] { email, password },
                null, null, null);

        if (cursor != null && cursor.moveToFirst()) {
            // Usuario Encontrado
            User user = new User(
                    cursor.getInt(0), // id
                    cursor.getString(1), // name
                    cursor.getString(2), // email
                    cursor.getString(3)); // password (hash o plano si no se encriptó)
            cursor.close();
            return user;
        }
        // Usuario no encontrado o credenciales incorrectas
        return null;
    }

    /**
     * Verifica si un email ya está en uso.
     * Utilizado durante el registro para evitar duplicados.
     * 
     * @param email Email a verificar.
     * @return true si el email ya existe, false si está libre.
     */
    public boolean checkEmailExists(String email) {
        Cursor cursor = database.query(DbHelper.TABLE_USERS,
                new String[] { DbHelper.COLUMN_USER_ID }, // Solo necesitamos saber si existe una ID
                DbHelper.COLUMN_EMAIL + "=?",
                new String[] { email },
                null, null, null);

        boolean exists = (cursor.getCount() > 0);
        cursor.close();
        return exists;
    }
}
