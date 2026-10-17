package com.tecnoserv.clientes.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.mongodb.repository.MongoRepository;

import com.tecnoserv.clientes.model.Equipo;

public interface EquipoRepository extends MongoRepository<Equipo, String> {

    List<Equipo> findByClienteId(String clienteId);

    boolean existsByClienteId(String clienteId);

    boolean existsBySerial(String serial);

    Optional<Equipo> findBySerial(String serial);
}
