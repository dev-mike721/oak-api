package com.abarrotes.oakapi.repository;

import com.abarrotes.oakapi.model.ProductoCompuesto;
import com.abarrotes.oakapi.model.ProductoCompuestoId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProductoCompuestoRepository extends JpaRepository<ProductoCompuesto, ProductoCompuestoId> {

    @Query("SELECT pc FROM ProductoCompuesto pc JOIN FETCH pc.productoHijo " +
           "WHERE pc.productoPadre.id = :productoPadreId ORDER BY pc.productoHijo.nombre")
    List<ProductoCompuesto> findByProductoPadreId(@Param("productoPadreId") Integer productoPadreId);

    boolean existsByIdProductoPadreIdAndIdProductoHijoId(Integer productoPadreId, Integer productoHijoId);

    void deleteByIdProductoPadreId(Integer productoPadreId);
}
