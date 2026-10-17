package com.tecnoserv.web.util;

import java.text.NumberFormat;
import java.util.Locale;

/** Funciones de formato disponibles en las plantillas como ${fmt.moneda(x)}, ${fmt.fecha(x)}, etc. */
public class Fmt {

    private final Locale co = new Locale("es", "CO");

    public String moneda(Object valor) {
        double v = valor instanceof Number n ? n.doubleValue() : 0;
        return "$ " + NumberFormat.getIntegerInstance(co).format(Math.round(v));
    }

    /** Convierte 2026-10-15T14:30:00.123 en 15/10/2026 14:30. */
    public String fecha(Object valor) {
        if (valor == null) {
            return "—";
        }
        String s = valor.toString();
        if (s.length() >= 16) {
            return s.substring(8, 10) + "/" + s.substring(5, 7) + "/" + s.substring(0, 4) + " " + s.substring(11, 16);
        }
        return s;
    }

    public String estado(Object valor) {
        if (valor == null) {
            return "";
        }
        return switch (valor.toString()) {
            case "INGRESADO" -> "Ingresado";
            case "EN_DIAGNOSTICO" -> "En diagnóstico";
            case "ESPERANDO_REPUESTO" -> "Esperando repuesto";
            case "COMPLETADO" -> "Completado";
            case "ENTREGADO" -> "Entregado";
            case "SOLICITADO" -> "Solicitado";
            default -> valor.toString();
        };
    }

    /** Clase de Bootstrap para pintar el estado como una etiqueta de color. */
    public String badge(Object valor) {
        if (valor == null) {
            return "text-bg-secondary";
        }
        return switch (valor.toString()) {
            case "EN_DIAGNOSTICO" -> "text-bg-info";
            case "ESPERANDO_REPUESTO", "SOLICITADO" -> "text-bg-warning";
            case "COMPLETADO" -> "text-bg-primary";
            case "ENTREGADO" -> "text-bg-success";
            default -> "text-bg-secondary";
        };
    }

    public String rol(Object valor) {
        if (valor == null) {
            return "";
        }
        return switch (valor.toString()) {
            case "ADMINISTRADOR" -> "Administrador";
            case "RECEPCIONISTA" -> "Recepcionista";
            case "TECNICO" -> "Técnico";
            case "CLIENTE" -> "Cliente";
            case "PROVEEDOR" -> "Proveedor de repuestos";
            default -> valor.toString();
        };
    }
}
