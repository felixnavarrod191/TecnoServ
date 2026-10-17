package com.tecnoserv.repuestos.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.mongodb.repository.MongoRepository;

import com.tecnoserv.repuestos.model.Repuesto;

public interface RepuestoRepository extends MongoRepository<Repuesto, String> {

    List<Repuesto> findByProveedorId(String proveedorId);

    boolean existsByProveedorId(String proveedorId);

    Optional<Repuesto> findByCodigo(String codigo);

    boolean existsByCodigo(String codigo);
}
