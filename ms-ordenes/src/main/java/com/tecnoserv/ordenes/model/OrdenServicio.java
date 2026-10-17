package com.tecnoserv.ordenes.model;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "ordenes")
public class OrdenServicio {

    @Id
    private String id;

    private String clienteId;
    private String clienteNombre;
    private String equipoId;
    private String equipoDescripcion;

    private String tipoReparacion;
    private String fallaReportada;

    private String tecnicoId;
    private String tecnicoNombre;
    /** Tarifa del tecnico al momento de asignarlo (para calcular la mano de obra). */
    private double tarifaHora;

    private Estado estado = Estado.INGRESADO;
    private LocalDateTime fechaIngreso;
    private double horasTrabajadas;

    private List<CambioEstado> historial = new ArrayList<>();
    private List<RepuestoUsado> repuestosUsados = new ArrayList<>();

    public OrdenServicio() {
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

    public String getClienteNombre() {
        return clienteNombre;
    }

    public void setClienteNombre(String clienteNombre) {
        this.clienteNombre = clienteNombre;
    }

    public String getEquipoId() {
        return equipoId;
    }

    public void setEquipoId(String equipoId) {
        this.equipoId = equipoId;
    }

    public String getEquipoDescripcion() {
        return equipoDescripcion;
    }

    public void setEquipoDescripcion(String equipoDescripcion) {
        this.equipoDescripcion = equipoDescripcion;
    }

    public String getTipoReparacion() {
        return tipoReparacion;
    }

    public void setTipoReparacion(String tipoReparacion) {
        this.tipoReparacion = tipoReparacion;
    }

    public String getFallaReportada() {
        return fallaReportada;
    }

    public void setFallaReportada(String fallaReportada) {
        this.fallaReportada = fallaReportada;
    }

    public String getTecnicoId() {
        return tecnicoId;
    }

    public void setTecnicoId(String tecnicoId) {
        this.tecnicoId = tecnicoId;
    }

    public String getTecnicoNombre() {
        return tecnicoNombre;
    }

    public void setTecnicoNombre(String tecnicoNombre) {
        this.tecnicoNombre = tecnicoNombre;
    }

    public double getTarifaHora() {
        return tarifaHora;
    }

    public void setTarifaHora(double tarifaHora) {
        this.tarifaHora = tarifaHora;
    }

    public Estado getEstado() {
        return estado;
    }

    public void setEstado(Estado estado) {
        this.estado = estado;
    }

    public LocalDateTime getFechaIngreso() {
        return fechaIngreso;
    }

    public void setFechaIngreso(LocalDateTime fechaIngreso) {
        this.fechaIngreso = fechaIngreso;
    }

    public double getHorasTrabajadas() {
        return horasTrabajadas;
    }

    public void setHorasTrabajadas(double horasTrabajadas) {
        this.horasTrabajadas = horasTrabajadas;
    }

    public List<CambioEstado> getHistorial() {
        return historial;
    }

    public void setHistorial(List<CambioEstado> historial) {
        this.historial = historial;
    }

    public List<RepuestoUsado> getRepuestosUsados() {
        return repuestosUsados;
    }

    public void setRepuestosUsados(List<RepuestoUsado> repuestosUsados) {
        this.repuestosUsados = repuestosUsados;
    }
}
