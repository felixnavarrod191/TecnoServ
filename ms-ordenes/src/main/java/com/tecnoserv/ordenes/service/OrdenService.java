package com.tecnoserv.ordenes.service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import com.tecnoserv.ordenes.controller.Requests;
import com.tecnoserv.ordenes.error.ApiException;
import com.tecnoserv.ordenes.model.CambioEstado;
import com.tecnoserv.ordenes.model.Diagnostico;
import com.tecnoserv.ordenes.model.Estado;
import com.tecnoserv.ordenes.model.OrdenServicio;
import com.tecnoserv.ordenes.model.RepuestoUsado;
import com.tecnoserv.ordenes.repository.DiagnosticoRepository;
import com.tecnoserv.ordenes.repository.OrdenRepository;


/**
 * Logica de negocio de las ordenes de servicio: asignacion de tecnicos,
 * flujo de estados estricto, diagnosticos y consumo de repuestos.
 */
@Service
public class OrdenService {

    private static final List<Estado> ACTIVAS =
            List.of(Estado.INGRESADO, Estado.EN_DIAGNOSTICO, Estado.ESPERANDO_REPUESTO);

    private final OrdenRepository ordenes;
    private final DiagnosticoRepository diagnosticos;
    private final Remotos remotos;

    @Value("${ordenes.max-activas:3}")
    private int maxActivas;

    public OrdenService(OrdenRepository ordenes, DiagnosticoRepository diagnosticos, Remotos remotos) {
        this.ordenes = ordenes;
        this.diagnosticos = diagnosticos;
        this.remotos = remotos;
    }

    // ------------------------------------------------------------------ consultas

    public OrdenServicio obtener(String id) {
        return ordenes.findById(id).orElseThrow(() -> ApiException.noEncontrado("Orden de servicio no encontrada"));
    }

    public List<OrdenServicio> listar(String clienteId, String tecnicoId, Estado estado) {
        return ordenes.findAll().stream()
                .filter(o -> vacio(clienteId) || clienteId.equals(o.getClienteId()))
                .filter(o -> vacio(tecnicoId) || tecnicoId.equals(o.getTecnicoId()))
                .filter(o -> estado == null || estado == o.getEstado())
                .sorted(Comparator.comparing(OrdenServicio::getFechaIngreso,
                        Comparator.nullsLast(Comparator.<LocalDateTime>reverseOrder())))
                .toList();
    }

    public List<Diagnostico> diagnosticos(String ordenId) {
        obtener(ordenId);
        return diagnosticos.findByOrdenIdOrderByFechaDesc(ordenId);
    }

    /** Tecnicos que se pueden ofrecer: disponibles, con la especialidad y por debajo del maximo de ordenes activas. */
    public List<Map<String, Object>> candidatos(String tipoReparacion) {
        return remotos.tecnicosDisponibles(tipoReparacion).stream()
                .filter(t -> activas(String.valueOf(t.get("id"))) < maxActivas)
                .toList();
    }

    // ------------------------------------------------------------------ crear y asignar

    public OrdenServicio crear(Requests.Crear req) {
        Map<String, Object> equipo = remotos.equipo(req.equipoId());
        if (!req.clienteId().equals(String.valueOf(equipo.get("clienteId")))) {
            throw ApiException.invalida("El equipo no pertenece al cliente seleccionado");
        }
        Map<String, Object> cliente = remotos.cliente(req.clienteId());

        LocalDateTime ahora = LocalDateTime.now();
        OrdenServicio o = new OrdenServicio();
        o.setClienteId(req.clienteId());
        o.setClienteNombre(texto(cliente.get("nombre")));
        o.setEquipoId(req.equipoId());
        o.setEquipoDescripcion(describir(equipo));
        o.setTipoReparacion(req.tipoReparacion());
        o.setFallaReportada(req.fallaReportada());
        o.setEstado(Estado.INGRESADO);
        o.setFechaIngreso(ahora);
        o.getHistorial().add(new CambioEstado(Estado.INGRESADO, ahora, usuarioOr(req.usuario())));
        return ordenes.save(o);
    }

    /** Regla 1: solo se asigna un tecnico disponible, con la especialidad adecuada y con cupo. */
    public OrdenServicio asignar(String id, String tecnicoId) {
        OrdenServicio o = obtener(id);
        if (!o.getEstado().activa()) {
            throw ApiException.conflicto("La orden ya fue completada o entregada; no se puede reasignar");
        }
        Map<String, Object> tecnico = remotos.tecnico(tecnicoId);

        if (!Boolean.TRUE.equals(tecnico.get("disponible"))) {
            throw ApiException.conflicto("El técnico no está disponible");
        }
        boolean compatible = tecnico.get("especialidades") instanceof List<?> especialidades
                && especialidades.stream().anyMatch(e -> String.valueOf(e).equalsIgnoreCase(o.getTipoReparacion()));
        if (!compatible) {
            throw ApiException.conflicto("La especialidad del técnico no corresponde al tipo de reparación (" + o.getTipoReparacion() + ")");
        }
        long activas = activas(tecnicoId);
        if (tecnicoId.equals(o.getTecnicoId())) {
            activas--; // esta misma orden ya cuenta como activa del tecnico
        }
        if (activas >= maxActivas) {
            throw ApiException.conflicto("El técnico ya tiene el máximo de órdenes activas (" + maxActivas + ")");
        }

        o.setTecnicoId(tecnicoId);
        o.setTecnicoNombre(texto(tecnico.get("nombre")));
        o.setTarifaHora(numero(tecnico.get("tarifaHora")));
        return ordenes.save(o);
    }

    // ------------------------------------------------------------------ flujo de estados

    /** Regla 2: solo se avanza al estado inmediatamente siguiente, con condiciones y por el rol correcto. */
    public OrdenServicio avanzar(String id, String usuario, String rol) {
        OrdenServicio o = obtener(id);
        Estado siguiente = o.getEstado().siguiente();
        if (siguiente == null) {
            throw ApiException.conflicto("La orden ya fue entregada");
        }
        if (siguiente == Estado.ENTREGADO) {
            if (!"RECEPCIONISTA".equals(rol)) {
                throw ApiException.conflicto("Solo recepción puede marcar la orden como Entregado");
            }
        } else if (!"TECNICO".equals(rol)) {
            throw ApiException.conflicto("Solo el técnico puede avanzar la orden a este estado");
        }
        if (siguiente == Estado.EN_DIAGNOSTICO && o.getTecnicoId() == null) {
            throw ApiException.conflicto("Debe asignarse un técnico antes de pasar a En diagnóstico");
        }
        if (siguiente == Estado.COMPLETADO && !diagnosticos.existsByOrdenId(id)) {
            throw ApiException.conflicto("No se puede completar una orden sin diagnóstico registrado");
        }
        return cambiarEstado(o, siguiente, usuario);
    }

    private OrdenServicio cambiarEstado(OrdenServicio o, Estado nuevo, String usuario) {
        o.setEstado(nuevo);
        o.getHistorial().add(new CambioEstado(nuevo, LocalDateTime.now(), usuarioOr(usuario)));
        return ordenes.save(o);
    }

    // ------------------------------------------------------------------ diagnostico, horas y repuestos

    public Diagnostico registrarDiagnostico(String id, Requests.DiagnosticoReq req) {
        OrdenServicio o = obtener(id);
        if (o.getEstado() != Estado.EN_DIAGNOSTICO && o.getEstado() != Estado.ESPERANDO_REPUESTO) {
            throw ApiException.conflicto("El diagnóstico se registra mientras la orden está en diagnóstico o esperando repuesto");
        }
        Diagnostico d = new Diagnostico();
        d.setOrdenId(id);
        d.setDescripcion(req.descripcion());
        d.setAutor(req.autor());
        d.setRepuestosRequeridos(req.repuestosRequeridos() == null ? new ArrayList<>() : req.repuestosRequeridos());
        d.setFecha(LocalDateTime.now());
        return diagnosticos.save(d);
    }

    public OrdenServicio fijarHoras(String id, double horas) {
        OrdenServicio o = obtener(id);
        if (o.getEstado() == Estado.ENTREGADO) {
            throw ApiException.conflicto("La orden ya fue entregada");
        }
        o.setHorasTrabajadas(horas);
        return ordenes.save(o);
    }

    /**
     * Asocia un repuesto a la orden y descuenta stock (via REST a ms-repuestos).
     * Si no hay existencias, la orden pasa a "Esperando repuesto".
     */
    public OrdenServicio agregarRepuesto(String id, Requests.RepuestoReq req) {
        OrdenServicio o = obtener(id);
        if (o.getEstado() != Estado.EN_DIAGNOSTICO && o.getEstado() != Estado.ESPERANDO_REPUESTO) {
            throw ApiException.conflicto("Solo se asocian repuestos mientras la orden está en diagnóstico o esperando repuesto");
        }

        Map<String, Object> repuesto;
        try {
            repuesto = remotos.descontarRepuesto(req.repuestoId(), req.cantidad());
        } catch (ApiException e) {
            if (e.getStatus() == HttpStatus.CONFLICT && o.getEstado() == Estado.EN_DIAGNOSTICO) {
                cambiarEstado(o, Estado.ESPERANDO_REPUESTO, "Sistema");
                throw ApiException.conflicto(e.getMessage() + ". La orden pasó a \"Esperando repuesto\".");
            }
            throw e;
        }

        String nombre = texto(repuesto.get("nombre"));
        double precio = numero(repuesto.get("precioUnitario"));
        o.getRepuestosUsados().stream()
                .filter(r -> r.getRepuestoId().equals(req.repuestoId()))
                .findFirst()
                .ifPresentOrElse(
                        r -> r.setCantidad(r.getCantidad() + req.cantidad()),
                        () -> o.getRepuestosUsados().add(new RepuestoUsado(req.repuestoId(), nombre, req.cantidad(), precio)));
        return ordenes.save(o);
    }

    // ------------------------------------------------------------------ utilidades

    private long activas(String tecnicoId) {
        return ordenes.countByTecnicoIdAndEstadoIn(tecnicoId, ACTIVAS);
    }

    private static boolean vacio(String s) {
        return s == null || s.isBlank();
    }

    private static String usuarioOr(String usuario) {
        return vacio(usuario) ? "Sistema" : usuario;
    }

    private static String texto(Object o) {
        return o == null ? "" : String.valueOf(o);
    }

    private static double numero(Object o) {
        return o instanceof Number n ? n.doubleValue() : 0;
    }

    private static String describir(Map<String, Object> equipo) {
        String base = (texto(equipo.get("tipo")) + " " + texto(equipo.get("marca")) + " " + texto(equipo.get("modelo"))).trim();
        String serial = texto(equipo.get("serial"));
        return serial.isEmpty() ? base : base + " (serial " + serial + ")";
    }
}
