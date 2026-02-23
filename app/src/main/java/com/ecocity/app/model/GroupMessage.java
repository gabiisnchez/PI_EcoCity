package com.ecocity.app.model;

import java.io.Serializable;

/**
 * Clase que representa un mensaje en el Chat Grupal de una Incidencia.
 */
public class GroupMessage implements Serializable {

    private String texto;
    private String senderEmail;
    private boolean isMine; // True si el mensaje fue enviado por el usuario actual

    public GroupMessage(String texto, String senderEmail, boolean isMine) {
        this.texto = texto;
        this.senderEmail = senderEmail;
        this.isMine = isMine;
    }

    public String getTexto() {
        return texto;
    }

    public String getSenderEmail() {
        return senderEmail;
    }

    public boolean isMine() {
        return isMine;
    }
}
