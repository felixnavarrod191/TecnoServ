package com.tecnoserv.web.controller;

import java.util.List;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.tecnoserv.web.api.Servicios;
import com.tecnoserv.web.seguridad.UsuarioActual;
import com.tecnoserv.web.util.M;


/** Proveedor de repuestos: ve SOLO sus repuestos, registra pedidos y entregas (HU-13). */
@Controller
@RequestMapping("/proveedor")
public class ProveedorController {

    private final Servicios sv;

    public ProveedorController(Servicios sv) {
        this.sv = sv;
    }

    @GetMapping
    public String panel(@AuthenticationPrincipal UsuarioActual u, Model model) {
        model.addAttribute("repuestos", sv.repuestos.lista("/api/repuestos?proveedorId={p}", u.getReferenciaId()));
        model.addAttribute("pedidos", sv.repuestos.lista("/api/pedidos?proveedorId={p}", u.getReferenciaId()));
        return "proveedor/panel";
    }

    @PostMapping("/pedidos")
    public String crearPedido(@RequestParam String repuestoId, @RequestParam int cantidad,
                              @AuthenticationPrincipal UsuarioActual u, RedirectAttributes ra) {
        Map<String, Object> repuesto = sv.repuestos.obtener("/api/repuestos/{id}", repuestoId);
        if (!u.getReferenciaId().equals(String.valueOf(repuesto.get("proveedorId")))) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Ese repuesto no es suministrado por ti");
        }
        sv.repuestos.crear("/api/pedidos", M.of("repuestoId", repuestoId, "cantidad", cantidad,
                "solicitadoPor", u.getNombre()));
        ra.addFlashAttribute("ok", "Pedido registrado");
        return "redirect:/proveedor";
    }

    @PostMapping("/pedidos/{id}/entregar")
    public String entregar(@PathVariable String id, @AuthenticationPrincipal UsuarioActual u, RedirectAttributes ra) {
        List<Map<String, Object>> propios = sv.repuestos.lista("/api/pedidos?proveedorId={p}", u.getReferenciaId());
        boolean esSuyo = propios.stream().anyMatch(p -> id.equals(String.valueOf(p.get("id"))));
        if (!esSuyo) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Ese pedido no corresponde a tu cuenta");
        }
        sv.repuestos.accion("/api/pedidos/{id}/entregar", id);
        ra.addFlashAttribute("ok", "Entrega registrada: el stock del repuesto aumentó");
        return "redirect:/proveedor";
    }
}
