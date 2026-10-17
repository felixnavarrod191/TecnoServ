package com.tecnoserv.web.controller;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.tecnoserv.web.api.ApiError;
import com.tecnoserv.web.api.Servicios;
import com.tecnoserv.web.demo.DemoService;
import com.tecnoserv.web.seguridad.Usuario;
import com.tecnoserv.web.seguridad.UsuarioActual;
import com.tecnoserv.web.seguridad.UsuarioRepository;
import com.tecnoserv.web.util.Catalogo;
import com.tecnoserv.web.util.M;


/** Panel del administrador: resumen, técnicos y usuarios. */
@Controller
@RequestMapping("/admin")
public class AdminController {

    private final Servicios sv;
    private final UsuarioRepository usuarios;
    private final PasswordEncoder encoder;
    private final DemoService demo;

    public AdminController(Servicios sv, UsuarioRepository usuarios, PasswordEncoder encoder, DemoService demo) {
        this.sv = sv;
        this.usuarios = usuarios;
        this.encoder = encoder;
        this.demo = demo;
    }

    @GetMapping
    public String panel(Model model) {
        List<Map<String, Object>> ordenes = sv.ordenes.lista("/api/ordenes");
        Map<String, Long> porEstado = new LinkedHashMap<>();
        for (String e : Catalogo.ESTADOS) {
            porEstado.put(e, 0L);
        }
        for (Map<String, Object> o : ordenes) {
            porEstado.merge(String.valueOf(o.get("estado")), 1L, Long::sum);
        }
        List<Map<String, Object>> bajoStock = sv.repuestos.lista("/api/repuestos").stream()
                .filter(r -> M.entero(r.get("stock")) <= M.entero(r.get("stockMinimo")))
                .toList();

        model.addAttribute("porEstado", porEstado);
        model.addAttribute("totalOrdenes", ordenes.size());
        model.addAttribute("totalClientes", sv.clientes.lista("/api/clientes").size());
        model.addAttribute("totalTecnicos", sv.tecnicos.lista("/api/tecnicos").size());
        model.addAttribute("bajoStock", bajoStock);
        return "admin/panel";
    }

    // ------------------------------------------------------------------ técnicos (RF-02)

    @GetMapping("/tecnicos")
    public String tecnicos(@RequestParam(required = false) String editar, Model model) {
        model.addAttribute("tecnicos", sv.tecnicos.lista("/api/tecnicos"));
        model.addAttribute("form", editar == null || editar.isBlank()
                ? M.of("id", null, "nombre", null, "especialidades", null, "disponible", true, "tarifaHora", null)
                : sv.tecnicos.obtener("/api/tecnicos/{id}", editar));
        return "admin/tecnicos";
    }

    @PostMapping("/tecnicos")
    public String guardarTecnico(@RequestParam(required = false) String id,
                                 @RequestParam String nombre,
                                 @RequestParam(required = false) List<String> especialidades,
                                 @RequestParam double tarifaHora,
                                 @RequestParam(defaultValue = "false") boolean disponible,
                                 RedirectAttributes ra) {
        Map<String, Object> cuerpo = M.of("nombre", nombre,
                "especialidades", especialidades == null ? List.of() : especialidades,
                "tarifaHora", tarifaHora, "disponible", disponible);
        if (id == null || id.isBlank()) {
            sv.tecnicos.crear("/api/tecnicos", cuerpo);
            ra.addFlashAttribute("ok", "Técnico creado");
        } else {
            sv.tecnicos.actualizar("/api/tecnicos/{id}", cuerpo, id);
            ra.addFlashAttribute("ok", "Técnico actualizado");
        }
        return "redirect:/admin/tecnicos";
    }

    @PostMapping("/tecnicos/{id}/eliminar")
    public String eliminarTecnico(@PathVariable String id, RedirectAttributes ra) {
        sv.tecnicos.eliminar("/api/tecnicos/{id}", id);
        ra.addFlashAttribute("ok", "Técnico eliminado");
        return "redirect:/admin/tecnicos";
    }

    // ------------------------------------------------------------------ usuarios

    @GetMapping("/usuarios")
    public String listaUsuarios(Model model) {
        model.addAttribute("usuarios", usuarios.findAll());
        model.addAttribute("tecnicos", sv.tecnicos.lista("/api/tecnicos"));
        model.addAttribute("clientes", sv.clientes.lista("/api/clientes"));
        model.addAttribute("proveedores", sv.repuestos.lista("/api/proveedores"));
        model.addAttribute("roles", Catalogo.ROLES);
        return "admin/usuarios";
    }

    @PostMapping("/usuarios")
    public String crearUsuario(@RequestParam String username,
                               @RequestParam String password,
                               @RequestParam String nombre,
                               @RequestParam String rol,
                               @RequestParam(required = false) String tecnicoId,
                               @RequestParam(required = false) String clienteId,
                               @RequestParam(required = false) String proveedorId,
                               RedirectAttributes ra) {
        if (!Catalogo.ROLES.contains(rol)) {
            throw new ApiError("Rol no válido");
        }
        if (usuarios.findByUsername(username).isPresent()) {
            throw new ApiError("Ya existe un usuario con ese nombre de usuario");
        }
        String referencia = switch (rol) {
            case "TECNICO" -> tecnicoId;
            case "CLIENTE" -> clienteId;
            case "PROVEEDOR" -> proveedorId;
            default -> null;
        };
        boolean necesitaReferencia = "TECNICO".equals(rol) || "CLIENTE".equals(rol) || "PROVEEDOR".equals(rol);
        if (necesitaReferencia && (referencia == null || referencia.isBlank())) {
            throw new ApiError("Selecciona el técnico, cliente o proveedor al que pertenece esta cuenta");
        }

        Usuario u = new Usuario();
        u.setUsername(username.trim());
        u.setPassword(encoder.encode(password));
        u.setNombre(nombre.trim());
        u.setRol(rol);
        u.setReferenciaId(necesitaReferencia ? referencia : null);
        usuarios.save(u);
        ra.addFlashAttribute("ok", "Usuario creado");
        return "redirect:/admin/usuarios";
    }

    @PostMapping("/usuarios/{id}/eliminar")
    public String eliminarUsuario(@PathVariable String id, @AuthenticationPrincipal UsuarioActual actual,
                                  RedirectAttributes ra) {
        if (id.equals(actual.getIdUsuario())) {
            throw new ApiError("No puedes eliminar tu propia cuenta");
        }
        usuarios.deleteById(id);
        ra.addFlashAttribute("ok", "Usuario eliminado");
        return "redirect:/admin/usuarios";
    }

    // ------------------------------------------------------------------ demostración

    @PostMapping("/demo")
    public String cargarDemo(RedirectAttributes ra) {
        ra.addFlashAttribute("ok", demo.cargar());
        return "redirect:/admin";
    }
}
