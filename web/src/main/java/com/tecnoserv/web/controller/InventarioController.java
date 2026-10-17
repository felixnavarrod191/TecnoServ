package com.tecnoserv.web.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.tecnoserv.web.api.Servicios;
import com.tecnoserv.web.seguridad.UsuarioActual;
import com.tecnoserv.web.util.M;


/** Administración de repuestos, proveedores y pedidos (RF-06 y RF-09). */
@Controller
@RequestMapping("/admin")
public class InventarioController {

    private final Servicios sv;

    public InventarioController(Servicios sv) {
        this.sv = sv;
    }

    // ------------------------------------------------------------------ repuestos

    @GetMapping("/repuestos")
    public String repuestos(@RequestParam(required = false) String editar, Model model) {
        List<Map<String, Object>> proveedores = sv.repuestos.lista("/api/proveedores");
        model.addAttribute("repuestos", sv.repuestos.lista("/api/repuestos"));
        model.addAttribute("proveedores", proveedores);
        model.addAttribute("nombresProveedor", nombres(proveedores));
        model.addAttribute("form", editar == null || editar.isBlank()
                ? M.of("id", null, "codigo", null, "nombre", null, "stock", null, "stockMinimo", null,
                        "precioUnitario", null, "proveedorId", null)
                : sv.repuestos.obtener("/api/repuestos/{id}", editar));
        return "admin/repuestos";
    }

    @PostMapping("/repuestos")
    public String guardarRepuesto(@RequestParam(required = false) String id,
                                  @RequestParam String codigo,
                                  @RequestParam String nombre,
                                  @RequestParam int stock,
                                  @RequestParam int stockMinimo,
                                  @RequestParam double precioUnitario,
                                  @RequestParam(required = false) String proveedorId,
                                  RedirectAttributes ra) {
        Map<String, Object> cuerpo = M.of("codigo", codigo, "nombre", nombre, "stock", stock,
                "stockMinimo", stockMinimo, "precioUnitario", precioUnitario,
                "proveedorId", proveedorId == null || proveedorId.isBlank() ? null : proveedorId);
        if (id == null || id.isBlank()) {
            sv.repuestos.crear("/api/repuestos", cuerpo);
            ra.addFlashAttribute("ok", "Repuesto creado");
        } else {
            sv.repuestos.actualizar("/api/repuestos/{id}", cuerpo, id);
            ra.addFlashAttribute("ok", "Repuesto actualizado");
        }
        return "redirect:/admin/repuestos";
    }

    @PostMapping("/repuestos/{id}/eliminar")
    public String eliminarRepuesto(@PathVariable String id, RedirectAttributes ra) {
        sv.repuestos.eliminar("/api/repuestos/{id}", id);
        ra.addFlashAttribute("ok", "Repuesto eliminado");
        return "redirect:/admin/repuestos";
    }

    // ------------------------------------------------------------------ proveedores

    @GetMapping("/proveedores")
    public String proveedores(@RequestParam(required = false) String editar, Model model) {
        model.addAttribute("proveedores", sv.repuestos.lista("/api/proveedores"));
        model.addAttribute("form", editar == null || editar.isBlank()
                ? M.of("id", null, "nombre", null, "contacto", null, "correo", null)
                : sv.repuestos.obtener("/api/proveedores/{id}", editar));
        return "admin/proveedores";
    }

    @PostMapping("/proveedores")
    public String guardarProveedor(@RequestParam(required = false) String id,
                                   @RequestParam String nombre,
                                   @RequestParam(required = false) String contacto,
                                   @RequestParam(required = false) String correo,
                                   RedirectAttributes ra) {
        Map<String, Object> cuerpo = M.of("nombre", nombre, "contacto", contacto,
                "correo", correo == null || correo.isBlank() ? null : correo);
        if (id == null || id.isBlank()) {
            sv.repuestos.crear("/api/proveedores", cuerpo);
            ra.addFlashAttribute("ok", "Proveedor creado");
        } else {
            sv.repuestos.actualizar("/api/proveedores/{id}", cuerpo, id);
            ra.addFlashAttribute("ok", "Proveedor actualizado");
        }
        return "redirect:/admin/proveedores";
    }

    @PostMapping("/proveedores/{id}/eliminar")
    public String eliminarProveedor(@PathVariable String id, RedirectAttributes ra) {
        sv.repuestos.eliminar("/api/proveedores/{id}", id);
        ra.addFlashAttribute("ok", "Proveedor eliminado");
        return "redirect:/admin/proveedores";
    }

    // ------------------------------------------------------------------ pedidos

    @GetMapping("/pedidos")
    public String pedidos(Model model) {
        model.addAttribute("pedidos", sv.repuestos.lista("/api/pedidos"));
        model.addAttribute("repuestos", sv.repuestos.lista("/api/repuestos"));
        model.addAttribute("nombresProveedor", nombres(sv.repuestos.lista("/api/proveedores")));
        return "admin/pedidos";
    }

    @PostMapping("/pedidos")
    public String crearPedido(@RequestParam String repuestoId, @RequestParam int cantidad,
                              @AuthenticationPrincipal UsuarioActual u, RedirectAttributes ra) {
        sv.repuestos.crear("/api/pedidos", M.of("repuestoId", repuestoId, "cantidad", cantidad,
                "solicitadoPor", u.getNombre()));
        ra.addFlashAttribute("ok", "Pedido registrado");
        return "redirect:/admin/pedidos";
    }

    private static Map<String, String> nombres(List<Map<String, Object>> proveedores) {
        Map<String, String> nombres = new HashMap<>();
        for (Map<String, Object> p : proveedores) {
            nombres.put(String.valueOf(p.get("id")), String.valueOf(p.get("nombre")));
        }
        return nombres;
    }
}
