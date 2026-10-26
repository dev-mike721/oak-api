package com.abarrotes.oakapi.repository;

import com.abarrotes.oakapi.model.ListaPrecioDetalle;
import com.abarrotes.oakapi.model.ListaPrecioDetalleId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ListaPrecioDetalleRepository extends JpaRepository<ListaPrecioDetalle, ListaPrecioDetalleId> {

    Optional<ListaPrecioDetalle> findByListaPrecioIdAndProductoId(Integer listaPrecioId, Integer productoId);

    boolean existsByListaPrecioIdAndProductoId(Integer listaPrecioId, Integer productoId);

    List<ListaPrecioDetalle> findByListaPrecioId(Integer listaPrecioId);
}