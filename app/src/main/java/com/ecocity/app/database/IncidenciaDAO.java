package com.ecocity.app.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.ecocity.app.model.Incidencia;

import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object (DAO) para la entidad Incidencia con arquitectura
 * Offline-First.
 * Prioriza SQLite para lecturas/escrituras y sincroniza con Firestore en
 * segundo plano.
 */
public class IncidenciaDAO {

    private static final String TAG = "IncidenciaDAO";
    private com.google.firebase.firestore.FirebaseFirestore firestoreDB;
    private static final String COLLECTION_NAME = "incidencias_v2";

    // SQLite
    private DbHelper dbHelper;
    private SQLiteDatabase sqLiteDb;

    /**
     * Interfaz para recibir resultados asíncronos.
     */
    public interface FirestoreCallback {
        void onSuccess(String result);

        void onFailure(Exception e);

        void onDataLoaded(List<Incidencia> incidencias);
    }

    public interface CountCallback {
        void onCountLoaded(int count);
    }

    public IncidenciaDAO(Context context) {
        firestoreDB = com.google.firebase.firestore.FirebaseFirestore.getInstance();
        dbHelper = new DbHelper(context);
    }

    public void open() {
        sqLiteDb = dbHelper.getWritableDatabase();
    }

    public void close() {
        if (dbHelper != null) {
            dbHelper.close();
        }
    }

    // --- MÉTODOS OFFLINE-FIRST (SQLite -> Firestore) ---

    /**
     * INSERT: Inserta primero en SQLite y luego intenta subir a Firestore.
     */
    public void insertIncidencia(final Incidencia incidencia, final FirestoreCallback callback) {
        incidencia.setIsSynced(0);
        incidencia.setSyncAction("INSERT");

        long localId = insertIntoSQLite(incidencia);

        if (localId != -1) {
            incidencia.setLocalId(localId);

            // Intentar sincronizar con Firestore
            firestoreDB.collection(COLLECTION_NAME)
                    .add(incidencia)
                    .addOnSuccessListener(documentReference -> {
                        String firestoreId = documentReference.getId();
                        incidencia.setId(firestoreId);
                        // Confirmar en Firestore con el ID dentro del doc
                        documentReference.set(incidencia);

                        // Actualizar SQLite como sincronizado
                        markAsSyncedInSQLite(localId, firestoreId);

                        if (callback != null)
                            callback.onSuccess(firestoreId);
                    })
                    .addOnFailureListener(e -> {
                        Log.e(TAG, "No se pudo sincronizar INSERT. Se intentará luego. Error: " + e.getMessage());
                        if (callback != null)
                            callback.onFailure(e);
                    });
        } else {
            if (callback != null)
                callback.onFailure(new Exception("Error al insertar en SQLite local"));
        }
    }

    /**
     * UPDATE: Actualiza primero en SQLite y luego intenta actualizar en Firestore.
     */
    public void updateIncidencia(final Incidencia incidencia, final FirestoreCallback callback) {
        incidencia.setIsSynced(0);
        incidencia.setSyncAction("UPDATE");

        int rows = updateInSQLite(incidencia);

        if (rows > 0) {
            // Intentar sincronizar con Firestore (solo si ya tenía ID de Firestore)
            if (incidencia.getId() != null && !incidencia.getId().isEmpty()) {
                firestoreDB.collection(COLLECTION_NAME).document(incidencia.getId())
                        .set(incidencia)
                        .addOnSuccessListener(aVoid -> {
                            markAsSyncedInSQLite(incidencia.getLocalId(), incidencia.getId());
                            if (callback != null)
                                callback.onSuccess("Actualizado en la nube");
                        })
                        .addOnFailureListener(e -> {
                            Log.e(TAG, "No se pudo sincronizar UPDATE. Se intentará luego. Error: " + e.getMessage());
                            if (callback != null)
                                callback.onFailure(e);
                        });
            } else {
                // Si aún no tenía ID de firestore (se creó offline y no se ha sincronizado),
                // dejamos pendiente para la sincronización principal
                if (callback != null)
                    callback.onSuccess("Actualizado localmente (pendiente nube)");
            }
        } else {
            if (callback != null)
                callback.onFailure(new Exception("Error al actualizar en SQLite local"));
        }
    }

    /**
     * DELETE: Borra de Firestore primero si es posible, o marca para borrar en
     * SQLite.
     * Si no tiene ID de Firestore aún, la borra físicamente.
     */
    public void deleteIncidencia(final Incidencia incidencia, final FirestoreCallback callback) {
        if (incidencia.getId() == null || incidencia.getId().isEmpty()) {
            // Nunca se subió a Firestore, borrar físicamente directo de SQLite
            int rows = deleteFromSQLite(incidencia.getLocalId());
            if (callback != null) {
                if (rows > 0)
                    callback.onSuccess("Eliminado localmente");
                else
                    callback.onFailure(new Exception("No se pudo eliminar localmente"));
            }
            return;
        }

        // Marcar para borrar en local
        incidencia.setIsSynced(0);
        incidencia.setSyncAction("DELETE");
        updateInSQLite(incidencia);

        // Intentar borrar en Firestore
        firestoreDB.collection(COLLECTION_NAME).document(incidencia.getId())
                .delete()
                .addOnSuccessListener(aVoid -> {
                    // Si se borra de la nube, borrar físicamente en local
                    deleteFromSQLite(incidencia.getLocalId());
                    if (callback != null)
                        callback.onSuccess("Eliminado de la nube y local");
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "No se pudo sincronizar DELETE. Se intentará luego. Error: " + e.getMessage());
                    if (callback != null)
                        callback.onFailure(e);
                });
    }

    // Sobrecarga para mantener firma antigua
    public void deleteIncidencia(String id, FirestoreCallback callback) {
        // En un esquema offline-first, es mejor pasar el objeto completo para saber el
        // localId
        // Por compatibilidad de código previo, asumimos que este método solo se llama
        // si ya viene de la lista
        callback.onFailure(new Exception(
                "use deleteIncidencia(Incidencia, Callback) en su lugar o implemente búsqueda por string"));
    }

    /**
     * READ (All): Obtiene localmente desde SQLite priorizando modo offline.
     */
    public void getAllIncidencias(final FirestoreCallback callback) {
        List<Incidencia> lista = getAllFromSQLite();
        // Filtrar las que están marcadas como DELETE
        List<Incidencia> activas = new ArrayList<>();
        for (Incidencia i : lista) {
            if (!"DELETE".equals(i.getSyncAction())) {
                activas.add(i);
            }
        }

        sortIncidencias(activas);
        if (callback != null)
            callback.onDataLoaded(activas);
    }

    // --- OPERACIONES SQLITE BASE ---

    private long insertIntoSQLite(Incidencia inc) {
        if (sqLiteDb == null || !sqLiteDb.isOpen())
            return -1;

        ContentValues values = new ContentValues();
        values.put(DbHelper.COLUMN_FIRESTORE_ID, inc.getId());
        values.put(DbHelper.COLUMN_TITULO, inc.getTitulo());
        values.put(DbHelper.COLUMN_DESCRIPCION, inc.getDescripcion());
        values.put(DbHelper.COLUMN_URGENCIA, inc.getUrgencia());
        values.put(DbHelper.COLUMN_FOTOPATH, inc.getFotoPath());
        values.put(DbHelper.COLUMN_ESTADO, inc.getEstado());
        values.put(DbHelper.COLUMN_LATITUD, inc.getLatitud());
        values.put(DbHelper.COLUMN_LONGITUD, inc.getLongitud());
        values.put(DbHelper.COLUMN_USER_EMAIL, inc.getUserEmail());
        values.put(DbHelper.COLUMN_IS_SYNCED, inc.getIsSynced());
        values.put(DbHelper.COLUMN_SYNC_ACTION, inc.getSyncAction());

        return sqLiteDb.insert(DbHelper.TABLE_INCIDENCIAS, null, values);
    }

    private int updateInSQLite(Incidencia inc) {
        if (sqLiteDb == null || !sqLiteDb.isOpen() || inc.getLocalId() == -1)
            return 0;

        ContentValues values = new ContentValues();
        values.put(DbHelper.COLUMN_FIRESTORE_ID, inc.getId());
        values.put(DbHelper.COLUMN_TITULO, inc.getTitulo());
        values.put(DbHelper.COLUMN_DESCRIPCION, inc.getDescripcion());
        values.put(DbHelper.COLUMN_URGENCIA, inc.getUrgencia());
        values.put(DbHelper.COLUMN_FOTOPATH, inc.getFotoPath());
        values.put(DbHelper.COLUMN_ESTADO, inc.getEstado());
        values.put(DbHelper.COLUMN_LATITUD, inc.getLatitud());
        values.put(DbHelper.COLUMN_LONGITUD, inc.getLongitud());
        values.put(DbHelper.COLUMN_USER_EMAIL, inc.getUserEmail());
        values.put(DbHelper.COLUMN_IS_SYNCED, inc.getIsSynced());
        values.put(DbHelper.COLUMN_SYNC_ACTION, inc.getSyncAction());

        return sqLiteDb.update(DbHelper.TABLE_INCIDENCIAS, values,
                DbHelper.COLUMN_LOCAL_ID + "=?", new String[] { String.valueOf(inc.getLocalId()) });
    }

    private int deleteFromSQLite(long localId) {
        if (sqLiteDb == null || !sqLiteDb.isOpen())
            return 0;
        return sqLiteDb.delete(DbHelper.TABLE_INCIDENCIAS,
                DbHelper.COLUMN_LOCAL_ID + "=?", new String[] { String.valueOf(localId) });
    }

    private void markAsSyncedInSQLite(long localId, String firestoreId) {
        if (sqLiteDb == null || !sqLiteDb.isOpen())
            return;
        ContentValues values = new ContentValues();
        values.put(DbHelper.COLUMN_IS_SYNCED, 1);
        values.put(DbHelper.COLUMN_FIRESTORE_ID, firestoreId);
        sqLiteDb.update(DbHelper.TABLE_INCIDENCIAS, values,
                DbHelper.COLUMN_LOCAL_ID + "=?", new String[] { String.valueOf(localId) });
    }

    private List<Incidencia> getAllFromSQLite() {
        List<Incidencia> lista = new ArrayList<>();
        if (sqLiteDb == null || !sqLiteDb.isOpen())
            return lista;

        Cursor cursor = sqLiteDb.query(DbHelper.TABLE_INCIDENCIAS, null, null, null, null, null, null);

        if (cursor.moveToFirst()) {
            do {
                Incidencia inc = new Incidencia();
                inc.setLocalId(cursor.getLong(cursor.getColumnIndexOrThrow(DbHelper.COLUMN_LOCAL_ID)));
                inc.setId(cursor.getString(cursor.getColumnIndexOrThrow(DbHelper.COLUMN_FIRESTORE_ID)));
                inc.setTitulo(cursor.getString(cursor.getColumnIndexOrThrow(DbHelper.COLUMN_TITULO)));
                inc.setDescripcion(cursor.getString(cursor.getColumnIndexOrThrow(DbHelper.COLUMN_DESCRIPCION)));
                inc.setUrgencia(cursor.getString(cursor.getColumnIndexOrThrow(DbHelper.COLUMN_URGENCIA)));
                inc.setFotoPath(cursor.getString(cursor.getColumnIndexOrThrow(DbHelper.COLUMN_FOTOPATH)));
                inc.setEstado(cursor.getString(cursor.getColumnIndexOrThrow(DbHelper.COLUMN_ESTADO)));
                inc.setLatitud(cursor.getDouble(cursor.getColumnIndexOrThrow(DbHelper.COLUMN_LATITUD)));
                inc.setLongitud(cursor.getDouble(cursor.getColumnIndexOrThrow(DbHelper.COLUMN_LONGITUD)));
                inc.setUserEmail(cursor.getString(cursor.getColumnIndexOrThrow(DbHelper.COLUMN_USER_EMAIL)));
                inc.setIsSynced(cursor.getInt(cursor.getColumnIndexOrThrow(DbHelper.COLUMN_IS_SYNCED)));
                inc.setSyncAction(cursor.getString(cursor.getColumnIndexOrThrow(DbHelper.COLUMN_SYNC_ACTION)));

                lista.add(inc);
            } while (cursor.moveToNext());
        }
        cursor.close();
        return lista;
    }

    // --- LÓGICA DE SINCRONIZACIÓN EN SEGUNDO PLANO ---

    /**
     * Sincroniza todas las incidencias pendientes.
     * Idealmente se llama desde un NetworkCallback.
     */
    public void syncWithFirestore() {
        if (sqLiteDb == null || !sqLiteDb.isOpen())
            return;

        Log.d(TAG, "Iniciando sincronización SQLite -> Firestore");

        // Obtener pendietes
        Cursor cursor = sqLiteDb.query(DbHelper.TABLE_INCIDENCIAS, null,
                DbHelper.COLUMN_IS_SYNCED + "=0", null, null, null, null);

        List<Incidencia> pendientes = new ArrayList<>();
        if (cursor.moveToFirst()) {
            do {
                Incidencia inc = new Incidencia();
                inc.setLocalId(cursor.getLong(cursor.getColumnIndexOrThrow(DbHelper.COLUMN_LOCAL_ID)));
                inc.setId(cursor.getString(cursor.getColumnIndexOrThrow(DbHelper.COLUMN_FIRESTORE_ID)));
                inc.setTitulo(cursor.getString(cursor.getColumnIndexOrThrow(DbHelper.COLUMN_TITULO)));
                inc.setDescripcion(cursor.getString(cursor.getColumnIndexOrThrow(DbHelper.COLUMN_DESCRIPCION)));
                inc.setUrgencia(cursor.getString(cursor.getColumnIndexOrThrow(DbHelper.COLUMN_URGENCIA)));
                inc.setFotoPath(cursor.getString(cursor.getColumnIndexOrThrow(DbHelper.COLUMN_FOTOPATH)));
                inc.setEstado(cursor.getString(cursor.getColumnIndexOrThrow(DbHelper.COLUMN_ESTADO)));
                inc.setLatitud(cursor.getDouble(cursor.getColumnIndexOrThrow(DbHelper.COLUMN_LATITUD)));
                inc.setLongitud(cursor.getDouble(cursor.getColumnIndexOrThrow(DbHelper.COLUMN_LONGITUD)));
                inc.setUserEmail(cursor.getString(cursor.getColumnIndexOrThrow(DbHelper.COLUMN_USER_EMAIL)));
                inc.setIsSynced(cursor.getInt(cursor.getColumnIndexOrThrow(DbHelper.COLUMN_IS_SYNCED)));
                inc.setSyncAction(cursor.getString(cursor.getColumnIndexOrThrow(DbHelper.COLUMN_SYNC_ACTION)));
                pendientes.add(inc);
            } while (cursor.moveToNext());
        }
        cursor.close();

        for (Incidencia inc : pendientes) {
            String act = inc.getSyncAction();
            if ("INSERT".equals(act)) {
                firestoreDB.collection(COLLECTION_NAME).add(inc).addOnSuccessListener(doc -> {
                    inc.setId(doc.getId());
                    doc.set(inc);
                    markAsSyncedInSQLite(inc.getLocalId(), doc.getId());
                });
            } else if ("UPDATE".equals(act)) {
                if (inc.getId() != null) {
                    firestoreDB.collection(COLLECTION_NAME).document(inc.getId()).set(inc).addOnSuccessListener(v -> {
                        markAsSyncedInSQLite(inc.getLocalId(), inc.getId());
                    });
                }
            } else if ("DELETE".equals(act)) {
                if (inc.getId() != null) {
                    firestoreDB.collection(COLLECTION_NAME).document(inc.getId()).delete().addOnSuccessListener(v -> {
                        deleteFromSQLite(inc.getLocalId());
                    });
                } else {
                    deleteFromSQLite(inc.getLocalId());
                }
            }
        }
    }

    // --- MÉTODOS AUXILIARES Y COUNT ---

    private void sortIncidencias(List<Incidencia> lista) {
        java.util.Collections.sort(lista, new java.util.Comparator<Incidencia>() {
            @Override
            public int compare(Incidencia i1, Incidencia i2) {
                int s1 = getStatusPriority(i1.getEstado());
                int s2 = getStatusPriority(i2.getEstado());
                if (s1 != s2)
                    return Integer.compare(s1, s2);
                int u1 = getUrgencyPriority(i1.getUrgencia());
                int u2 = getUrgencyPriority(i2.getUrgencia());
                return Integer.compare(u1, u2);
            }
        });
    }

    private int getStatusPriority(String status) {
        if (status == null)
            return 4;
        switch (status) {
            case "En proceso":
                return 1;
            case "Pendiente":
                return 2;
            case "Resuelta":
                return 3;
            default:
                return 4;
        }
    }

    private int getUrgencyPriority(String urgency) {
        if (urgency == null)
            return 4;
        switch (urgency) {
            case "Alta":
                return 1;
            case "Media":
                return 2;
            case "Baja":
                return 3;
            default:
                return 4;
        }
    }

    /**
     * COUNT: Cuenta incidencias activas (no DELETE) en Local.
     */
    public void getIncidenciasCount(String userEmail, String estado, CountCallback callback) {
        if (sqLiteDb == null || !sqLiteDb.isOpen()) {
            if (callback != null)
                callback.onCountLoaded(0);
            return;
        }

        StringBuilder selection = new StringBuilder(DbHelper.COLUMN_SYNC_ACTION + " != 'DELETE'");
        List<String> argsList = new ArrayList<>();

        if (userEmail != null) {
            selection.append(" AND ").append(DbHelper.COLUMN_USER_EMAIL).append(" = ?");
            argsList.add(userEmail);
        }
        if (estado != null) {
            selection.append(" AND ").append(DbHelper.COLUMN_ESTADO).append(" = ?");
            argsList.add(estado);
        }

        Cursor cursor = sqLiteDb.query(DbHelper.TABLE_INCIDENCIAS, null,
                selection.toString(), argsList.toArray(new String[0]), null, null, null);

        int count = cursor.getCount();
        cursor.close();
        if (callback != null)
            callback.onCountLoaded(count);
    }
}
