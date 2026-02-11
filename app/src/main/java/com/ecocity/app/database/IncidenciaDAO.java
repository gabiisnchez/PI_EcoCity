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
 * Esta clase encapsula toda la lógica de interacción con la base de datos para
 * las incidencias,
 * gestionando todas las operaciones CRUD (Crear, Leer, Actualizar, Borrar).
 * 
 * Patrón: DAO (Objeto de Acceso a Datos).
 */
public class IncidenciaDAO {

    private SQLiteDatabase database;
    private DbHelper dbHelper;

    /**
     * Constructor del DAO.
     * Instancia el DbHelper para preparar la conexión.
     * 
     * @param context Contexto de la aplicación.
     */
    public IncidenciaDAO(Context context) {
        dbHelper = new DbHelper(context);
    }

    /**
     * Abre la conexión con la base de datos en modo escritura.
     * Debe llamarse antes de realizar cualquier operación.
     */
    public void open() {
        database = dbHelper.getWritableDatabase();
    }

    /**
     * Cierra la conexión con la base de datos para liberar recursos.
     * Debe llamarse cuando ya no se necesite el DAO (ej. en onDestroy).
     */
    public void close() {
        dbHelper.close();
    }

    /**
     * INSERT: Inserta una nueva incidencia en la base de datos.
     * 
     * @param incidencia Objeto Incidencia con los datos a guardar.
     * @return El ID de la nueva fila insertada o -1 si hubo un error.
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

        // Metodo insert() devuelve el ID de la fila o -1
        return database.insert(DbHelper.TABLE_INCIDENCIAS, null, values);
    }

    /**
     * UPDATE: Actualiza los datos de una incidencia existente.
     * Utiliza el ID de la incidencia para localizar la fila a modificar.
     * 
     * @param incidencia Objeto con los nuevos datos actualizados.
     * @return Número de filas afectadas (debería ser 1 si todo va bien).
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
        // Normalmente no permitimos cambiar el propietario (user_email), así que no se
        // actualiza aquí.
        // values.put(DbHelper.COLUMN_USER_EMAIL, incidencia.getUserEmail());

        // clausula WHERE 'id = ?'
        return database.update(DbHelper.TABLE_INCIDENCIAS, values,
                DbHelper.COLUMN_ID + " = ?",
                new String[] { String.valueOf(incidencia.getId()) });
    }

    /**
     * DELETE: Elimina una incidencia físicamente de la base de datos por su ID.
     * 
     * @param id Identificador único de la incidencia a borrar.
     */
    public void deleteIncidencia(int id) {
        database.delete(DbHelper.TABLE_INCIDENCIAS,
                DbHelper.COLUMN_ID + " = ?",
                new String[] { String.valueOf(id) });
    }

    /**
     * READ (All): Obtiene todas las incidencias con un ordenamiento personalizado
     * complejo.
     * 
     * Criterios de Ordenación:
     * 1. Estado: Priorizamos las activas sobre las resueltas (En proceso >
     * Pendientes > Resuelta).
     * 2. Urgencia: Dentro del mismo estado, priorizamos por gravedad (Alta > Media
     * > Baja).
     * 
     * @return Lista completa de incidencias ordenada estratégicamente.
     */
    public List<Incidencia> getAllIncidencias() {
        List<Incidencia> incidencias = new ArrayList<>();

        // Construcción de la sentencia ORDER BY personalizada usando CASE WHEN de SQL.
        // Esto permite dar un valor numérico a cadenas de texto para ordenar
        // semánticamente.

        // CASE WHEN estado ... END ASC
        String orderBy = "CASE " + DbHelper.COLUMN_ESTADO +
                " WHEN 'En proceso' THEN 1 " + // Máxima prioridad visual
                " WHEN 'Pendiente' THEN 2 " +
                " WHEN 'Resuelta' THEN 3 " + // Al final de la lista
                " ELSE 4 END ASC, " +
                // Segundo criterio: CASE WHEN urgencia ... END ASC
                "CASE " + DbHelper.COLUMN_URGENCIA +
                " WHEN 'Alta' THEN 1 " +
                " WHEN 'Media' THEN 2 " +
                " WHEN 'Baja' THEN 3 " +
                " ELSE 4 END ASC";

        // Ejecutar consulta (SELECT * FROM incidencias ORDER BY ...)
        Cursor cursor = database.query(DbHelper.TABLE_INCIDENCIAS,
                null, null, null, null, null, orderBy);

        // Iterar sobre los resultados (Cursor)
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

                // Recuperar email del usuario creador
                int emailIndex = cursor.getColumnIndex(DbHelper.COLUMN_USER_EMAIL);
                if (emailIndex != -1) {
                    incidencia.setUserEmail(cursor.getString(emailIndex));
                }

                incidencias.add(incidencia);
            } while (cursor.moveToNext());
        }
        cursor.close(); // Siempre cerrar el cursor para evitar fugas de memoria
        return incidencias;
    }

    /**
     * COUNT: Cuenta el número de incidencias aplicando filtros opcionales.
     * Útil para generar estadísticas en el perfil de usuario.
     * 
     * @param userEmail Email del usuario para filtrar (null para contar todas).
     * @param estado    Estado específico para filtrar (null para contar cualquier
     *                  estado).
     * @return Número entero de incidencias que cumplen los criterios.
     */
    public int getIncidenciasCount(String userEmail, String estado) {
        int count = 0;
        String selection = "";
        List<String> argsList = new ArrayList<>();

        // Constuir clausula WHERE dinámicamente

        // 1. Filtro por Usuario
        if (userEmail != null) {
            selection += DbHelper.COLUMN_USER_EMAIL + " = ?";
            argsList.add(userEmail);
        }

        // 2. Filtro por Estado
        if (estado != null) {
            // Añadir 'AND' si ya había una condición previa
            if (!selection.isEmpty())
                selection += " AND ";
            selection += DbHelper.COLUMN_ESTADO + " = ?";
            argsList.add(estado);
        }

        // Convertir lista de argumentos a Array
        String[] selectionArgs = argsList.isEmpty() ? null : argsList.toArray(new String[0]);
        if (selection.isEmpty())
            selection = null; // Si no hay filtros, selection es null (selecciona todo)

        // Ejecutar consulta COUNT(*)
        Cursor cursor = database.query(DbHelper.TABLE_INCIDENCIAS,
                new String[] { "COUNT(*)" }, // Proyección: Solo deulve el conteo
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
