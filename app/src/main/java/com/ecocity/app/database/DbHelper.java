package com.ecocity.app.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DbHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "EcoCity.db";
    private static final int DATABASE_VERSION = 1;

    public static final String TABLE_INCIDENCIAS = "incidencias";
    public static final String COLUMN_ID = "id";
    public static final String COLUMN_TITULO = "titulo";
    public static final String COLUMN_DESCRIPCION = "descripcion";
    public static final String COLUMN_URGENCIA = "urgencia";
    public static final String COLUMN_FOTOPATH = "foto_path";
    public static final String COLUMN_ESTADO = "estado";
    public static final String COLUMN_LATITUD = "latitud";
    public static final String COLUMN_LONGITUD = "longitud";

    private static final String TABLE_CREATE =
            "CREATE TABLE " + TABLE_INCIDENCIAS + " (" +
            COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
            COLUMN_TITULO + " TEXT, " +
            COLUMN_DESCRIPCION + " TEXT, " +
            COLUMN_URGENCIA + " TEXT, " +
            COLUMN_FOTOPATH + " TEXT, " +
            COLUMN_ESTADO + " TEXT, " +
            COLUMN_LATITUD + " REAL, " +
            COLUMN_LONGITUD + " REAL" +
            ");";

    public DbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(TABLE_CREATE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_INCIDENCIAS);
        onCreate(db);
    }
}
