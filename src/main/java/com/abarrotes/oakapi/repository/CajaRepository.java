package com.abarrotes.oakapi.repository;

import com.abarrotes.oakapi.model.Caja;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CajaRepository extends JpaRepository<Caja, Integer> {

    List<Caja> findByActivaTrue();

    Optional<Caja> findByNombre(String nombre);

    boolean existsByNombre(String nombre);
}