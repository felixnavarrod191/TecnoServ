package com.tecnoserv.reportes;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

/** Comprueba la formula de la regla 3 con un ejemplo a mano. */
class CostoTest {

    @Test
    void costoTotalEsManoDeObraMasRepuestos() {
        double manoObra = 2.5 * 25000;            // 2,5 horas a 25.000 la hora
        double repuestos = 1 * 180000 + 2 * 60000; // 1 pantalla + 2 baterias
        assertEquals(362500.0, manoObra + repuestos, 0.001);
    }
}
