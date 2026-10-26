package com.abarrotes.oakapi.repository;

import com.abarrotes.oakapi.model.Compra;
import com.abarrotes.oakapi.model.enums.EstadoCompra;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;

@Repository
public interface CompraRepository extends JpaRepository<Compra, Integer> {

    Optional<Compra> findById(Integer id);

    boolean existsByFolioFactura(String folioFactura);

    @Query("SELECT c FROM Compra c WHERE " +
           "(:estado IS NULL OR c.estado = :estado) AND " +
           "(:proveedorId IS NULL OR c.proveedor.id = :proveedorId) AND " +
           "(:fechaDesde IS NULL OR c.fechaCompra >= :fechaDesde) AND " +
           "(:fechaHasta IS NULL OR c.fechaCompra < :fechaHasta) " +
           "ORDER BY c.fechaCompra DESC")
    Page<Compra> buscarConFiltros(@Param("estado") EstadoCompra estado,
                                  @Param("proveedorId") Integer proveedorId,
                                  @Param("fechaDesde") LocalDateTime fechaDesde,
                                  @Param("fechaHasta") LocalDateTime fechaHasta,
                                  Pageable pageable);
}