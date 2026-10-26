package com.abarrotes.oakapi.repository;

import com.abarrotes.oakapi.model.PagoCreditoAplicacion;
import com.abarrotes.oakapi.model.PagoCreditoAplicacionId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PagoCreditoAplicacionRepository extends JpaRepository<PagoCreditoAplicacion, PagoCreditoAplicacionId> {

    List<PagoCreditoAplicacion> findByPagoId(Integer pagoId);

    List<PagoCreditoAplicacion> findByVentaId(String ventaId);
}