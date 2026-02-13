package com.ecocity.app.model;

import java.io.Serializable;

/**
 * Modelo de Usuario para Firestore.
 * Representa los datos de un usuario registrado en la aplicación.
 */
public class Usuario implements Serializable {

    private String id; // UID de Firebase Auth
    private String nombre;
    private String apellidos;
    private String email;
    private String fotoUrl; // URL de la foto de perfil (opcional)
    private long fechaRegistro; // Timestamp de registro

    // Constructor vacío requerido por Firestore
    public Usuario() {
    }

    public Usuario(String id, String nombre, String apellidos, String email) {
        this.id = id;
        this.nombre = nombre;
        this.apellidos = apellidos;
        this.email = email;
        this.fechaRegistro = System.currentTimeMillis();
    }

    // --- Getters y Setters ---

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getApellidos() {
        return apellidos;
    }

    public void setApellidos(String apellidos) {
        this.apellidos = apellidos;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getFotoUrl() {
        return fotoUrl;
    }

    public void setFotoUrl(String fotoUrl) {
        this.fotoUrl = fotoUrl;
    }

    public long getFechaRegistro() {
        return fechaRegistro;
    }

    public void setFechaRegistro(long fechaRegistro) {
        this.fechaRegistro = fechaRegistro;
    }
}
