package com.tecnoserv.web.util;

import java.util.List;

/** Listas fijas que usan los formularios. */
public final class Catalogo {

    private Catalogo() {
    }

    /** Tipos de reparación: son las especialidades que puede tener un técnico. */
    public static final List<String> TIPOS_REPARACION =
            List.of("Computadores", "Celulares", "Electrodomésticos", "Impresoras", "Redes");

    public static final List<String> ESTADOS =
            List.of("INGRESADO", "EN_DIAGNOSTICO", "ESPERANDO_REPUESTO", "COMPLETADO", "ENTREGADO");

    public static final List<String> ROLES =
            List.of("ADMINISTRADOR", "RECEPCIONISTA", "TECNICO", "CLIENTE", "PROVEEDOR");
}
