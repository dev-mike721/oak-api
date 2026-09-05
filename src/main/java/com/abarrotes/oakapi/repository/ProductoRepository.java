package com.abarrotes.oakapi.repository;
import com.abarrotes.oakapi.model.Producto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface ProductoRepository extends JpaRepository<Producto, Integer> { // <-- antes era Long
    Optional<Producto> findByPluAndActivoTrue(String plu);
    Optional<Producto> findByIdAndActivoTrue(Integer id);
    List<Producto> findByActivoTrue();
    List<Producto> findByCategoriaIdAndActivoTrue(Integer categoriaId);
    List<Producto> findByNombreContainingIgnoreCaseAndActivoTrue(String nombre);
    boolean existsByPlu(String plu);
    boolean existsByPluAndIdNot(String plu, Integer id);
}