package com.abarrotes.oakapi.repository;

import com.abarrotes.oakapi.model.Impuesto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ImpuestoRepository extends JpaRepository<Impuesto, Integer> {

    Optional<Impuesto> findByClave(String clave);

    boolean existsByClave(String clave);

    List<Impuesto> findByActivoTrue();

    Optional<Impuesto> findByClaveAndActivoTrue(String clave);
}