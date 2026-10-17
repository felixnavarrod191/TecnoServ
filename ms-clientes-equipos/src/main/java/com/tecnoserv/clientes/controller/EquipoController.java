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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.tecnoserv.clientes.error.ApiException;
import com.tecnoserv.clientes.model.Equipo;
import com.tecnoserv.clientes.repository.ClienteRepository;
import com.tecnoserv.clientes.repository.EquipoRepository;

import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/equipos")
@Tag(name = "Equipos", description = "Equipos o dispositivos asociados a un cliente")
public class EquipoController {

    private final EquipoRepository equipos;
    private final ClienteRepository clientes;

    public EquipoController(EquipoRepository equipos, ClienteRepository clientes) {
        this.equipos = equipos;
        this.clientes = clientes;
    }

    @GetMapping
    public List<Equipo> listar(@RequestParam(required = false) String clienteId) {
        return clienteId == null || clienteId.isBlank() ? equipos.findAll() : equipos.findByClienteId(clienteId);
    }

    @GetMapping("/{id}")
    public Equipo obtener(@PathVariable String id) {
        return equipos.findById(id).orElseThrow(() -> ApiException.noEncontrado("Equipo no encontrado"));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Equipo crear(@Valid @RequestBody Equipo equipo) {
        equipo.setId(null);
        if (!clientes.existsById(equipo.getClienteId())) {
            throw ApiException.invalida("El cliente indicado no existe");
        }
        if (equipos.existsBySerial(equipo.getSerial())) {
            throw ApiException.conflicto("Ya existe un equipo con ese serial");
        }
        return equipos.save(equipo);
    }

    @PutMapping("/{id}")
    public Equipo actualizar(@PathVariable String id, @Valid @RequestBody Equipo equipo) {
        obtener(id);
        if (!clientes.existsById(equipo.getClienteId())) {
            throw ApiException.invalida("El cliente indicado no existe");
        }
        equipos.findBySerial(equipo.getSerial()).ifPresent(otro -> {
            if (!otro.getId().equals(id)) {
                throw ApiException.conflicto("Ya existe un equipo con ese serial");
            }
        });
        equipo.setId(id);
        return equipos.save(equipo);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void eliminar(@PathVariable String id) {
        obtener(id);
        equipos.deleteById(id);
    }
}
