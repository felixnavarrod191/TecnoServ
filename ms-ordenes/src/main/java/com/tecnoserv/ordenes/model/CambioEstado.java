package com.tecnoserv.ordenes.model;

import java.time.LocalDateTime;


/** Entrada del historial de estados de una orden (no se modifica una vez creada). */
public class CambioEstado {
    private Estado estado;
    private LocalDateTime fecha;
    private String usuario;

    public CambioEstado() {
    }

    public CambioEstado(Estado estado, LocalDateTime fecha, String usuario) {
        this.estado = estado;
        this.fecha = fecha;
        this.usuario = usuario;
    }

    public Estado getEstado() {
        return estado;
    }

    public void setEstado(Estado estado) {
        this.estado = estado;
    }

    public LocalDateTime getFecha() {
        return fecha;
    }

    public void setFecha(LocalDateTime fecha) {
        this.fecha = fecha;
    }

    public String getUsuario() {
        return usuario;
    }

    public void setUsuario(String usuario) {
        this.usuario = usuario;
    }
}
