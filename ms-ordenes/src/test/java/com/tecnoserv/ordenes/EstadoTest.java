package com.tecnoserv.ordenes;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import com.tecnoserv.ordenes.model.Estado;

/** Prueba de la regla 2: el flujo de estados es estricto y lineal. */
class EstadoTest {

    @Test
    void elFlujoAvanzaSoloAlSiguienteEstado() {
        assertEquals(Estado.EN_DIAGNOSTICO, Estado.INGRESADO.siguiente());
        assertEquals(Estado.ESPERANDO_REPUESTO, Estado.EN_DIAGNOSTICO.siguiente());
        assertEquals(Estado.COMPLETADO, Estado.ESPERANDO_REPUESTO.siguiente());
        assertEquals(Estado.ENTREGADO, Estado.COMPLETADO.siguiente());
    }

    @Test
    void entregadoEsElEstadoFinal() {
        assertNull(Estado.ENTREGADO.siguiente());
    }

    @Test
    void soloLosTresPrimerosEstadosSeConsideranActivos() {
        assertTrue(Estado.INGRESADO.activa());
        assertTrue(Estado.EN_DIAGNOSTICO.activa());
        assertTrue(Estado.ESPERANDO_REPUESTO.activa());
        assertFalse(Estado.COMPLETADO.activa());
        assertFalse(Estado.ENTREGADO.activa());
    }
}
