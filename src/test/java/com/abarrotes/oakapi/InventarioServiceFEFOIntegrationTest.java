package com.abarrotes.oakapi;

import com.abarrotes.oakapi.exception.BusinessRuleException;
import com.abarrotes.oakapi.model.Categoria;
import com.abarrotes.oakapi.model.Lote;
import com.abarrotes.oakapi.model.Producto;
import com.abarrotes.oakapi.model.Usuario;
import com.abarrotes.oakapi.model.enums.TipoMovimientoInventario;
import com.abarrotes.oakapi.model.enums.UnidadMedida;
import com.abarrotes.oakapi.repository.CategoriaRepository;
import com.abarrotes.oakapi.repository.LoteRepository;
import com.abarrotes.oakapi.repository.MovimientoInventarioRepository;
import com.abarrotes.oakapi.repository.ProductoRepository;
import com.abarrotes.oakapi.repository.UsuarioRepository;
import com.abarrotes.oakapi.service.InventarioService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.test.annotation.Rollback;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
@Import(TestDatabaseConfig.class)
class InventarioServiceFEFOIntegrationTest {

    private static final String SUFIJO = String.valueOf(System.nanoTime());

    @Autowired
    private InventarioService inventarioService;
    @Autowired
    private ProductoRepository productoRepository;
    @Autowired
    private CategoriaRepository categoriaRepository;
    @Autowired
    private LoteRepository loteRepository;
    @Autowired
    private UsuarioRepository usuarioRepository;
    @Autowired
    private MovimientoInventarioRepository movimientoInventarioRepository;

    private Integer productoId;
    private Integer loteACaducidadProximaId;
    private Integer loteBSinCaducidadId;
    private Integer usuarioId;

    @BeforeEach
    void sembrarDatos() {
        Categoria categoria = categoriaRepository.findAll().get(0);

        Producto producto = productoRepository.save(Producto.builder()
                .plu("FEFO" + SUFIJO)
                .nombre("Producto FEFO Test")
                .categoria(categoria)
                .unidadMedida(UnidadMedida.PIEZA)
                .precioBase(new BigDecimal("10.00"))
                .esCompuesto(false)
                .activo(true)
                .build());
        productoId = producto.getId();

        Lote conCaducidadProxima = loteRepository.save(Lote.builder()
                .producto(producto)
                .codigoLote("FEFO-A-" + SUFIJO)
                .costoCompra(new BigDecimal("8.00"))
                .cantidadInicial(new BigDecimal("4"))
                .cantidadActual(new BigDecimal("4"))
                .fechaRecepcion(LocalDate.now().minusDays(10))
                .fechaCaducidad(LocalDate.now().plusDays(5))
                .build());
        loteACaducidadProximaId = conCaducidadProxima.getId();

        Lote sinCaducidad = loteRepository.save(Lote.builder()
                .producto(producto)
                .codigoLote("FEFO-B-" + SUFIJO)
                .costoCompra(new BigDecimal("7.00"))
                .cantidadInicial(new BigDecimal("6"))
                .cantidadActual(new BigDecimal("6"))
                .fechaRecepcion(LocalDate.now().minusDays(30))
                .fechaCaducidad(null)
                .build());
        loteBSinCaducidadId = sinCaducidad.getId();

        Usuario usuario = usuarioRepository.save(Usuario.builder()
                .nombre("Tester FEFO")
                .username("fefo_" + SUFIJO)
                .passwordHash("x")
                .rol(com.abarrotes.oakapi.model.enums.Rol.ADMINISTRADOR)
                .activo(true)
                .build());
        usuarioId = usuario.getId();
    }

    @Test
    void consumePrimeroElLoteConCaducidadMasProxima() {
        List<?> movimientos = inventarioService.consumirStockFEFO(
                productoId, new BigDecimal("6"), usuarioId, null);

        Lote loteA = loteRepository.findById(loteACaducidadProximaId).orElseThrow();
        Lote loteB = loteRepository.findById(loteBSinCaducidadId).orElseThrow();

        assertEquals(0, loteA.getCantidadActual().compareTo(BigDecimal.ZERO),
                "El lote con caducidad próxima debe agotarse primero");
        assertEquals(0, loteB.getCantidadActual().compareTo(new BigDecimal("4")),
                "El lote sin caducidad debe absorber el faltante");
        assertEquals(2, movimientos.size(), "Deben generarse 2 movimientos (uno por lote)");
    }

    @Test
    void generaMovimientosConSnapshotCorrecto() {
        inventarioService.consumirStockFEFO(productoId, new BigDecimal("6"), usuarioId, "f47ac10b-58cc-4372-a567-0e02b2c3d479");

        List<com.abarrotes.oakapi.model.MovimientoInventario> salidas =
                movimientoInventarioRepository.findAllConLoteYProducto().stream()
                        .filter(m -> m.getTipoMovimiento() == TipoMovimientoInventario.SALIDA_VENTA)
                        .filter(m -> m.getLote().getProducto().getId().equals(productoId))
                        .toList();

        assertEquals(2, salidas.size());
        salidas.forEach(m -> {
            assertEquals(TipoMovimientoInventario.SALIDA_VENTA, m.getTipoMovimiento());
            assertEquals(com.abarrotes.oakapi.model.enums.DocumentoOrigen.VENTA, m.getDocumentoTipo());
            assertEquals("f47ac10b-58cc-4372-a567-0e02b2c3d479", m.getDocumentoId());
            assertEquals(0, m.getCantidadAnterior().subtract(m.getCantidad()).compareTo(m.getCantidadNueva()),
                    "Debe cumplirse anterior - cantidad = nueva en cada movimiento");
        });
    }

    @Test
    void rechazaConsumoSinStockSuficienteSinAlterarInventario() {
        BigDecimal stockA = loteRepository.findById(loteACaducidadProximaId).orElseThrow().getCantidadActual();
        BigDecimal stockB = loteRepository.findById(loteBSinCaducidadId).orElseThrow().getCantidadActual();

        assertThrows(BusinessRuleException.class,
                () -> inventarioService.consumirStockFEFO(productoId, new BigDecimal("99"), usuarioId, null));

        assertEquals(stockA, loteRepository.findById(loteACaducidadProximaId).orElseThrow().getCantidadActual(),
                "El stock del lote A no debe alterarse ante stock insuficiente");
        assertEquals(stockB, loteRepository.findById(loteBSinCaducidadId).orElseThrow().getCantidadActual(),
                "El stock del lote B no debe alterarse ante stock insuficiente");
        assertEquals(0, contarSalidasDelProducto(), "No debe quedar ningún movimiento registrado");
    }

    private long contarSalidasDelProducto() {
        return movimientoInventarioRepository.findAllConLoteYProducto().stream()
                .filter(m -> m.getTipoMovimiento() == TipoMovimientoInventario.SALIDA_VENTA)
                .filter(m -> m.getLote().getProducto().getId().equals(productoId))
                .count();
    }
}
