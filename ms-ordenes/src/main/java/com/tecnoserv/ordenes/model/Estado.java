package com.tecnoserv.ordenes.model;

/**
 * Estados de una orden de servicio. El orden de declaracion ES el flujo estricto:
 * Ingresado -> En diagnostico -> Esperando repuesto -> Completado -> Entregado.
 */
public enum Estado {
    INGRESADO,
    EN_DIAGNOSTICO,
    ESPERANDO_REPUESTO,
    COMPLETADO,
    ENTREGADO;

    /** Unico estado al que se puede avanzar desde este; null si ya es el ultimo. */
    public Estado siguiente() {
        return ordinal() == values().length - 1 ? null : values()[ordinal() + 1];
    }

    /** Una orden es "activa" (cuenta para la carga del tecnico) hasta que se completa. */
    public boolean activa() {
        return this == INGRESADO || this == EN_DIAGNOSTICO || this == ESPERANDO_REPUESTO;
    }
}
