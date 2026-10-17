package com.tecnoserv.web.util;

import java.util.HashMap;
import java.util.Map;

/** Construye un mapa a partir de pares clave, valor: M.of("nombre", "Ana", "edad", 20). */
public final class M {

    private M() {
    }

    public static Map<String, Object> of(Object... pares) {
        Map<String, Object> mapa = new HashMap<>();
        for (int i = 0; i + 1 < pares.length; i += 2) {
            mapa.put(String.valueOf(pares[i]), pares[i + 1]);
        }
        return mapa;
    }

    public static int entero(Object valor) {
        return valor instanceof Number n ? n.intValue() : 0;
    }
}
