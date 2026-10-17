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
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.tecnoserv.repuestos.error.ApiException;
import com.tecnoserv.repuestos.model.Proveedor;
import com.tecnoserv.repuestos.repository.ProveedorRepository;
import com.tecnoserv.repuestos.repository.RepuestoRepository;

import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/proveedores")
@Tag(name = "Proveedores", description = "Proveedores de repuestos")
public class ProveedorController {

    private final ProveedorRepository proveedores;
    private final RepuestoRepository repuestos;

    public ProveedorController(ProveedorRepository proveedores, RepuestoRepository repuestos) {
        this.proveedores = proveedores;
        this.repuestos = repuestos;
    }

    @GetMapping
    public List<Proveedor> listar() {
        return proveedores.findAll();
    }

    @GetMapping("/{id}")
    public Proveedor obtener(@PathVariable String id) {
        return proveedores.findById(id).orElseThrow(() -> ApiException.noEncontrado("Proveedor no encontrado"));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Proveedor crear(@Valid @RequestBody Proveedor proveedor) {
        proveedor.setId(null);
        return proveedores.save(proveedor);
    }

    @PutMapping("/{id}")
    public Proveedor actualizar(@PathVariable String id, @Valid @RequestBody Proveedor proveedor) {
        obtener(id);
        proveedor.setId(id);
        return proveedores.save(proveedor);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void eliminar(@PathVariable String id) {
        obtener(id);
        if (repuestos.existsByProveedorId(id)) {
            throw ApiException.conflicto("El proveedor tiene repuestos asociados");
        }
        proveedores.deleteById(id);
    }
}
