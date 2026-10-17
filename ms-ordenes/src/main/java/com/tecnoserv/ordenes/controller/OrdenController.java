package com.tecnoserv.ordenes.controller;

import java.util.List;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.tecnoserv.ordenes.model.Diagnostico;
import com.tecnoserv.ordenes.model.Estado;
import com.tecnoserv.ordenes.model.OrdenServicio;
import com.tecnoserv.ordenes.service.OrdenService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/ordenes")
@Tag(name = "Ordenes de servicio", description = "Ciclo completo de una orden: ingreso, asignacion, diagnostico, repuestos y entrega")
public class OrdenController {

    private final OrdenService service;

    public OrdenController(OrdenService service) {
        this.service = service;
    }

    @GetMapping
    @Operation(summary = "Lista ordenes (filtros opcionales por cliente, tecnico y estado)")
    public List<OrdenServicio> listar(@RequestParam(required = false) String clienteId,
                                      @RequestParam(required = false) String tecnicoId,
                                      @RequestParam(required = false) Estado estado) {
        return service.listar(clienteId, tecnicoId, estado);
    }

    @GetMapping("/{id}")
    public OrdenServicio obtener(@PathVariable String id) {
        return service.obtener(id);
    }

    @GetMapping("/tecnicos-candidatos")
    @Operation(summary = "Tecnicos disponibles y compatibles con un tipo de reparacion")
    public List<Map<String, Object>> candidatos(@RequestParam String tipoReparacion) {
        return service.candidatos(tipoReparacion);
    }

    @GetMapping("/{id}/diagnosticos")
    public List<Diagnostico> diagnosticos(@PathVariable String id) {
        return service.diagnosticos(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Crea una orden de servicio en estado Ingresado")
    public OrdenServicio crear(@Valid @RequestBody Requests.Crear req) {
        return service.crear(req);
    }

    @PutMapping("/{id}/asignar")
    @Operation(summary = "Asigna un tecnico (disponible, con la especialidad y con cupo)")
    public OrdenServicio asignar(@PathVariable String id, @Valid @RequestBody Requests.Asignar req) {
        return service.asignar(id, req.tecnicoId());
    }

    @PostMapping("/{id}/avanzar")
    @Operation(summary = "Avanza la orden al estado inmediatamente siguiente (flujo estricto)")
    public OrdenServicio avanzar(@PathVariable String id, @Valid @RequestBody Requests.Avanzar req) {
        return service.avanzar(id, req.usuario(), req.rol());
    }

    @PostMapping("/{id}/diagnostico")
    @ResponseStatus(HttpStatus.CREATED)
    public Diagnostico diagnostico(@PathVariable String id, @Valid @RequestBody Requests.DiagnosticoReq req) {
        return service.registrarDiagnostico(id, req);
    }

    @PostMapping("/{id}/repuestos")
    @Operation(summary = "Asocia un repuesto a la orden y descuenta stock")
    public OrdenServicio repuesto(@PathVariable String id, @Valid @RequestBody Requests.RepuestoReq req) {
        return service.agregarRepuesto(id, req);
    }

    @PutMapping("/{id}/horas")
    @Operation(summary = "Registra las horas de mano de obra trabajadas")
    public OrdenServicio horas(@PathVariable String id, @Valid @RequestBody Requests.Horas req) {
        return service.fijarHoras(id, req.horas());
    }
}
