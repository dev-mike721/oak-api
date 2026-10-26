package com.abarrotes.oakapi.repository;

import com.abarrotes.oakapi.model.Lote;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface LoteRepository extends JpaRepository<Lote, Integer> {

    Optional<Lote> findTopByProductoIdAndCodigoLoteStartingWithOrderByCodigoLoteDesc(Integer productoId, String prefijo);

    boolean existsByProductoIdAndCodigoLote(Integer productoId, String codigoLote);

    @Query("SELECT l FROM Lote l WHERE l.producto.id = :productoId AND l.cantidadActual > 0 " +
           "ORDER BY CASE WHEN l.fechaCaducidad IS NULL THEN 1 ELSE 0 END, " +
           "l.fechaCaducidad ASC, l.fechaRecepcion ASC, l.id ASC")
    List<Lote> findLotesDisponiblesFEFO(@Param("productoId") Integer productoId);

    @Query(value = "SELECT l.* FROM lotes l WHERE l.producto_id = :productoId AND l.cantidad_actual > 0 " +
           "ORDER BY CASE WHEN l.fecha_caducidad IS NULL THEN 1 ELSE 0 END, " +
           "l.fecha_caducidad ASC, l.fecha_recepcion ASC, l.id ASC FOR UPDATE", nativeQuery = true)
    List<Lote> findLotesDisponiblesFEFOConLock(@Param("productoId") Integer productoId);

    @Query(value = "SELECT l.* FROM lotes l WHERE l.id = :id FOR UPDATE", nativeQuery = true)
    Lote findByIdConLock(@Param("id") Integer id);

    @Query("SELECT l FROM Lote l JOIN FETCH l.producto p JOIN FETCH p.categoria " +
           "WHERE l.cantidadActual > 0 AND l.fechaCaducidad IS NOT NULL AND l.fechaCaducidad <= :fechaLimite " +
           "ORDER BY l.fechaCaducidad ASC")
    List<Lote> findLotesPorVencer(@Param("fechaLimite") LocalDate fechaLimite);

    @Query("SELECT c.nombre AS categoria, COALESCE(SUM(l.cantidadActual * l.costoCompra), 0) AS valor, COUNT(l.id) AS lotes " +
           "FROM Lote l JOIN l.producto p JOIN p.categoria c " +
           "GROUP BY c.nombre ORDER BY valor DESC")
    List<ValorizacionCategoriaProjection> valorizarPorCategoria();
}
