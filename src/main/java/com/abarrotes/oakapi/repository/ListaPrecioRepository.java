package com.abarrotes.oakapi.repository;

import com.abarrotes.oakapi.model.ListaPrecio;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface ListaPrecioRepository extends JpaRepository<ListaPrecio, Integer> {

    List<ListaPrecio> findByActivaTrue();

    List<ListaPrecio> findByEsDefaultCanalTrue();

    @Query("SELECT lp FROM ListaPrecio lp " +
           "WHERE lp.activa = true " +
           "AND lp.vigenciaInicio <= :fecha " +
           "AND (lp.vigenciaFin IS NULL OR lp.vigenciaFin >= :fecha)")
    List<ListaPrecio> findVigentes(@Param("fecha") LocalDate fecha);

    @Query("SELECT lp FROM ListaPrecio lp " +
           "WHERE lp.activa = true " +
           "AND lp.vigenciaInicio <= :fecha " +
           "AND (lp.vigenciaFin IS NULL OR lp.vigenciaFin >= :fecha)")
    Page<ListaPrecio> findVigentes(@Param("fecha") LocalDate fecha, Pageable pageable);

    Optional<ListaPrecio> findByNombreAndActivaTrue(String nombre);

    boolean existsByNombreAndActivaTrue(String nombre);
}