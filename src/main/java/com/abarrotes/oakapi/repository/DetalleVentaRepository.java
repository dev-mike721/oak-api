package com.abarrotes.oakapi.repository;

import com.abarrotes.oakapi.model.DetalleVenta;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DetalleVentaRepository extends JpaRepository<DetalleVenta, Long> {

    List<DetalleVenta> findByVentaUuidOrderByIdAsc(String ventaUuid);
}