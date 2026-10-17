package com.tecnoserv.web.controller;

import java.net.URI;
import java.util.List;

import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.client.RestClientException;
import org.springframework.web.servlet.support.RequestContextUtils;

import com.tecnoserv.web.api.ApiError;
import com.tecnoserv.web.util.Catalogo;
import com.tecnoserv.web.util.Fmt;

import jakarta.servlet.http.HttpServletRequest;

/**
 * Datos comunes a todas las vistas y manejo central de errores:
 * si un microservicio rechaza una operación, el mensaje se muestra al usuario.
 */
@ControllerAdvice
public class ManejadorGlobal {

    private static final Fmt FMT = new Fmt();

    @ModelAttribute("fmt")
    public Fmt fmt() {
        return FMT;
    }

    @ModelAttribute("tipos")
    public List<String> tipos() {
        return Catalogo.TIPOS_REPARACION;
    }

    @ExceptionHandler(ApiError.class)
    public String apiError(ApiError e, HttpServletRequest request, Model model) {
        return responder(e.getMessage(), request, model);
    }

    @ExceptionHandler(RestClientException.class)
    public String sinConexion(RestClientException e, HttpServletRequest request, Model model) {
        return responder("No se pudo conectar con uno de los microservicios. Verifica que estén en ejecución.", request, model);
    }

    private String responder(String mensaje, HttpServletRequest request, Model model) {
        if ("GET".equals(request.getMethod())) {
            model.addAttribute("fmt", FMT);
            model.addAttribute("mensaje", mensaje);
            return "error-servicio";
        }
        // Operaciones POST: se vuelve a la página anterior mostrando el mensaje de error
        RequestContextUtils.getOutputFlashMap(request).put("error", mensaje);
        return "redirect:" + paginaAnterior(request);
    }

    private String paginaAnterior(HttpServletRequest request) {
        String referer = request.getHeader("Referer");
        if (referer != null) {
            try {
                URI uri = URI.create(referer);
                String path = uri.getPath() == null || uri.getPath().isEmpty() ? "/" : uri.getPath();
                return uri.getQuery() == null ? path : path + "?" + uri.getQuery();
            } catch (IllegalArgumentException ignorado) {
                // referer inválido: se vuelve al inicio
            }
        }
        return "/";
    }
}
