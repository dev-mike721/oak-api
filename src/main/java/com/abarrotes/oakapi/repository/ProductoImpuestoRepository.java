package com.abarrotes.oakapi.repository;

import com.abarrotes.oakapi.model.ProductoImpuesto;
import com.abarrotes.oakapi.model.ProductoImpuestoId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProductoImpuestoRepository extends JpaRepository<ProductoImpuesto, ProductoImpuestoId> {

    List<ProductoImpuesto> findByProductoId(Integer productoId);

    Optional<ProductoImpuesto> findByProductoIdAndImpuestoId(Integer productoId, Integer impuestoId);

    List<ProductoImpuesto> findByImpuestoId(Integer impuestoId);

    boolean existsByProductoIdAndImpuestoId(Integer productoId, Integer impuestoId);
}