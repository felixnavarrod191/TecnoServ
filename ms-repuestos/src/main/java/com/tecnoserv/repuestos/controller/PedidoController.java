package com.tecnoserv.repuestos.controller;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.tecnoserv.repuestos.error.ApiException;
import com.tecnoserv.repuestos.model.Pedido;
import com.tecnoserv.repuestos.model.Repuesto;
import com.tecnoserv.repuestos.repository.PedidoRepository;
import com.tecnoserv.repuestos.repository.RepuestoRepository;

import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;

@RestController
@RequestMapping("/api/pedidos")
@Tag(name = "Pedidos", description = "Pedidos y entregas de repuestos de los proveedores")
public class PedidoController {

    public record PedidoRequest(
            @NotBlank(message = "El repuesto es obligatorio") String repuestoId,
            @Positive(message = "La cantidad debe ser mayor que cero") int cantidad,
            String solicitadoPor) {
    }

    private final PedidoRepository pedidos;
    private final RepuestoRepository repuestos;

    public PedidoController(PedidoRepository pedidos, RepuestoRepository repuestos) {
        this.pedidos = pedidos;
        this.repuestos = repuestos;
    }

    @GetMapping
    public List<Pedido> listar(@RequestParam(required = false) String proveedorId) {
        List<Pedido> lista = proveedorId == null || proveedorId.isBlank()
                ? pedidos.findAll() : pedidos.findByProveedorId(proveedorId);
        return lista.stream()
                .sorted(Comparator.comparing(Pedido::getFechaSolicitud, Comparator.nullsLast(Comparator.<LocalDateTime>reverseOrder())))
                .toList();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Pedido crear(@Valid @RequestBody PedidoRequest req) {
        Repuesto repuesto = repuestos.findById(req.repuestoId())
                .orElseThrow(() -> ApiException.invalida("El repuesto indicado no existe"));
        if (repuesto.getProveedorId() == null) {
            throw ApiException.conflicto("El repuesto no tiene un proveedor asignado");
        }
        Pedido p = new Pedido();
        p.setProveedorId(repuesto.getProveedorId());
        p.setRepuestoId(repuesto.getId());
        p.setRepuestoNombre(repuesto.getNombre());
        p.setCantidad(req.cantidad());
        p.setEstado(Pedido.SOLICITADO);
        p.setFechaSolicitud(LocalDateTime.now());
        p.setSolicitadoPor(req.solicitadoPor());
        return pedidos.save(p);
    }

    /** Registra la entrega del pedido: el stock del repuesto aumenta en la cantidad entregada. */
    @PostMapping("/{id}/entregar")
    public Pedido entregar(@PathVariable String id) {
        Pedido p = pedidos.findById(id).orElseThrow(() -> ApiException.noEncontrado("Pedido no encontrado"));
        if (Pedido.ENTREGADO.equals(p.getEstado())) {
            throw ApiException.conflicto("El pedido ya fue entregado");
        }
        Repuesto repuesto = repuestos.findById(p.getRepuestoId())
                .orElseThrow(() -> ApiException.conflicto("El repuesto del pedido ya no existe"));
        repuesto.setStock(repuesto.getStock() + p.getCantidad());
        repuestos.save(repuesto);
        p.setEstado(Pedido.ENTREGADO);
        p.setFechaEntrega(LocalDateTime.now());
        return pedidos.save(p);
    }
}
