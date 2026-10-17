package com.tecnoserv.repuestos.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.PositiveOrZero;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "repuestos")
public class Repuesto {

    @Id
    private String id;

    @NotBlank(message = "El codigo es obligatorio")
    @Indexed(unique = true)
    private String codigo;

    @NotBlank(message = "El nombre es obligatorio")
    private String nombre;

    @PositiveOrZero(message = "El stock no puede ser negativo")
    private int stock;

    /** Si el stock baja de este valor, el repuesto se considera por reponer. */
    @PositiveOrZero(message = "El stock minimo no puede ser negativo")
    private int stockMinimo;

    @PositiveOrZero(message = "El precio no puede ser negativo")
    private double precioUnitario;

    /** Proveedor que suministra este repuesto (opcional). */
    private String proveedorId;

    public Repuesto() {
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getCodigo() {
        return codigo;
    }

    public void setCodigo(String codigo) {
        this.codigo = codigo;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public int getStock() {
        return stock;
    }

    public void setStock(int stock) {
        this.stock = stock;
    }

    public int getStockMinimo() {
        return stockMinimo;
    }

    public void setStockMinimo(int stockMinimo) {
        this.stockMinimo = stockMinimo;
    }

    public double getPrecioUnitario() {
        return precioUnitario;
    }

    public void setPrecioUnitario(double precioUnitario) {
        this.precioUnitario = precioUnitario;
    }

    public String getProveedorId() {
        return proveedorId;
    }

    public void setProveedorId(String proveedorId) {
        this.proveedorId = proveedorId;
    }
}
