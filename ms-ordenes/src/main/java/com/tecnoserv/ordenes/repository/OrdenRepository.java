package com.tecnoserv.ordenes.repository;

import java.util.Collection;

import org.springframework.data.mongodb.repository.MongoRepository;

import com.tecnoserv.ordenes.model.Estado;
import com.tecnoserv.ordenes.model.OrdenServicio;

public interface OrdenRepository extends MongoRepository<OrdenServicio, String> {

    long countByTecnicoIdAndEstadoIn(String tecnicoId, Collection<Estado> estados);
}
