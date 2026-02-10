package com.ecocity.app.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Helper para la gestión de la base de datos SQLite.
 * Se encarga de la creación y actualización de las tablas de la aplicación.
 */
public class DbHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "EcoCity.db";
    private static final int DATABASE_VERSION = 3; // Incrementado

    // Tabla de Incidencias
    public static final String TABLE_INCIDENCIAS = "incidencias";
    public static final String COLUMN_ID = "id";
    public static final String COLUMN_TITULO = "titulo";
    public static final String COLUMN_DESCRIPCION = "descripcion";
    public static final String COLUMN_URGENCIA = "urgencia";
    public static final String COLUMN_FOTOPATH = "foto_path";
    public static final String COLUMN_ESTADO = "estado";
    public static final String COLUMN_LATITUD = "latitud";
    public static final String COLUMN_LONGITUD = "longitud";
    public static final String COLUMN_USER_EMAIL = "user_email";

    // Tabla de Usuarios
    public static final String TABLE_USERS = "users";
    public static final String COLUMN_USER_ID = "id";
    public static final String COLUMN_NAME = "name";
    public static final String COLUMN_EMAIL = "email";
    public static final String COLUMN_PASSWORD = "password";

    // SQL Para crear tabla de incidencias
    private static final String TABLE_CREATE_INCIDENCIAS = "CREATE TABLE " + TABLE_INCIDENCIAS + " (" +
            COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
            COLUMN_TITULO + " TEXT, " +
            COLUMN_DESCRIPCION + " TEXT, " +
            COLUMN_URGENCIA + " TEXT, " +
            COLUMN_FOTOPATH + " TEXT, " +
            COLUMN_ESTADO + " TEXT, " +
            COLUMN_LATITUD + " REAL, " +
            COLUMN_LONGITUD + " REAL, " +
            COLUMN_USER_EMAIL + " TEXT" +
            ");";

    // SQL Para crear tabla de usuarios
    private static final String TABLE_CREATE_USERS = "CREATE TABLE " + TABLE_USERS + " (" +
            COLUMN_USER_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
            COLUMN_NAME + " TEXT, " +
            COLUMN_EMAIL + " TEXT UNIQUE, " +
            COLUMN_PASSWORD + " TEXT" +
            ");";

    public DbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(TABLE_CREATE_INCIDENCIAS);
        db.execSQL(TABLE_CREATE_USERS);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Eliminar tablas antiguas si existen
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_INCIDENCIAS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_USERS);

        // Crear de nuevo
        onCreate(db);
    }
}
