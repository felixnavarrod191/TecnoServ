package com.tecnoserv.reportes.repository;

import java.util.Optional;

import org.springframework.data.mongodb.repository.MongoRepository;

import com.tecnoserv.reportes.model.Reporte;

public interface ReporteRepository extends MongoRepository<Reporte, String> {

    Optional<Reporte> findByOrdenId(String ordenId);
}
