package com.tecnoserv.web.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import com.tecnoserv.web.api.Servicios;


/** Reporte de costo total (mano de obra + repuestos) de todas las órdenes (RF-07). */
@Controller
public class ReportesController {

    private final Servicios sv;

    public ReportesController(Servicios sv) {
        this.sv = sv;
    }

    @GetMapping("/reportes")
    public String reportes(Model model) {
        model.addAttribute("resumen", sv.reportes.obtener("/api/reportes/resumen"));
        return "reportes/resumen";
    }
}
