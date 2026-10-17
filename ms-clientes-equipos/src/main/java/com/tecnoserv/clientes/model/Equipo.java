package com.tecnoserv.clientes.model;

import jakarta.validation.constraints.NotBlank;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "equipos")
public class Equipo {

    @Id
    private String id;

    @NotBlank(message = "El cliente es obligatorio")
    private String clienteId;

    @NotBlank(message = "El tipo de equipo es obligatorio")
    private String tipo;

    private String marca;
    private String modelo;

    @NotBlank(message = "El serial es obligatorio")
    @Indexed(unique = true)
    private String serial;

    private String descripcionFalla;

    public Equipo() {
    }

    public Equipo(String id, String clienteId, String tipo, String marca, String modelo, String serial, String descripcionFalla) {
        this.id = id;
        this.clienteId = clienteId;
        this.tipo = tipo;
        this.marca = marca;
        this.modelo = modelo;
        this.serial = serial;
        this.descripcionFalla = descripcionFalla;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getClienteId() {
        return clienteId;
    }

    public void setClienteId(String clienteId) {
        this.clienteId = clienteId;
    }

    public String getTipo() {
        return tipo;
    }

    public void setTipo(String tipo) {
        this.tipo = tipo;
    }

    public String getMarca() {
        return marca;
    }

    public void setMarca(String marca) {
        this.marca = marca;
    }

    public String getModelo() {
        return modelo;
    }

    public void setModelo(String modelo) {
        this.modelo = modelo;
    }

    public String getSerial() {
        return serial;
    }

    public void setSerial(String serial) {
        this.serial = serial;
    }

    public String getDescripcionFalla() {
        return descripcionFalla;
    }

    public void setDescripcionFalla(String descripcionFalla) {
        this.descripcionFalla = descripcionFalla;
    }
}
