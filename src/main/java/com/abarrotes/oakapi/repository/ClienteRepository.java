package com.abarrotes.oakapi.repository;

import com.abarrotes.oakapi.model.Cliente;
import com.abarrotes.oakapi.model.Venta;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface ClienteRepository extends JpaRepository<Cliente, Integer> {

    Optional<Cliente> findByRfc(String rfc);

    boolean existsByRfc(String rfc);

    boolean existsByEmail(String email);

    List<Cliente> findByActivoTrue();

    Optional<Cliente> findByIdAndActivoTrue(Integer id);

    List<Cliente> findByListaPrecioId(Integer listaPrecioId);

    @Query("SELECT c FROM Cliente c WHERE c.saldoPendiente > 0")
    List<Cliente> findClientesConSaldoPendiente();

    @Query("SELECT c FROM Cliente c JOIN c.ventas v WHERE c.activo = true AND c.saldoPendiente > 0 AND v.fechaVencimiento < :fecha AND v.estado = 'COMPLETADA' AND v.metodoPago = 'CREDITO'")
    List<Cliente> findClientesVencidos(@Param("fecha") LocalDate fecha);
}