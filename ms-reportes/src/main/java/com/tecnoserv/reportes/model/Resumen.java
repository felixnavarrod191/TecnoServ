package com.tecnoserv.reportes.model;

import java.util.List;

/** Reporte de todas las ordenes y el total general facturable. */
public record Resumen(List<Reporte> ordenes, double totalGeneral) {
}
