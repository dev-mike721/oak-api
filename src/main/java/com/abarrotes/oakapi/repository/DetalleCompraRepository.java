package com.abarrotes.oakapi.repository;

import com.abarrotes.oakapi.model.DetalleCompra;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DetalleCompraRepository extends JpaRepository<DetalleCompra, Long> {

    List<DetalleCompra> findByCompraIdOrderByIdAsc(Integer compraId);
}