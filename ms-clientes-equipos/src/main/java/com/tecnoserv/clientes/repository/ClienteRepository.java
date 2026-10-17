package com.tecnoserv.clientes.repository;

import java.util.Optional;

import org.springframework.data.mongodb.repository.MongoRepository;

import com.tecnoserv.clientes.model.Cliente;

public interface ClienteRepository extends MongoRepository<Cliente, String> {

    boolean existsByDocumento(String documento);

    Optional<Cliente> findByDocumento(String documento);
}
