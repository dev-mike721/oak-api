package com.abarrotes.oakapi;

import com.abarrotes.oakapi.dto.request.CategoriaRequestDto;
import com.abarrotes.oakapi.dto.response.CategoriaDto;
import com.abarrotes.oakapi.exception.BusinessRuleException;
import com.abarrotes.oakapi.exception.DuplicateResourceException;
import com.abarrotes.oakapi.exception.ResourceNotFoundException;
import com.abarrotes.oakapi.model.Categoria;
import com.abarrotes.oakapi.model.Producto;
import com.abarrotes.oakapi.model.enums.UnidadMedida;
import com.abarrotes.oakapi.repository.CategoriaRepository;
import com.abarrotes.oakapi.repository.ProductoRepository;
import com.abarrotes.oakapi.service.CategoriaService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.annotation.DirtiesContext;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
@Import(TestDatabaseConfig.class)
class CategoriaServiceIntegrationTest {

    private static final String SUFIJO = String.valueOf(System.nanoTime());

    @Autowired
    private CategoriaService categoriaService;
    @Autowired
    private CategoriaRepository categoriaRepository;
    @Autowired
    private ProductoRepository productoRepository;

    private CategoriaRequestDto request(String nombre) {
        CategoriaRequestDto dto = new CategoriaRequestDto();
        dto.setNombre(nombre);
        dto.setDescripcion("Categoría de prueba");
        return dto;
    }

    @Test
    void creaCategoriaConIdNumerico() {
        CategoriaDto creada = categoriaService.crear(request("Bebidas " + SUFIJO));

        assertNotNull(creada.getId());
        assertEquals("Bebidas " + SUFIJO, creada.getNombre());
        assertEquals("Categoría de prueba", creada.getDescripcion());
    }

    @Test
    void elIdEsIntegerYNoLong() {
        CategoriaDto creada = categoriaService.crear(request("Refrescos " + SUFIJO));

        // El DTO expone Integer (alineado con Categoria.id y con la columna int)
        Integer id = creada.getId();
        assertEquals(categoriaRepository.findById(id).orElseThrow().getId(), id);
    }

    @Test
    void noPermiteNombreDuplicado() {
        String nombre = "Lácteos " + SUFIJO;
        categoriaService.crear(request(nombre));

        assertThrows(DuplicateResourceException.class, () -> categoriaService.crear(request(nombre)));
    }

    @Test
    void actualizaCampos() {
        CategoriaDto creada = categoriaService.crear(request("Original " + SUFIJO));

        CategoriaRequestDto edicion = new CategoriaRequestDto();
        edicion.setNombre("Editada " + SUFIJO);
        edicion.setDescripcion("Nueva descripción");

        CategoriaDto actualizada = categoriaService.actualizar(creada.getId(), edicion);

        assertEquals("Editada " + SUFIJO, actualizada.getNombre());
        assertEquals("Nueva descripción", actualizada.getDescripcion());
    }

    @Test
    void noPermiteDuplicarNombreAlActualizar() {
        String nombreA = "Alfa " + SUFIJO;
        String nombreB = "Beta " + SUFIJO;
        categoriaService.crear(request(nombreA));
        CategoriaDto b = categoriaService.crear(request(nombreB));

        // Intentamos renombrar B con el nombre que ya tiene A
        CategoriaRequestDto edicion = new CategoriaRequestDto();
        edicion.setNombre(nombreA);

        assertThrows(DuplicateResourceException.class, () -> categoriaService.actualizar(b.getId(), edicion));
    }

    @Test
    void eliminaCategoriaSinProductos() {
        CategoriaDto creada = categoriaService.crear(request("Temporal " + SUFIJO));

        categoriaService.eliminar(creada.getId());

        assertFalse(categoriaRepository.existsById(creada.getId()));
        assertThrows(ResourceNotFoundException.class, () -> categoriaService.obtenerPorId(creada.getId()));
    }

    @Test
    void noEliminaCategoriaConProductosAsociados() {
        Categoria categoria = categoriaRepository.save(Categoria.builder()
                .nombre("Con productos " + SUFIJO)
                .descripcion("No se debe eliminar")
                .build());
        productoRepository.save(Producto.builder()
                .plu("CAT" + SUFIJO)
                .nombre("Producto prueba")
                .categoria(categoria)
                .unidadMedida(UnidadMedida.PIEZA)
                .precioBase(new BigDecimal("10.00"))
                .esCompuesto(false)
                .activo(true)
                .build());

        BusinessRuleException ex = assertThrows(BusinessRuleException.class,
                () -> categoriaService.eliminar(categoria.getId()));
        assertTrue(ex.getMessage().contains("producto"));

        assertTrue(categoriaRepository.existsById(categoria.getId()));
    }

    @Test
    void listaTodasLasCategorias() {
        int antes = categoriaService.obtenerTodas().size();
        categoriaService.crear(request("Nueva " + SUFIJO));

        assertEquals(antes + 1, categoriaService.obtenerTodas().size());
    }
}