package com.abarrotes.oakapi.repository;

import com.abarrotes.oakapi.model.TurnoCaja;
import com.abarrotes.oakapi.model.enums.EstadoTurno;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;

@Repository
public interface TurnoCajaRepository extends JpaRepository<TurnoCaja, Integer> {

    boolean existsByUsuarioIdAndEstado(Integer usuarioId, EstadoTurno estado);

    Optional<TurnoCaja> findFirstByUsuarioIdAndEstadoOrderByIdDesc(Integer usuarioId, EstadoTurno estado);

    Optional<TurnoCaja> findById(Integer id);

    @Query("SELECT t FROM TurnoCaja t WHERE " +
           "(:estado IS NULL OR t.estado = :estado) AND " +
           "(:usuarioId IS NULL OR t.usuario.id = :usuarioId) AND " +
           "(:fechaDesde IS NULL OR t.fechaApertura >= :fechaDesde) AND " +
           "(:fechaHasta IS NULL OR t.fechaApertura < :fechaHasta) " +
           "ORDER BY t.fechaApertura DESC")
    Page<TurnoCaja> buscarConFiltros(@Param("estado") EstadoTurno estado,
                                     @Param("usuarioId") Integer usuarioId,
                                     @Param("fechaDesde") LocalDateTime fechaDesde,
                                     @Param("fechaHasta") LocalDateTime fechaHasta,
                                     Pageable pageable);
}