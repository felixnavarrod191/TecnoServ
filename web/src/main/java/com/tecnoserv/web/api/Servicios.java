package com.tecnoserv.web.api;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/** Punto único desde el que la web llega a los 5 microservicios. */
@Component
public class Servicios {

    public final Api clientes;
    public final Api tecnicos;
    public final Api repuestos;
    public final Api ordenes;
    public final Api reportes;

    public Servicios(@Value("${servicios.clientes.url}") String clientes,
                     @Value("${servicios.tecnicos.url}") String tecnicos,
                     @Value("${servicios.repuestos.url}") String repuestos,
                     @Value("${servicios.ordenes.url}") String ordenes,
                     @Value("${servicios.reportes.url}") String reportes) {
        this.clientes = new Api(clientes);
        this.tecnicos = new Api(tecnicos);
        this.repuestos = new Api(repuestos);
        this.ordenes = new Api(ordenes);
        this.reportes = new Api(reportes);
    }
}
