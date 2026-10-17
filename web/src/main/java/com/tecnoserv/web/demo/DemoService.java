package com.tecnoserv.web.demo;

import java.util.List;
import java.util.Map;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.tecnoserv.web.api.Servicios;
import com.tecnoserv.web.seguridad.Usuario;
import com.tecnoserv.web.seguridad.UsuarioRepository;
import com.tecnoserv.web.util.M;


/** Carga datos de ejemplo (técnicos, repuestos, un cliente y usuarios) para probar el sistema rápido. */
@Service
public class DemoService {

    private final Servicios sv;
    private final UsuarioRepository usuarios;
    private final PasswordEncoder encoder;

    public DemoService(Servicios sv, UsuarioRepository usuarios, PasswordEncoder encoder) {
        this.sv = sv;
        this.usuarios = usuarios;
        this.encoder = encoder;
    }

    public String cargar() {
        if (!sv.tecnicos.lista("/api/tecnicos").isEmpty() || !sv.clientes.lista("/api/clientes").isEmpty()) {
            return "Ya existen datos en el sistema; no se cargó la demostración.";
        }

        Map<String, Object> carlos = sv.tecnicos.crear("/api/tecnicos", M.of(
                "nombre", "Carlos Ruiz", "especialidades", List.of("Computadores", "Impresoras"),
                "disponible", true, "tarifaHora", 25000));
        Map<String, Object> laura = sv.tecnicos.crear("/api/tecnicos", M.of(
                "nombre", "Laura Gómez", "especialidades", List.of("Celulares", "Electrodomésticos"),
                "disponible", true, "tarifaHora", 22000));

        Map<String, Object> proveedor = sv.repuestos.crear("/api/proveedores", M.of(
                "nombre", "TecnoPartes S.A.S.", "contacto", "Andrés Mora", "correo", "ventas@tecnopartes.example"));
        String proveedorId = String.valueOf(proveedor.get("id"));
        sv.repuestos.crear("/api/repuestos", M.of("codigo", "REP-001", "nombre", "Pantalla portátil 15.6 pulgadas",
                "stock", 5, "stockMinimo", 2, "precioUnitario", 180000, "proveedorId", proveedorId));
        sv.repuestos.crear("/api/repuestos", M.of("codigo", "REP-002", "nombre", "Batería para portátil",
                "stock", 10, "stockMinimo", 3, "precioUnitario", 60000, "proveedorId", proveedorId));
        sv.repuestos.crear("/api/repuestos", M.of("codigo", "REP-003", "nombre", "Disco SSD 480 GB",
                "stock", 1, "stockMinimo", 2, "precioUnitario", 220000, "proveedorId", proveedorId));

        Map<String, Object> maria = sv.clientes.crear("/api/clientes", M.of(
                "nombre", "María Pérez", "documento", "1098765432", "telefono", "3001234567", "correo", "maria@example.com"));
        sv.clientes.crear("/api/equipos", M.of("clienteId", maria.get("id"), "tipo", "Computadores",
                "marca", "HP", "modelo", "Pavilion 15", "serial", "SN-HP-0001", "descripcionFalla", "No enciende"));

        crearUsuario("tecnico1", "tecnico123", "Carlos Ruiz", "TECNICO", String.valueOf(carlos.get("id")));
        crearUsuario("tecnico2", "tecnico123", "Laura Gómez", "TECNICO", String.valueOf(laura.get("id")));
        crearUsuario("cliente1", "cliente123", "María Pérez", "CLIENTE", String.valueOf(maria.get("id")));
        crearUsuario("proveedor1", "proveedor123", "TecnoPartes S.A.S.", "PROVEEDOR", proveedorId);

        return "Datos de demostración cargados. Usuarios: tecnico1, tecnico2 (clave tecnico123), "
                + "cliente1 (cliente123) y proveedor1 (proveedor123).";
    }

    private void crearUsuario(String username, String clave, String nombre, String rol, String referenciaId) {
        if (usuarios.findByUsername(username).isPresent()) {
            return;
        }
        Usuario u = new Usuario();
        u.setUsername(username);
        u.setPassword(encoder.encode(clave));
        u.setNombre(nombre);
        u.setRol(rol);
        u.setReferenciaId(referenciaId);
        usuarios.save(u);
    }
}
