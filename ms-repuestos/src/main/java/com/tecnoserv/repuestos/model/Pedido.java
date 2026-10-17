package com.tecnoserv.repuestos.model;

import java.time.LocalDateTime;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

/** Pedido de reposicion de un repuesto a un proveedor. Estados: SOLICITADO -> ENTREGADO. */
@Document(collection = "pedidos")
public class Pedido {

    public static final String SOLICITADO = "SOLICITADO";
    public static final String ENTREGADO = "ENTREGADO";

    @Id
    private String id;

    private String proveedorId;
    private String repuestoId;
    private String repuestoNombre;
    private int cantidad;
    private String estado = SOLICITADO;
    private LocalDateTime fechaSolicitud;
    private LocalDateTime fechaEntrega;
    private String solicitadoPor;

    public Pedido() {
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getProveedorId() {
        return proveedorId;
    }

    public void setProveedorId(String proveedorId) {
        this.proveedorId = proveedorId;
    }

    public String getRepuestoId() {
        return repuestoId;
    }

    public void setRepuestoId(String repuestoId) {
        this.repuestoId = repuestoId;
    }

    public String getRepuestoNombre() {
        return repuestoNombre;
    }

    public void setRepuestoNombre(String repuestoNombre) {
        this.repuestoNombre = repuestoNombre;
    }

    public int getCantidad() {
        return cantidad;
    }

    public void setCantidad(int cantidad) {
        this.cantidad = cantidad;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }

    public LocalDateTime getFechaSolicitud() {
        return fechaSolicitud;
    }

    public void setFechaSolicitud(LocalDateTime fechaSolicitud) {
        this.fechaSolicitud = fechaSolicitud;
    }

    public LocalDateTime getFechaEntrega() {
        return fechaEntrega;
    }

    public void setFechaEntrega(LocalDateTime fechaEntrega) {
        this.fechaEntrega = fechaEntrega;
    }

    public String getSolicitadoPor() {
        return solicitadoPor;
    }

    public void setSolicitadoPor(String solicitadoPor) {
        this.solicitadoPor = solicitadoPor;
    }
}
