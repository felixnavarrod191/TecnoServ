package com.tecnoserv.web.controller;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

import com.tecnoserv.web.seguridad.UsuarioActual;

@Controller
public class HomeController {

    @GetMapping("/login")
    public String login() {
        return "login";
    }

    /** Cada rol aterriza en su pantalla principal. */
    @GetMapping("/")
    public String inicio(@AuthenticationPrincipal UsuarioActual u) {
        return switch (u.getRol()) {
            case "ADMINISTRADOR" -> "redirect:/admin";
            case "RECEPCIONISTA" -> "redirect:/recepcion/ordenes";
            case "TECNICO" -> "redirect:/tecnico/ordenes";
            case "CLIENTE" -> "redirect:/cliente/ordenes";
            case "PROVEEDOR" -> "redirect:/proveedor";
            default -> "redirect:/login?error";
        };
    }
}
