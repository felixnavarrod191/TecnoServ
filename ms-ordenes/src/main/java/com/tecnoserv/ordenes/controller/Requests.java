package com.tecnoserv.ordenes.controller;

import java.util.List;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;

/** Cuerpos de las solicitudes que recibe el servicio de ordenes. */
public final class Requests {

    private Requests() {
    }

    public record Crear(
            @NotBlank(message = "El cliente es obligatorio") String clienteId,
            @NotBlank(message = "El equipo es obligatorio") String equipoId,
            @NotBlank(message = "El tipo de reparacion es obligatorio") String tipoReparacion,
            @NotBlank(message = "La falla reportada es obligatoria") String fallaReportada,
            String usuario) {
    }

    public record Asignar(@NotBlank(message = "El tecnico es obligatorio") String tecnicoId) {
    }

    public record Avanzar(
            @NotBlank(message = "El usuario es obligatorio") String usuario,
            @NotBlank(message = "El rol es obligatorio") String rol) {
    }

    public record DiagnosticoReq(
            @NotBlank(message = "La descripcion del diagnostico es obligatoria") String descripcion,
            String autor,
            List<String> repuestosRequeridos) {
    }

    public record RepuestoReq(
            @NotBlank(message = "El repuesto es obligatorio") String repuestoId,
            @Positive(message = "La cantidad debe ser mayor que cero") int cantidad) {
    }

    public record Horas(@PositiveOrZero(message = "Las horas no pueden ser negativas") double horas) {
    }
}
