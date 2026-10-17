package com.tecnoserv.web.controller;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.server.ResponseStatusException;

import com.tecnoserv.web.api.Servicios;
import com.tecnoserv.web.seguridad.UsuarioActual;
import com.tecnoserv.web.util.Catalogo;
import com.tecnoserv.web.util.Fmt;
import com.tecnoserv.web.util.M;


/**
 * Órdenes de servicio: listados por rol, creación, detalle y las acciones sobre una orden
 * (asignar técnico, diagnosticar, usar repuestos, registrar horas y avanzar el estado).
 */
@Controller
public class OrdenController {

    private static final Fmt FMT = new Fmt();

    private final Servicios sv;

    public OrdenController(Servicios sv) {
        this.sv = sv;
    }

    // ------------------------------------------------------------------ listados

    @GetMapping("/recepcion/ordenes")
    public String todas(@RequestParam(required = false) String estado, Model model) {
        boolean sinFiltro = estado == null || estado.isBlank();
        model.addAttribute("titulo", "Órdenes de servicio");
        model.addAttribute("ordenes", sinFiltro
                ? sv.ordenes.lista("/api/ordenes") : sv.ordenes.lista("/api/ordenes?estado={e}", estado));
        model.addAttribute("filtro", true);
        model.addAttribute("estadoSel", sinFiltro ? "" : estado);
        model.addAttribute("estados", Catalogo.ESTADOS);
        return "ordenes/lista";
    }

    @GetMapping("/tecnico/ordenes")
    public String delTecnico(@AuthenticationPrincipal UsuarioActual u, Model model) {
        model.addAttribute("titulo", "Mis órdenes asignadas");
        model.addAttribute("ordenes", sv.ordenes.lista("/api/ordenes?tecnicoId={t}", u.getReferenciaId()));
        model.addAttribute("filtro", false);
        return "ordenes/lista";
    }

    @GetMapping("/cliente/ordenes")
    public String delCliente(@AuthenticationPrincipal UsuarioActual u, Model model) {
        model.addAttribute("titulo", "Mis equipos y órdenes");
        model.addAttribute("ordenes", sv.ordenes.lista("/api/ordenes?clienteId={c}", u.getReferenciaId()));
        model.addAttribute("filtro", false);
        return "ordenes/lista";
    }

    // ------------------------------------------------------------------ nueva orden (recepción)

    @GetMapping("/recepcion/ordenes/nueva")
    public String nueva(@RequestParam(required = false) String clienteId, Model model) {
        boolean hayCliente = clienteId != null && !clienteId.isBlank();
        model.addAttribute("clientes", sv.clientes.lista("/api/clientes"));
        model.addAttribute("clienteId", hayCliente ? clienteId : "");
        model.addAttribute("equipos", hayCliente
                ? sv.clientes.lista("/api/equipos?clienteId={c}", clienteId) : List.of());
        return "recepcion/nueva-orden";
    }

    @PostMapping("/recepcion/ordenes")
    public String crear(@RequestParam String clienteId,
                        @RequestParam String equipoId,
                        @RequestParam String tipoReparacion,
                        @RequestParam String fallaReportada,
                        @AuthenticationPrincipal UsuarioActual u,
                        RedirectAttributes ra) {
        Map<String, Object> creada = sv.ordenes.crear("/api/ordenes", M.of("clienteId", clienteId,
                "equipoId", equipoId, "tipoReparacion", tipoReparacion, "fallaReportada", fallaReportada,
                "usuario", u.getNombre()));
        ra.addFlashAttribute("ok", "Orden creada en estado Ingresado");
        return "redirect:/ordenes/" + creada.get("id");
    }

    // ------------------------------------------------------------------ detalle

    @GetMapping("/ordenes/{id}")
    public String detalle(@PathVariable String id, @AuthenticationPrincipal UsuarioActual u, Model model) {
        Map<String, Object> orden = cargar(id, u);
        String estado = String.valueOf(orden.get("estado"));
        int idx = Catalogo.ESTADOS.indexOf(estado);
        String siguiente = idx >= 0 && idx < Catalogo.ESTADOS.size() - 1 ? Catalogo.ESTADOS.get(idx + 1) : null;

        boolean esTecnico = "TECNICO".equals(u.getRol());
        boolean esRecepcion = "RECEPCIONISTA".equals(u.getRol());
        boolean activa = idx >= 0 && idx <= 2;
        boolean enDiagnostico = idx == 1 || idx == 2;

        model.addAttribute("orden", orden);
        model.addAttribute("diagnosticos", sv.ordenes.lista("/api/ordenes/{id}/diagnosticos", id));
        model.addAttribute("siguiente", siguiente);
        model.addAttribute("puedeAvanzar", siguiente != null
                && ((esTecnico && !"ENTREGADO".equals(siguiente)) || (esRecepcion && "ENTREGADO".equals(siguiente))));
        model.addAttribute("puedeAsignar", esRecepcion && activa);
        model.addAttribute("puedeTrabajar", esTecnico && enDiagnostico);
        model.addAttribute("candidatos", esRecepcion && activa
                ? sv.ordenes.lista("/api/ordenes/tecnicos-candidatos?tipoReparacion={t}", orden.get("tipoReparacion"))
                : List.of());
        model.addAttribute("repuestos", esTecnico && enDiagnostico
                ? sv.repuestos.lista("/api/repuestos") : List.of());

        Map<String, Object> costo = null;
        try {
            costo = sv.reportes.obtener("/api/reportes/orden/{id}", id);
        } catch (RuntimeException e) {
            // si el servicio de reportes no responde, la orden igual se muestra
        }
        model.addAttribute("costo", costo);
        return "ordenes/detalle";
    }

    // ------------------------------------------------------------------ acciones

    @PostMapping("/ordenes/{id}/asignar")
    @PreAuthorize("hasRole('RECEPCIONISTA')")
    public String asignar(@PathVariable String id, @RequestParam String tecnicoId,
                          @AuthenticationPrincipal UsuarioActual u, RedirectAttributes ra) {
        cargar(id, u);
        sv.ordenes.actualizar("/api/ordenes/{id}/asignar", M.of("tecnicoId", tecnicoId), id);
        ra.addFlashAttribute("ok", "Técnico asignado");
        return "redirect:/ordenes/" + id;
    }

    @PostMapping("/ordenes/{id}/avanzar")
    @PreAuthorize("hasAnyRole('TECNICO','RECEPCIONISTA')")
    public String avanzar(@PathVariable String id, @AuthenticationPrincipal UsuarioActual u, RedirectAttributes ra) {
        cargar(id, u);
        Map<String, Object> r = sv.ordenes.crear("/api/ordenes/{id}/avanzar",
                M.of("usuario", u.getNombre(), "rol", u.getRol()), id);
        ra.addFlashAttribute("ok", "La orden pasó a: " + FMT.estado(r.get("estado")));
        return "redirect:/ordenes/" + id;
    }

    @PostMapping("/ordenes/{id}/diagnostico")
    @PreAuthorize("hasRole('TECNICO')")
    public String diagnostico(@PathVariable String id,
                              @RequestParam String descripcion,
                              @RequestParam(required = false) String repuestosRequeridos,
                              @AuthenticationPrincipal UsuarioActual u, RedirectAttributes ra) {
        cargar(id, u);
        List<String> requeridos = repuestosRequeridos == null ? List.of()
                : Arrays.stream(repuestosRequeridos.split(",")).map(String::trim).filter(s -> !s.isEmpty()).toList();
        sv.ordenes.crear("/api/ordenes/{id}/diagnostico",
                M.of("descripcion", descripcion, "autor", u.getNombre(), "repuestosRequeridos", requeridos), id);
        ra.addFlashAttribute("ok", "Diagnóstico registrado");
        return "redirect:/ordenes/" + id;
    }

    @PostMapping("/ordenes/{id}/repuestos")
    @PreAuthorize("hasRole('TECNICO')")
    public String repuesto(@PathVariable String id, @RequestParam String repuestoId, @RequestParam int cantidad,
                           @AuthenticationPrincipal UsuarioActual u, RedirectAttributes ra) {
        cargar(id, u);
        sv.ordenes.crear("/api/ordenes/{id}/repuestos", M.of("repuestoId", repuestoId, "cantidad", cantidad), id);
        ra.addFlashAttribute("ok", "Repuesto asociado a la orden y descontado del inventario");
        return "redirect:/ordenes/" + id;
    }

    @PostMapping("/ordenes/{id}/horas")
    @PreAuthorize("hasRole('TECNICO')")
    public String horas(@PathVariable String id, @RequestParam double horas,
                        @AuthenticationPrincipal UsuarioActual u, RedirectAttributes ra) {
        cargar(id, u);
        sv.ordenes.actualizar("/api/ordenes/{id}/horas", M.of("horas", horas), id);
        ra.addFlashAttribute("ok", "Horas registradas");
        return "redirect:/ordenes/" + id;
    }

    // ------------------------------------------------------------------ acceso

    /** Carga la orden y verifica que el usuario tenga derecho a verla (técnico: la suya; cliente: la suya). */
    private Map<String, Object> cargar(String id, UsuarioActual u) {
        Map<String, Object> orden = sv.ordenes.obtener("/api/ordenes/{id}", id);
        boolean ajenaAlTecnico = "TECNICO".equals(u.getRol())
                && !String.valueOf(orden.get("tecnicoId")).equals(u.getReferenciaId());
        boolean ajenaAlCliente = "CLIENTE".equals(u.getRol())
                && !String.valueOf(orden.get("clienteId")).equals(u.getReferenciaId());
        if (ajenaAlTecnico || ajenaAlCliente) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "No tienes acceso a esta orden");
        }
        return orden;
    }
}
