package com.tecnoserv.ordenes.model;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "diagnosticos")
public class Diagnostico {

    @Id
    private String id;
    private String ordenId;
    private String descripcion;
    private List<String> repuestosRequeridos = new ArrayList<>();
    private String autor;
    private LocalDateTime fecha;

    public Diagnostico() {
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getOrdenId() {
        return ordenId;
    }

    public void setOrdenId(String ordenId) {
        this.ordenId = ordenId;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public List<String> getRepuestosRequeridos() {
        return repuestosRequeridos;
    }

    public void setRepuestosRequeridos(List<String> repuestosRequeridos) {
        this.repuestosRequeridos = repuestosRequeridos;
    }

    public String getAutor() {
        return autor;
    }

    public void setAutor(String autor) {
        this.autor = autor;
    }

    public LocalDateTime getFecha() {
        return fecha;
    }

    public void setFecha(LocalDateTime fecha) {
        this.fecha = fecha;
    }
}
