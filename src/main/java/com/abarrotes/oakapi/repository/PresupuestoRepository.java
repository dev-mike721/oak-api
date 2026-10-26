package com.abarrotes.oakapi.repository;

import com.abarrotes.oakapi.model.Presupuesto;
import com.abarrotes.oakapi.model.enums.EstadoPresupuesto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface PresupuestoRepository extends JpaRepository<Presupuesto, String> {

    Optional<Presupuesto> findById(String id);

    Page<Presupuesto> findByClienteId(Integer clienteId, Pageable pageable);

    Page<Presupuesto> findByUsuarioId(Integer usuarioId, Pageable pageable);

    Page<Presupuesto> findByEstado(EstadoPresupuesto estado, Pageable pageable);

    @Query("SELECT p FROM Presupuesto p WHERE p.estado IN (:estados)")
    List<Presupuesto> findByEstadoInPresupuesto(@Param("estados") List<EstadoPresupuesto> estados);

    @Query("SELECT p FROM Presupuesto p " +
           "WHERE p.cliente.id = :clienteId " +
           "AND p.estado IN (:estados) " +
           "ORDER BY p.fecha DESC")
    List<Presupuesto> findByClienteAndEstados(@Param("clienteId") Integer clienteId, @Param("estados") List<EstadoPresupuesto> estados);

    @Query("SELECT p FROM Presupuesto p " +
           "WHERE (:estado IS NULL OR p.estado = :estado) " +
           "AND (:clienteId IS NULL OR p.cliente.id = :clienteId) " +
           "AND (:usuarioId IS NULL OR p.usuario.id = :usuarioId) " +
           "AND (:fechaDesde IS NULL OR p.fecha >= :fechaDesde) " +
           "AND (:fechaHasta IS NULL OR p.fecha <= :fechaHasta) " +
           "ORDER BY p.fecha DESC")
    Page<Presupuesto> buscarConFiltros(@Param("estado") EstadoPresupuesto estado,
                                       @Param("clienteId") Integer clienteId,
                                       @Param("usuarioId") Integer usuarioId,
                                       @Param("fechaDesde") LocalDate fechaDesde,
                                       @Param("fechaHasta") LocalDate fechaHasta,
                                       Pageable pageable);
}