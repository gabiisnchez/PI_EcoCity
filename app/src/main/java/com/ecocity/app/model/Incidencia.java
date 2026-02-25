package com.ecocity.app.model;

import java.io.Serializable;

/**
 * Clase Modelo que representa una Incidencia en el sistema EcoCity.
 * Implementa Serializable para poder pasar objetos de esta clase entre
 * Actividades a través de Intents.
 */
public class Incidencia implements Serializable {

    private static final long serialVersionUID = 1L;

    // Identificador único de la incidencia en la base de datos (Firestore usa
    // String)
    private String id;

    // Título breve del problema
    private String titulo;

    // Descripción detallada del problema reportado
    private String descripcion;

    // Nivel de urgencia: Alta, Media, Baja
    private String urgencia;

    // Ruta del archivo de la foto adjunta
    private String fotoPath;

    // Estado actual de la incidencia: Pendiente, En proceso, Resuelta
    private String estado;

    // Coordenadas geográficas
    private double latitud;
    private double longitud;

    // Email del usuario que reportó la incidencia
    private String userEmail;

    // --- Campos de Sincronización Local (SQLite) ---
    // ID autonumérico local
    private long localId = -1;
    // Bandera para saber si ya se subió a Firestore
    private int isSynced = 0; // 0 = pendiente, 1 = sincronizado
    // Acción pendiente de sincronizar: INSERT, UPDATE, DELETE
    private String syncAction = "INSERT";

    /**
     * Constructor vacío requerido para Firestore.
     */
    public Incidencia() {
    }

    /**
     * Constructor principal para crear una nueva incidencia.
     *
     * @param titulo      Título de la incidencia.
     * @param descripcion Descripción del problema.
     * @param urgencia    Nivel de urgencia.
     * @param fotoPath    Ruta de la imagen adjunta.
     * @param latitud     Latitud geográfica.
     * @param longitud    Longitud geográfica.
     */
    public Incidencia(String titulo, String descripcion, String urgencia, String fotoPath, double latitud,
            double longitud) {
        this.titulo = titulo;
        this.descripcion = descripcion;
        this.urgencia = urgencia;
        this.fotoPath = fotoPath;
        this.latitud = latitud;
        this.longitud = longitud;
        this.estado = "Pendiente"; // Estado inicial por defecto
    }

    // --- Getters y Setters ---

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTitulo() {
        return titulo;
    }

    public void setTitulo(String titulo) {
        this.titulo = titulo;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public String getUrgencia() {
        return urgencia;
    }

    public void setUrgencia(String urgencia) {
        this.urgencia = urgencia;
    }

    public String getFotoPath() {
        return fotoPath;
    }

    public void setFotoPath(String fotoPath) {
        this.fotoPath = fotoPath;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }

    public double getLatitud() {
        return latitud;
    }

    public void setLatitud(double latitud) {
        this.latitud = latitud;
    }

    public double getLongitud() {
        return longitud;
    }

    public void setLongitud(double longitud) {
        this.longitud = longitud;
    }

    public String getUserEmail() {
        return userEmail;
    }

    public void setUserEmail(String userEmail) {
        this.userEmail = userEmail;
    }

    public long getLocalId() {
        return localId;
    }

    public void setLocalId(long localId) {
        this.localId = localId;
    }

    public int getIsSynced() {
        return isSynced;
    }

    public void setIsSynced(int isSynced) {
        this.isSynced = isSynced;
    }

    public String getSyncAction() {
        return syncAction;
    }

    public void setSyncAction(String syncAction) {
        this.syncAction = syncAction;
    }
}
