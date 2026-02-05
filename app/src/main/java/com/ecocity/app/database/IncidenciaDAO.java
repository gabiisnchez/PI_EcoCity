package com.ecocity.app.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import com.ecocity.app.model.Incidencia;
import java.util.ArrayList;
import java.util.List;

public class IncidenciaDAO {

    private SQLiteDatabase database;
    private DbHelper dbHelper;

    public IncidenciaDAO(Context context) {
        dbHelper = new DbHelper(context);
    }

    public void open() {
        database = dbHelper.getWritableDatabase();
    }

    public void close() {
        dbHelper.close();
    }

    public long insertIncidencia(Incidencia incidencia) {
        ContentValues values = new ContentValues();
        values.put(DbHelper.COLUMN_TITULO, incidencia.getTitulo());
        values.put(DbHelper.COLUMN_DESCRIPCION, incidencia.getDescripcion());
        values.put(DbHelper.COLUMN_URGENCIA, incidencia.getUrgencia());
        values.put(DbHelper.COLUMN_FOTOPATH, incidencia.getFotoPath());
        values.put(DbHelper.COLUMN_ESTADO, incidencia.getEstado());
        values.put(DbHelper.COLUMN_LATITUD, incidencia.getLatitud());
        values.put(DbHelper.COLUMN_LONGITUD, incidencia.getLongitud());

        return database.insert(DbHelper.TABLE_INCIDENCIAS, null, values);
    }

    public int updateIncidencia(Incidencia incidencia) {
        ContentValues values = new ContentValues();
        values.put(DbHelper.COLUMN_TITULO, incidencia.getTitulo());
        values.put(DbHelper.COLUMN_DESCRIPCION, incidencia.getDescripcion());
        values.put(DbHelper.COLUMN_URGENCIA, incidencia.getUrgencia());
        values.put(DbHelper.COLUMN_FOTOPATH, incidencia.getFotoPath());
        values.put(DbHelper.COLUMN_ESTADO, incidencia.getEstado());
        values.put(DbHelper.COLUMN_LATITUD, incidencia.getLatitud());
        values.put(DbHelper.COLUMN_LONGITUD, incidencia.getLongitud());

        return database.update(DbHelper.TABLE_INCIDENCIAS, values,
                DbHelper.COLUMN_ID + " = ?",
                new String[] { String.valueOf(incidencia.getId()) });
    }

    public void deleteIncidencia(int id) {
        database.delete(DbHelper.TABLE_INCIDENCIAS,
                DbHelper.COLUMN_ID + " = ?",
                new String[] { String.valueOf(id) });
    }

    public List<Incidencia> getAllIncidencias() {
        List<Incidencia> incidencias = new ArrayList<>();
        Cursor cursor = database.query(DbHelper.TABLE_INCIDENCIAS,
                null, null, null, null, null, DbHelper.COLUMN_ID + " DESC");

        if (cursor.moveToFirst()) {
            do {
                Incidencia incidencia = new Incidencia();
                incidencia.setId(cursor.getInt(cursor.getColumnIndexOrThrow(DbHelper.COLUMN_ID)));
                incidencia.setTitulo(cursor.getString(cursor.getColumnIndexOrThrow(DbHelper.COLUMN_TITULO)));

                String descripcion = cursor.getString(cursor.getColumnIndexOrThrow(DbHelper.COLUMN_DESCRIPCION));
                incidencia.setDescripcion(descripcion != null ? descripcion : "");

                String urgencia = cursor.getString(cursor.getColumnIndexOrThrow(DbHelper.COLUMN_URGENCIA));
                incidencia.setUrgencia(urgencia != null ? urgencia : "Baja");

                incidencia.setFotoPath(cursor.getString(cursor.getColumnIndexOrThrow(DbHelper.COLUMN_FOTOPATH)));
                incidencia.setEstado(cursor.getString(cursor.getColumnIndexOrThrow(DbHelper.COLUMN_ESTADO)));
                incidencia.setLatitud(cursor.getDouble(cursor.getColumnIndexOrThrow(DbHelper.COLUMN_LATITUD)));
                incidencia.setLongitud(cursor.getDouble(cursor.getColumnIndexOrThrow(DbHelper.COLUMN_LONGITUD)));

                incidencias.add(incidencia);
            } while (cursor.moveToNext());
        }
        cursor.close();
        return incidencias;
    }

    public int getIncidenciasCount(String estado) {
        int count = 0;
        String selection = null;
        String[] selectionArgs = null;

        if (estado != null) {
            selection = DbHelper.COLUMN_ESTADO + " = ?";
            selectionArgs = new String[] { estado };
        }

        Cursor cursor = database.query(DbHelper.TABLE_INCIDENCIAS,
                new String[] { "COUNT(*)" },
                selection,
                selectionArgs,
                null, null, null);

        if (cursor.moveToFirst()) {
            count = cursor.getInt(0);
        }
        cursor.close();
        return count;
    }
}
