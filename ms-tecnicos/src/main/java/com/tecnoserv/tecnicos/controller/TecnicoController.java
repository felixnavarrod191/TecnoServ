package com.tecnoserv.tecnicos.controller;

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

import com.tecnoserv.tecnicos.error.ApiException;
import com.tecnoserv.tecnicos.model.Tecnico;
import com.tecnoserv.tecnicos.repository.TecnicoRepository;

import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/tecnicos")
@Tag(name = "Tecnicos", description = "Gestion de tecnicos, especialidades y disponibilidad")
public class TecnicoController {

    private final TecnicoRepository tecnicos;

    public TecnicoController(TecnicoRepository tecnicos) {
        this.tecnicos = tecnicos;
    }

    /** Lista tecnicos; se puede filtrar por especialidad y por disponibilidad. */
    @GetMapping
    public List<Tecnico> listar(@RequestParam(required = false) String especialidad,
                                @RequestParam(required = false) Boolean disponible) {
        return tecnicos.findAll().stream()
                .filter(t -> disponible == null || t.isDisponible() == disponible)
                .filter(t -> especialidad == null || especialidad.isBlank()
                        || t.getEspecialidades().stream().anyMatch(e -> e.equalsIgnoreCase(especialidad)))
                .toList();
    }

    @GetMapping("/{id}")
    public Tecnico obtener(@PathVariable String id) {
        return tecnicos.findById(id).orElseThrow(() -> ApiException.noEncontrado("Tecnico no encontrado"));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Tecnico crear(@Valid @RequestBody Tecnico tecnico) {
        tecnico.setId(null);
        return tecnicos.save(tecnico);
    }

    @PutMapping("/{id}")
    public Tecnico actualizar(@PathVariable String id, @Valid @RequestBody Tecnico tecnico) {
        obtener(id);
        tecnico.setId(id);
        return tecnicos.save(tecnico);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void eliminar(@PathVariable String id) {
        obtener(id);
        tecnicos.deleteById(id);
    }
}
