package com.abarrotes.oakapi.repository;

import com.abarrotes.oakapi.model.Serie;
import com.abarrotes.oakapi.model.enums.EstadoSerie;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SerieRepository extends JpaRepository<Serie, Integer> {

    Optional<Serie> findByCodigoSerie(String codigoSerie);

    boolean existsByCodigoSerie(String codigoSerie);

    List<Serie> findByProductoIdAndEstado(Integer productoId, EstadoSerie estado);

    @Query("SELECT s FROM Serie s " +
           "WHERE s.producto.id = :productoId " +
           "AND s.estado = :estado " +
           "ORDER BY s.lote.fechaRecepcion ASC, s.id ASC")
    List<Serie> findDisponiblesFIFOByProducto(@Param("productoId") Integer productoId, @Param("estado") EstadoSerie estado);

    List<Serie> findByLoteId(Integer loteId);

    List<Serie> findByVentaId(String ventaId);

    @Query("SELECT s FROM Serie s " +
           "WHERE s.producto.id = :productoId " +
           "AND s.estado = :estado " +
           "ORDER BY s.lote.fechaRecepcion ASC, s.id ASC")
    List<Serie> findDisponiblesFIFOByProductoLimit(@Param("productoId") Integer productoId, @Param("estado") EstadoSerie estado, org.springframework.data.domain.Pageable pageable);
}