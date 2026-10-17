package com.tecnoserv.web.controller;

import java.util.Map;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.tecnoserv.web.api.Servicios;
import com.tecnoserv.web.util.M;


/** Recepción: registro de clientes y de sus equipos (RF-01). */
@Controller
@RequestMapping("/recepcion/clientes")
public class RecepcionController {

    private final Servicios sv;

    public RecepcionController(Servicios sv) {
        this.sv = sv;
    }

    @GetMapping
    public String clientes(@RequestParam(required = false) String editar, Model model) {
        model.addAttribute("clientes", sv.clientes.lista("/api/clientes"));
        model.addAttribute("form", editar == null || editar.isBlank()
                ? M.of("id", null, "nombre", null, "documento", null, "telefono", null, "correo", null)
                : sv.clientes.obtener("/api/clientes/{id}", editar));
        return "recepcion/clientes";
    }

    @PostMapping
    public String guardarCliente(@RequestParam(required = false) String id,
                                 @RequestParam String nombre,
                                 @RequestParam String documento,
                                 @RequestParam(required = false) String telefono,
                                 @RequestParam(required = false) String correo,
                                 RedirectAttributes ra) {
        Map<String, Object> cuerpo = M.of("nombre", nombre, "documento", documento, "telefono", telefono,
                "correo", correo == null || correo.isBlank() ? null : correo);
        if (id == null || id.isBlank()) {
            sv.clientes.crear("/api/clientes", cuerpo);
            ra.addFlashAttribute("ok", "Cliente registrado");
        } else {
            sv.clientes.actualizar("/api/clientes/{id}", cuerpo, id);
            ra.addFlashAttribute("ok", "Cliente actualizado");
        }
        return "redirect:/recepcion/clientes";
    }

    @PostMapping("/{id}/eliminar")
    public String eliminarCliente(@PathVariable String id, RedirectAttributes ra) {
        sv.clientes.eliminar("/api/clientes/{id}", id);
        ra.addFlashAttribute("ok", "Cliente eliminado");
        return "redirect:/recepcion/clientes";
    }

    @GetMapping("/{id}")
    public String detalle(@PathVariable String id, Model model) {
        model.addAttribute("cliente", sv.clientes.obtener("/api/clientes/{id}", id));
        model.addAttribute("equipos", sv.clientes.lista("/api/equipos?clienteId={c}", id));
        model.addAttribute("ordenes", sv.ordenes.lista("/api/ordenes?clienteId={c}", id));
        return "recepcion/cliente-detalle";
    }

    @PostMapping("/{id}/equipos")
    public String agregarEquipo(@PathVariable String id,
                                @RequestParam String tipo,
                                @RequestParam(required = false) String marca,
                                @RequestParam(required = false) String modelo,
                                @RequestParam String serial,
                                @RequestParam(required = false) String descripcionFalla,
                                RedirectAttributes ra) {
        sv.clientes.crear("/api/equipos", M.of("clienteId", id, "tipo", tipo, "marca", marca, "modelo", modelo,
                "serial", serial, "descripcionFalla", descripcionFalla));
        ra.addFlashAttribute("ok", "Equipo registrado");
        return "redirect:/recepcion/clientes/" + id;
    }

    @PostMapping("/{id}/equipos/{equipoId}/eliminar")
    public String eliminarEquipo(@PathVariable String id, @PathVariable String equipoId, RedirectAttributes ra) {
        sv.clientes.eliminar("/api/equipos/{id}", equipoId);
        ra.addFlashAttribute("ok", "Equipo eliminado");
        return "redirect:/recepcion/clientes/" + id;
    }
}
