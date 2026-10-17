package com.tecnoserv.repuestos.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.tecnoserv.repuestos.error.ApiException;
import com.tecnoserv.repuestos.model.Repuesto;
import com.tecnoserv.repuestos.repository.ProveedorRepository;
import com.tecnoserv.repuestos.repository.RepuestoRepository;

import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;

@RestController
@RequestMapping("/api/repuestos")
@Tag(name = "Repuestos", description = "Catalogo e inventario de repuestos")
public class RepuestoController {

    public record CantidadRequest(@Positive(message = "La cantidad debe ser mayor que cero") int cantidad) {
    }

    private final RepuestoRepository repuestos;
    private final ProveedorRepository proveedores;

    public RepuestoController(RepuestoRepository repuestos, ProveedorRepository proveedores) {
        this.repuestos = repuestos;
        this.proveedores = proveedores;
    }

    @GetMapping
    public List<Repuesto> listar(@RequestParam(required = false) String proveedorId) {
        return proveedorId == null || proveedorId.isBlank() ? repuestos.findAll() : repuestos.findByProveedorId(proveedorId);
    }

    @GetMapping("/{id}")
    public Repuesto obtener(@PathVariable String id) {
        return repuestos.findById(id).orElseThrow(() -> ApiException.noEncontrado("Repuesto no encontrado"));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Repuesto crear(@Valid @RequestBody Repuesto repuesto) {
        repuesto.setId(null);
        validarProveedor(repuesto);
        if (repuestos.existsByCodigo(repuesto.getCodigo())) {
            throw ApiException.conflicto("Ya existe un repuesto con ese codigo");
        }
        return repuestos.save(repuesto);
    }

    @PutMapping("/{id}")
    public Repuesto actualizar(@PathVariable String id, @Valid @RequestBody Repuesto repuesto) {
        obtener(id);
        validarProveedor(repuesto);
        repuestos.findByCodigo(repuesto.getCodigo()).ifPresent(otro -> {
            if (!otro.getId().equals(id)) {
                throw ApiException.conflicto("Ya existe un repuesto con ese codigo");
            }
        });
        repuesto.setId(id);
        return repuestos.save(repuesto);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void eliminar(@PathVariable String id) {
        obtener(id);
        repuestos.deleteById(id);
    }

    /**
     * Descuenta stock al usar un repuesto en una orden. Si no alcanza el inventario
     * responde 409 y el servicio de ordenes mueve la orden a "Esperando repuesto".
     */
    @PostMapping("/{id}/descontar")
    public Repuesto descontar(@PathVariable String id, @Valid @RequestBody CantidadRequest req) {
        Repuesto r = obtener(id);
        if (r.getStock() < req.cantidad()) {
            throw ApiException.conflicto("Sin existencias suficientes de \"" + r.getNombre()
                    + "\" (disponibles: " + r.getStock() + ")");
        }
        r.setStock(r.getStock() - req.cantidad());
        return repuestos.save(r);
    }

    private void validarProveedor(Repuesto repuesto) {
        String proveedorId = repuesto.getProveedorId();
        if (proveedorId != null && proveedorId.isBlank()) {
            repuesto.setProveedorId(null);
        } else if (proveedorId != null && !proveedores.existsById(proveedorId)) {
            throw ApiException.invalida("El proveedor indicado no existe");
        }
    }
}
