package com.tecnoserv.ordenes.model;


/** Repuesto consumido en una orden. El precio se copia al usarlo para no alterar ordenes cerradas. */
public class RepuestoUsado {
    private String repuestoId;
    private String nombre;
    private int cantidad;
    private double precioUnitario;

    public RepuestoUsado() {
    }

    public RepuestoUsado(String repuestoId, String nombre, int cantidad, double precioUnitario) {
        this.repuestoId = repuestoId;
        this.nombre = nombre;
        this.cantidad = cantidad;
        this.precioUnitario = precioUnitario;
    }

    public String getRepuestoId() {
        return repuestoId;
    }

    public void setRepuestoId(String repuestoId) {
        this.repuestoId = repuestoId;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public int getCantidad() {
        return cantidad;
    }

    public void setCantidad(int cantidad) {
        this.cantidad = cantidad;
    }

    public double getPrecioUnitario() {
        return precioUnitario;
    }

    public void setPrecioUnitario(double precioUnitario) {
        this.precioUnitario = precioUnitario;
    }
}
