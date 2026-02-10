package com.ecocity.app.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import com.ecocity.app.model.Incidencia;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object (DAO) para la entidad Incidencia.
 * Gestiona todas las operaciones CRUD (Crear, Leer, Actualizar, Borrar) en SQLite.
 */
public class IncidenciaDAO {

    private SQLiteDatabase database;
    private DbHelper dbHelper;

    public IncidenciaDAO(Context context) {
        dbHelper = new DbHelper(context);
    }

    /**
     * Abre la conexión con la base de datos en modo escritura.
     */
    public void open() {
        database = dbHelper.getWritableDatabase();
    }

    /**
     * Cierra la conexión con la base de datos.
     */
    public void close() {
        dbHelper.close();
    }

    /**
     * Inserta una nueva incidencia en la base de datos.
     * @param incidencia Objeto Incidencia a insertar.
     * @return El ID de la nueva fila o -1 si hubo error.
     */
    public long insertIncidencia(Incidencia incidencia) {
        ContentValues values = new ContentValues();
        values.put(DbHelper.COLUMN_TITULO, incidencia.getTitulo());
        values.put(DbHelper.COLUMN_DESCRIPCION, incidencia.getDescripcion());
        values.put(DbHelper.COLUMN_URGENCIA, incidencia.getUrgencia());
        values.put(DbHelper.COLUMN_FOTOPATH, incidencia.getFotoPath());
        values.put(DbHelper.COLUMN_ESTADO, incidencia.getEstado());
        values.put(DbHelper.COLUMN_LATITUD, incidencia.getLatitud());
        values.put(DbHelper.COLUMN_LONGITUD, incidencia.getLongitud());
        values.put(DbHelper.COLUMN_USER_EMAIL, incidencia.getUserEmail());

        return database.insert(DbHelper.TABLE_INCIDENCIAS, null, values);
    }

    /**
     * Actualiza una incidencia existente.
     * @param incidencia Objeto con los nuevos datos.
     * @return Número de filas afectadas.
     */
    public int updateIncidencia(Incidencia incidencia) {
        ContentValues values = new ContentValues();
        values.put(DbHelper.COLUMN_TITULO, incidencia.getTitulo());
        values.put(DbHelper.COLUMN_DESCRIPCION, incidencia.getDescripcion());
        values.put(DbHelper.COLUMN_URGENCIA, incidencia.getUrgencia());
        values.put(DbHelper.COLUMN_FOTOPATH, incidencia.getFotoPath());
        values.put(DbHelper.COLUMN_ESTADO, incidencia.getEstado());
        values.put(DbHelper.COLUMN_LATITUD, incidencia.getLatitud());
        values.put(DbHelper.COLUMN_LONGITUD, incidencia.getLongitud());
        // Normalmente no cambiamos el propietario, pero se asegura por consistencia
        // values.put(DbHelper.COLUMN_USER_EMAIL, incidencia.getUserEmail());

        return database.update(DbHelper.TABLE_INCIDENCIAS, values,
                DbHelper.COLUMN_ID + " = ?",
                new String[] { String.valueOf(incidencia.getId()) });
    }

    /**
     * Elimina una incidencia por su ID.
     * @param id Identificador de la incidencia a borrar.
     */
    public void deleteIncidencia(int id) {
        database.delete(DbHelper.TABLE_INCIDENCIAS,
                DbHelper.COLUMN_ID + " = ?",
                new String[] { String.valueOf(id) });
    }

    /**
     * Obtiene todas las incidencias ordenadas por Estado y Urgencia.
     * @return Lista completa de incidencias.
     */
    public List<Incidencia> getAllIncidencias() {
        List<Incidencia> incidencias = new ArrayList<>();

        // Orden Personalizado:
        // 1. Estado: En proceso > Pendientes > Resuelta
        // 2. Urgencia: Alta > Media > Baja

        String orderBy = "CASE " + DbHelper.COLUMN_ESTADO +
                " WHEN 'En proceso' THEN 1 " +
                " WHEN 'Pendiente' THEN 2 " +
                " WHEN 'Resuelta' THEN 3 " +
                " ELSE 4 END ASC, " +
                "CASE " + DbHelper.COLUMN_URGENCIA +
                " WHEN 'Alta' THEN 1 " +
                " WHEN 'Media' THEN 2 " +
                " WHEN 'Baja' THEN 3 " +
                " ELSE 4 END ASC";

        Cursor cursor = database.query(DbHelper.TABLE_INCIDENCIAS,
                null, null, null, null, null, orderBy);

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

                // Recuperar email del usuario propietario
                int emailIndex = cursor.getColumnIndex(DbHelper.COLUMN_USER_EMAIL);
                if (emailIndex != -1) {
                    incidencia.setUserEmail(cursor.getString(emailIndex));
                }

                incidencias.add(incidencia);
            } while (cursor.moveToNext());
        }
        cursor.close();
        return incidencias;
    }

    /**
     * Cuenta incidencias filtrando por usuario y/o estado.
     * @param userEmail Email del usuario (opcional).
     * @param estado Estado de la incidencia (opcional).
     * @return Número de incidencias encontradas.
     */
    public int getIncidenciasCount(String userEmail, String estado) {
        int count = 0;
        String selection = "";
        List<String> argsList = new ArrayList<>();

        // Filtrar por Email
        if (userEmail != null) {
            selection += DbHelper.COLUMN_USER_EMAIL + " = ?";
            argsList.add(userEmail);
        }

        // Filtrar por Estado
        if (estado != null) {
            if (!selection.isEmpty())
                selection += " AND ";
            selection += DbHelper.COLUMN_ESTADO + " = ?";
            argsList.add(estado);
        }

        String[] selectionArgs = argsList.isEmpty() ? null : argsList.toArray(new String[0]);
        if (selection.isEmpty())
            selection = null;

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
