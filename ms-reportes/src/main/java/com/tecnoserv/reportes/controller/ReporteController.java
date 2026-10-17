package com.tecnoserv.reportes.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.tecnoserv.reportes.model.Reporte;
import com.tecnoserv.reportes.model.Resumen;
import com.tecnoserv.reportes.service.ReporteService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/api/reportes")
@Tag(name = "Reportes", description = "Reportes de costo total (mano de obra + repuestos)")
public class ReporteController {

    private final ReporteService service;

    public ReporteController(ReporteService service) {
        this.service = service;
    }

    @GetMapping("/orden/{ordenId}")
    @Operation(summary = "Costo total de una orden")
    public Reporte porOrden(@PathVariable String ordenId) {
        return service.reporteDeOrden(ordenId);
    }

    @GetMapping("/resumen")
    @Operation(summary = "Costo de todas las ordenes y total general")
    public Resumen resumen() {
        return service.resumen();
    }
}
