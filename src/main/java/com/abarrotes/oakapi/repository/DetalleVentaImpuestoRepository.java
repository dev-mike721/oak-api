package com.abarrotes.oakapi.repository;

import com.abarrotes.oakapi.model.DetalleVentaImpuesto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DetalleVentaImpuestoRepository extends JpaRepository<DetalleVentaImpuesto, Long> {

    List<DetalleVentaImpuesto> findByDetalleVentaId(Long detalleVentaId);

    List<DetalleVentaImpuesto> findByImpuestoId(Integer impuestoId);

    boolean existsByImpuestoId(Integer impuestoId);
}