package com.abarrotes.oakapi.repository;

import com.abarrotes.oakapi.model.Venta;
import com.abarrotes.oakapi.model.enums.EstadoVenta;
import com.abarrotes.oakapi.model.enums.MetodoPago;
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
public interface VentaRepository extends JpaRepository<Venta, String> {

    Optional<Venta> findById(String uuid);

    @Query("SELECT v FROM Venta v WHERE " +
           "(:metodoPago IS NULL OR v.metodoPago = :metodoPago) AND " +
           "(:estado IS NULL OR v.estado = :estado) AND " +
           "(:usuarioId IS NULL OR v.usuario.id = :usuarioId) AND " +
           "(:turnoId IS NULL OR v.turno.id = :turnoId) AND " +
           "(:cajaId IS NULL OR v.caja.id = :cajaId) AND " +
           "(:clienteId IS NULL OR v.cliente.id = :clienteId) AND " +
           "(:fechaDesde IS NULL OR v.fechaHora >= :fechaDesde) AND " +
           "(:fechaHasta IS NULL OR v.fechaHora < :fechaHasta) " +
           "ORDER BY v.fechaHora DESC")
    Page<Venta> buscarConFiltros(@Param("metodoPago") MetodoPago metodoPago,
                                 @Param("estado") EstadoVenta estado,
                                 @Param("usuarioId") Integer usuarioId,
                                 @Param("turnoId") Integer turnoId,
                                 @Param("cajaId") Integer cajaId,
                                 @Param("clienteId") Integer clienteId,
                                 @Param("fechaDesde") LocalDateTime fechaDesde,
                                 @Param("fechaHasta") LocalDateTime fechaHasta,
                                 Pageable pageable);

    List<Venta> findByClienteIdAndEstadoAndFechaVencimientoBefore(Integer clienteId, EstadoVenta estado, LocalDate fechaVencimiento);

    List<Venta> findByClienteIdAndEstadoAndMetodoPago(Integer clienteId, EstadoVenta estado, MetodoPago metodoPago);

    @Query("SELECT v FROM Venta v WHERE v.presupuestoId = :presupuestoId")
    Optional<Venta> findByPresupuestoId(String presupuestoId);

    List<Venta> findByCajaId(Integer cajaId);
}