package com.abarrotes.oakapi.repository;

import com.abarrotes.oakapi.model.ClientePrecioEspecial;
import com.abarrotes.oakapi.model.ClientePrecioEspecialId;
import com.abarrotes.oakapi.model.Producto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface ClientePrecioEspecialRepository extends JpaRepository<ClientePrecioEspecial, ClientePrecioEspecialId> {

    Optional<ClientePrecioEspecial> findByClienteIdAndProductoIdAndVigenciaFinIsNullOrVigenciaFinAfter(
            Integer clienteId, Integer productoId, LocalDateTime ahora);

    @Query("SELECT cpe FROM ClientePrecioEspecial cpe " +
           "WHERE cpe.cliente.id = :clienteId " +
           "AND cpe.producto.id = :productoId " +
           "AND cpe.vigenciaInicio <= :ahora " +
           "AND (cpe.vigenciaFin IS NULL OR cpe.vigenciaFin >= :ahora)")
    Optional<ClientePrecioEspecial> findVigenteByClienteAndProducto(
            @Param("clienteId") Integer clienteId,
            @Param("productoId") Integer productoId,
            @Param("ahora") LocalDateTime ahora);

    List<ClientePrecioEspecial> findByClienteId(Integer clienteId);

    Optional<ClientePrecioEspecial> findByIdClienteIdAndIdProductoId(Integer clienteId, Integer productoId);

    @Query("SELECT cpe FROM ClientePrecioEspecial cpe " +
           "WHERE cpe.cliente.id = :clienteId " +
           "AND cpe.producto.id = :productoId " +
           "AND cpe.vigenciaInicio <= :nuevaVigenciaInicio " +
           "AND (cpe.vigenciaFin IS NULL OR cpe.vigenciaFin >= :nuevaVigenciaInicio)")
    List<ClientePrecioEspecial> findOverlappingByClienteAndProducto(
            @Param("clienteId") Integer clienteId,
            @Param("productoId") Integer productoId,
            @Param("nuevaVigenciaInicio") LocalDateTime nuevaVigenciaInicio);
}