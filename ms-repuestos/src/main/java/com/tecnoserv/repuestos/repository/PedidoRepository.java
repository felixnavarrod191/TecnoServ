package com.tecnoserv.repuestos.repository;

import java.util.List;

import org.springframework.data.mongodb.repository.MongoRepository;

import com.tecnoserv.repuestos.model.Pedido;

public interface PedidoRepository extends MongoRepository<Pedido, String> {

    List<Pedido> findByProveedorId(String proveedorId);
}
