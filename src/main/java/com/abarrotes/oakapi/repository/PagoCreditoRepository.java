package com.abarrotes.oakapi.repository;

import com.abarrotes.oakapi.model.PagoCredito;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Repository
public interface PagoCreditoRepository extends JpaRepository<PagoCredito, Integer> {

    List<PagoCredito> findByClienteId(Integer clienteId);

    List<PagoCredito> findByClienteIdAndAplicadoFalse(Integer clienteId);

    @Query("SELECT pc FROM PagoCredito pc " +
           "WHERE pc.cliente.id = :clienteId " +
           "AND pc.anulado = false " +
           "ORDER BY pc.fecha DESC")
    List<PagoCredito> findByClienteOrderByFechaDesc(@Param("clienteId") Integer clienteId);

    @Modifying
    @Transactional
    @Query(value = """
        UPDATE ventas v
        JOIN (
            SELECT v2.uuid, v2.saldo_pendiente,
                   ROW_NUMBER() OVER (ORDER BY v2.fecha_vencimiento IS NULL,
                                               v2.fecha_vencimiento ASC,
                                               v2.fecha_hora ASC) as rn
            FROM ventas v2
            WHERE v2.cliente_id = :clienteId
              AND v2.estado = 'COMPLETADA'
              AND v2.metodo_pago = 'CREDITO'
              AND v2.saldo_pendiente > 0
        ) ordered_ventas ON v.uuid = ordered_ventas.uuid
        SET v.saldo_pendiente = v.saldo_pendiente - LEAST(v.saldo_pendiente, :montoDisponible)
        WHERE :montoDisponible > 0
        """, nativeQuery = true)
    int aplicarFIFO(@Param("clienteId") Integer clienteId, @Param("montoDisponible") java.math.BigDecimal montoDisponible);

    // Alternative: select for update to get ordered ventas
    @Query(value = """
        SELECT v.* FROM ventas v
        WHERE v.cliente_id = :clienteId
          AND v.estado = 'COMPLETADA'
          AND v.metodo_pago = 'CREDITO'
          AND v.saldo_pendiente > 0
        ORDER BY v.fecha_vencimiento IS NULL, v.fecha_vencimiento ASC, v.fecha_hora ASC
        FOR UPDATE
        """, nativeQuery = true)
    List<Object[]> findVentasPendientesFIFOForUpdate(@Param("clienteId") Integer clienteId);
}