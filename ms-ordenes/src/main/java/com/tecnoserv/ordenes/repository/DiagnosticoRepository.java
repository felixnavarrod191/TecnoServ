package com.tecnoserv.ordenes.repository;

import java.util.List;

import org.springframework.data.mongodb.repository.MongoRepository;

import com.tecnoserv.ordenes.model.Diagnostico;

public interface DiagnosticoRepository extends MongoRepository<Diagnostico, String> {

    List<Diagnostico> findByOrdenIdOrderByFechaDesc(String ordenId);

    boolean existsByOrdenId(String ordenId);
}
