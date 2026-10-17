package com.tecnoserv.clientes.controller;

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

import com.tecnoserv.clientes.error.ApiException;
import com.tecnoserv.clientes.model.Cliente;
import com.tecnoserv.clientes.repository.ClienteRepository;
import com.tecnoserv.clientes.repository.EquipoRepository;

import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/clientes")
@Tag(name = "Clientes", description = "Gestion de clientes del taller")
public class ClienteController {

    private final ClienteRepository clientes;
    private final EquipoRepository equipos;

    public ClienteController(ClienteRepository clientes, EquipoRepository equipos) {
        this.clientes = clientes;
        this.equipos = equipos;
    }

    @GetMapping
    public List<Cliente> listar() {
        return clientes.findAll();
    }

    @GetMapping("/{id}")
    public Cliente obtener(@PathVariable String id) {
        return clientes.findById(id).orElseThrow(() -> ApiException.noEncontrado("Cliente no encontrado"));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Cliente crear(@Valid @RequestBody Cliente cliente) {
        cliente.setId(null);
        if (clientes.existsByDocumento(cliente.getDocumento())) {
            throw ApiException.conflicto("Ya existe un cliente con ese documento");
        }
        return clientes.save(cliente);
    }

    @PutMapping("/{id}")
    public Cliente actualizar(@PathVariable String id, @Valid @RequestBody Cliente cliente) {
        obtener(id);
        clientes.findByDocumento(cliente.getDocumento()).ifPresent(otro -> {
            if (!otro.getId().equals(id)) {
                throw ApiException.conflicto("Ya existe un cliente con ese documento");
            }
        });
        cliente.setId(id);
        return clientes.save(cliente);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void eliminar(@PathVariable String id) {
        obtener(id);
        if (equipos.existsByClienteId(id)) {
            throw ApiException.conflicto("El cliente tiene equipos registrados; eliminelos primero");
        }
        clientes.deleteById(id);
    }
}
