package com.abarrotes.oakapi.repository;

import com.abarrotes.oakapi.model.MovimientoInventario;
import com.abarrotes.oakapi.model.enums.TipoMovimientoInventario;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface MovimientoInventarioRepository extends JpaRepository<MovimientoInventario, Long> {

    @Query("SELECT m FROM MovimientoInventario m " +
           "WHERE (:loteId IS NULL OR m.lote.id = :loteId) " +
           "AND (:productoId IS NULL OR m.lote.producto.id = :productoId) " +
           "AND (:tipo IS NULL OR m.tipoMovimiento = :tipo) ")
    Page<MovimientoInventario> buscarConFiltros(@Param("loteId") Integer loteId,
                                                @Param("productoId") Integer productoId,
                                                @Param("tipo") TipoMovimientoInventario tipo,
                                                Pageable pageable);
}
