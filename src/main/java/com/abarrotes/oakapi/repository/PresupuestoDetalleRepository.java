package com.abarrotes.oakapi.repository;

import com.abarrotes.oakapi.model.PresupuestoDetalle;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PresupuestoDetalleRepository extends JpaRepository<PresupuestoDetalle, Integer> {

    List<PresupuestoDetalle> findByPresupuestoId(String presupuestoId);
}