package com.ecocity.app.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Clase Ayudante (Helper) para la gestión base de la base de datos SQLite.
 * Extiende de SQLiteOpenHelper para manejar la creación y actualización de
 * versiones de la BD.
 * 
 * Responsabilidades:
 * 1. Definir constantes para nombres de tablas y columnas (Esquema).
 * 2. Crear las tablas cuando la BD se inicia por primera vez.
 * 3. Gestionar actualizaciones de estructura (migraciones) cuando cambia la
 * versión.
 */
public class DbHelper extends SQLiteOpenHelper {

    // Nombre del archivo de base de datos
    private static final String DATABASE_NAME = "EcoCity.db";
    // Versión de la base de datos. Incrementar este número si se cambia el esquema
    // (tablas/columnas).
    private static final int DATABASE_VERSION = 3;

    // --- Definición de Tabla Incidencias ---
    public static final String TABLE_INCIDENCIAS = "incidencias";
    public static final String COLUMN_ID = "id";
    public static final String COLUMN_TITULO = "titulo";
    public static final String COLUMN_DESCRIPCION = "descripcion";
    public static final String COLUMN_URGENCIA = "urgencia";
    public static final String COLUMN_FOTOPATH = "foto_path"; // Ruta a la imagen
    public static final String COLUMN_ESTADO = "estado";
    public static final String COLUMN_LATITUD = "latitud";
    public static final String COLUMN_LONGITUD = "longitud";
    public static final String COLUMN_USER_EMAIL = "user_email"; // Clave foránea lógica (Email del usuario)

    // --- Definición de Tabla Usuarios ---
    public static final String TABLE_USERS = "users";
    public static final String COLUMN_USER_ID = "id";
    public static final String COLUMN_NAME = "name";
    public static final String COLUMN_EMAIL = "email";
    public static final String COLUMN_PASSWORD = "password";

    // --- Sentencias SQL de Creación ---

    // SQL para crear la tabla de incidencias
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

    // SQL para crear la tabla de usuarios
    private static final String TABLE_CREATE_USERS = "CREATE TABLE " + TABLE_USERS + " (" +
            COLUMN_USER_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
            COLUMN_NAME + " TEXT, " +
            COLUMN_EMAIL + " TEXT UNIQUE, " + // Email único para evitar duplicados
            COLUMN_PASSWORD + " TEXT" +
            ");";

    /**
     * Constructor del Helper.
     * 
     * @param context Contexto de la aplicación.
     */
    public DbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    /**
     * Se llama cuando la base de datos se crea por primera vez.
     * Aquí ejecutamos los comandos SQL para crear las tablas.
     * 
     * @param db Instancia de la base de datos.
     */
    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(TABLE_CREATE_INCIDENCIAS);
        db.execSQL(TABLE_CREATE_USERS);
    }

    /**
     * Se llama cuando se detecta que la versión de la base de datos ha cambiado
     * (DATABASE_VERSION).
     * Útil para migraciones. En este caso básico, eliminamos las tablas antiguas y
     * las recreamos.
     * ¡CUIDADO! Esto borra los datos existentes.
     * 
     * @param db         Instancia de la base de datos.
     * @param oldVersion Versión anterior.
     * @param newVersion Nueva versión.
     */
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Eliminar tablas antiguas si existen
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_INCIDENCIAS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_USERS);

        // Crear de nuevo
        onCreate(db);
    }
}
