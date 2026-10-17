package com.tecnoserv.web.api;

/** Error devuelto por un microservicio (con el mensaje que se muestra al usuario). */
public class ApiError extends RuntimeException {
    public ApiError(String mensaje) {
        super(mensaje);
    }
}
