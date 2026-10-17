package com.tecnoserv.tecnicos.repository;

import org.springframework.data.mongodb.repository.MongoRepository;

import com.tecnoserv.tecnicos.model.Tecnico;

public interface TecnicoRepository extends MongoRepository<Tecnico, String> {
}
