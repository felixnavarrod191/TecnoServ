package com.tecnoserv.repuestos.repository;

import org.springframework.data.mongodb.repository.MongoRepository;

import com.tecnoserv.repuestos.model.Proveedor;

public interface ProveedorRepository extends MongoRepository<Proveedor, String> {
}
