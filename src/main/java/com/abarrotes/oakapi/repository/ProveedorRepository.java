package com.abarrotes.oakapi.repository;

import com.abarrotes.oakapi.model.Proveedor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ProveedorRepository extends JpaRepository<Proveedor, Integer> {

    Optional<Proveedor> findByIdAndActivoTrue(Integer id);

    Optional<Proveedor> findByRfcAndActivoTrue(String rfc);

    Page<Proveedor> findByActivoTrue(Pageable pageable);

    Page<Proveedor> findByActivoTrueAndRazonSocialContainingIgnoreCase(String termino, Pageable pageable);

    boolean existsByRfc(String rfc);

    boolean existsByRfcAndIdNot(String rfc, Integer id);
}