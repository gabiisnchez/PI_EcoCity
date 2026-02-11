package com.ecocity.app.model;

/**
 * Clase Modelo que representa a un Usuario registrado en la aplicación.
 */
public class User {
    // Identificador único del usuario
    private int id;

    // Nombre completo del usuario
    private String name;

    // Correo electrónico (usado para login y vinculación con incidencias)
    private String email;

    // Contraseña del usuario
    private String password;

    /**
     * Constructor para crear un nuevo usuario (antes de tener ID de base de datos).
     * 
     * @param name     Nombre del usuario.
     * @param email    Email del usuario.
     * @param password Contraseña.
     */
    public User(String name, String email, String password) {
        this.name = name;
        this.email = email;
        this.password = password;
    }

    /**
     * Constructor completo (cuando ya se recupera de la base de datos con ID).
     * 
     * @param id       ID de base de datos.
     * @param name     Nombre del usuario.
     * @param email    Email del usuario.
     * @param password Contraseña.
     */
    public User(int id, String name, String email, String password) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.password = password;
    }

    // --- Getters y Setters ---

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
