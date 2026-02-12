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

    private com.google.firebase.firestore.FirebaseFirestore db;
    private static final String COLLECTION_NAME = "incidencias";

    /**
     * Interfaz para recibir resultados asíncronos de Firestore.
     */
    public interface FirestoreCallback {
        void onSuccess(String result); // Para inserts/updates

        void onFailure(Exception e);

        void onDataLoaded(List<Incidencia> incidencias); // Para lecturas
    }

    // Interfaz simple para conteos
    public interface CountCallback {
        void onCountLoaded(int count);
    }

    public IncidenciaDAO(Context context) {
        // Inicializar Firestore
        db = com.google.firebase.firestore.FirebaseFirestore.getInstance();
    }

    public void open() {
        // No es necesario abrir conexión explícita en Firestore
    }

    public void close() {
        // No es necesario cerrar conexión explícita en Firestore
    }

    /**
     * INSERT: Inserta una nueva incidencia en la colección 'incidencias'.
     */
    public void insertIncidencia(Incidencia incidencia, FirestoreCallback callback) {
        // Generar ID único automáticamente si no existe (aunque Firestore lo hace al
        // usar add,
        // aquí lo generamos para tener referencia en el objeto)
        // Usaremos .add() que genera el ID autmáticamente

        db.collection(COLLECTION_NAME)
                .add(incidencia)
                .addOnSuccessListener(documentReference -> {
                    // Actualizar el objeto con el ID generado en Firestore
                    incidencia.setId(documentReference.getId());
                    documentReference.set(incidencia); // Re-guardar con el ID dentro del objeto (opcional pero útil)
                    callback.onSuccess(documentReference.getId());
                })
                .addOnFailureListener(e -> {
                    callback.onFailure(e);
                });
    }

    /**
     * UPDATE: Actualiza los datos de una incidencia existente.
     */
    public void updateIncidencia(Incidencia incidencia, FirestoreCallback callback) {
        if (incidencia.getId() == null) {
            callback.onFailure(new Exception("ID de incidencia nulo"));
            return;
        }

        db.collection(COLLECTION_NAME).document(incidencia.getId())
                .set(incidencia)
                .addOnSuccessListener(aVoid -> callback.onSuccess("Actualizado"))
                .addOnFailureListener(e -> callback.onFailure(e));
    }

    /**
     * DELETE: Elimina una incidencia por su ID.
     */
    public void deleteIncidencia(String id, FirestoreCallback callback) {
        db.collection(COLLECTION_NAME).document(id)
                .delete()
                .addOnSuccessListener(aVoid -> callback.onSuccess("Eliminado"))
                .addOnFailureListener(e -> callback.onFailure(e));
    }

    /**
     * READ (All): Obtiene todas las incidencias.
     * El ordenamiento complejo lo haremos en cliente (Java) o índices compuestos en
     * Firestore.
     * Por simplicidad inicial, descargaremos y ordenaremos en memoria (si la lista
     * no es gigante).
     */
    public void getAllIncidencias(FirestoreCallback callback) {
        db.collection(COLLECTION_NAME)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<Incidencia> lista = new ArrayList<>();
                    for (com.google.firebase.firestore.DocumentSnapshot doc : queryDocumentSnapshots) {
                        Incidencia i = doc.toObject(Incidencia.class);
                        if (i != null) {
                            i.setId(doc.getId()); // Asegurar ID
                            lista.add(i);
                        }
                    }
                    // Ordenamiento en cliente (Memoria) para replicar la lógica compleja de SQL
                    sortIncidencias(lista);
                    callback.onDataLoaded(lista);
                })
                .addOnFailureListener(e -> callback.onFailure(e));
    }

    /**
     * Ordena la lista de incidencias según la lógica de negocio:
     * 1. Estado: En proceso > Pendiente > Resuelta
     * 2. Urgencia: Alta > Media > Baja
     */
    private void sortIncidencias(List<Incidencia> lista) {
        java.util.Collections.sort(lista, new java.util.Comparator<Incidencia>() {
            @Override
            public int compare(Incidencia i1, Incidencia i2) {
                // 1. Estado
                int s1 = getStatusPriority(i1.getEstado());
                int s2 = getStatusPriority(i2.getEstado());
                if (s1 != s2)
                    return Integer.compare(s1, s2);

                // 2. Urgencia
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
     * COUNT: Cuenta incidencias con filtros.
     */
    public void getIncidenciasCount(String userEmail, String estado, CountCallback callback) {
        com.google.firebase.firestore.Query query = db.collection(COLLECTION_NAME);

        if (userEmail != null) {
            query = query.whereEqualTo("userEmail", userEmail);
        }
        if (estado != null) {
            query = query.whereEqualTo("estado", estado);
        }

        // Usamos count() aggregation de Firestore (más eficiente que descargar
        // documentos)
        // Requiere AggregateQuerySnapshot (disponible en BOM recientes)
        // Si hay error en versión antigua, fallback a size()
        query.get().addOnSuccessListener(queryDocumentSnapshots -> {
            callback.onCountLoaded(queryDocumentSnapshots.size());
        }).addOnFailureListener(e -> {
            callback.onCountLoaded(0); // Fail safe
        });
    }
}
