package com.ecocity.app.model;

public class Mensaje {
    private String texto;
    private long timestamp;
    private boolean esUsuario; // true = Usuario, false = Soporte

    public Mensaje(String texto, boolean esUsuario) {
        this.texto = texto;
        this.esUsuario = esUsuario;
        this.timestamp = System.currentTimeMillis();
    }

    public String getTexto() {
        return texto;
    }

    public boolean esUsuario() {
        return esUsuario;
    }

    public long getTimestamp() {
        return timestamp;
    }
}
