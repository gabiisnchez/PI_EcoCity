package com.ecocity.app.model;

/**
 * Clase Modelo que representa un mensaje de chat.
 * Se utiliza en la funcionalidad de comunicación entre Usuario y Soporte.
 */
public class Mensaje {
    // Contenido del mensaje
    private String texto;

    // Marca de tiempo de cuando se creó el mensaje
    private long timestamp;

    // Indica si el mensaje fue enviado por el usuario (true) o por el soporte
    // (false)
    private boolean esUsuario; // true = Usuario, false = Soporte

    /**
     * Constructor del mensaje.
     * 
     * @param texto     Contenido del mensaje.
     * @param esUsuario true si es del usuario, false si es del sistema/soporte.
     */
    public Mensaje(String texto, boolean esUsuario) {
        this.texto = texto;
        this.esUsuario = esUsuario;
        // Asigna la hora actual del sistema al momento de crear el objeto
        this.timestamp = System.currentTimeMillis();
    }

    public String getTexto() {
        return texto;
    }

    /**
     * @return true si el mensaje es del usuario actual, false si es recibido.
     */
    public boolean esUsuario() {
        return esUsuario;
    }

    public long getTimestamp() {
        return timestamp;
    }
}
