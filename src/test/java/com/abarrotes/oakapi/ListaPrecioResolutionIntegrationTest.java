package com.abarrotes.oakapi;

import com.abarrotes.oakapi.model.Categoria;
import com.abarrotes.oakapi.model.Cliente;
import com.abarrotes.oakapi.model.ListaPrecio;
import com.abarrotes.oakapi.model.ListaPrecioDetalle;
import com.abarrotes.oakapi.model.Producto;
import com.abarrotes.oakapi.model.Usuario;
import com.abarrotes.oakapi.model.enums.RegimenFiscal;
import com.abarrotes.oakapi.model.enums.UnidadMedida;
import com.abarrotes.oakapi.repository.CategoriaRepository;
import com.abarrotes.oakapi.repository.ClienteRepository;
import com.abarrotes.oakapi.repository.ListaPrecioDetalleRepository;
import com.abarrotes.oakapi.repository.ListaPrecioRepository;
import com.abarrotes.oakapi.repository.ProductoRepository;
import com.abarrotes.oakapi.repository.UsuarioRepository;
import com.abarrotes.oakapi.service.ListaPrecioService;
import com.abarrotes.oakapi.service.PrecioResolverService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.Import;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.annotation.Rollback;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.annotation.Rollback;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

import org.springframework.security.test.context.support.WithMockUser;

@SpringBootTest
@Transactional
@Rollback(false)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
@Import(TestDatabaseConfig.class)
@WithMockUser(roles = "ADMINISTRADOR")
class ListaPrecioResolutionIntegrationTest {

    @Autowired
    private PrecioResolverService precioResolverService;
    @Autowired
    private ListaPrecioService listaPrecioService;
    @Autowired
    private ListaPrecioRepository listaPrecioRepository;
    @Autowired
    private ListaPrecioDetalleRepository listaPrecioDetalleRepository;
    @Autowired
    private ClienteRepository clienteRepository;
    @Autowired
    private ProductoRepository productoRepository;
    @Autowired
    private CategoriaRepository categoriaRepository;
    @Autowired
    private UsuarioRepository usuarioRepository;

    private Integer clienteId;
    private Integer productoId;
    private Integer listaPrecioId;
    private Integer listaCanalId;
    private Integer usuarioId;

    @BeforeEach
    void setUp() {
        Categoria categoria = categoriaRepository.findAll().get(0);

        Producto producto = productoRepository.save(Producto.builder()
                .plu("PREC" + System.nanoTime())
                .nombre("Producto Precio Test")
                .categoria(categoria)
                .unidadMedida(UnidadMedida.PIEZA)
                .precioBase(new BigDecimal("100.00"))
                .esCompuesto(false)
                .activo(true)
                .build());
        productoId = producto.getId();

        Usuario usuario = usuarioRepository.save(Usuario.builder()
                .nombre("Tester Precio")
                .username("precio_" + System.nanoTime())
                .passwordHash("$2a$10$test")
                .rol(com.abarrotes.oakapi.model.enums.Rol.ADMINISTRADOR)
                .activo(true)
                .build());
        usuarioId = usuario.getId();

        // Lista de precios del cliente
        ListaPrecio listaCliente = listaPrecioRepository.save(ListaPrecio.builder()
                .nombre("Lista Cliente Test")
                .descripcion("Lista de precios específica del cliente")
                .vigenciaInicio(LocalDate.now().minusDays(30))
                .activa(true)
                .build());
        listaPrecioId = listaCliente.getId();

        listaPrecioDetalleRepository.save(ListaPrecioDetalle.builder()
                .listaPrecio(listaCliente)
                .producto(productoRepository.findById(productoId).orElseThrow())
                .precioBase(new BigDecimal("88.00"))
                .volMin1(5)
                .precio1(new BigDecimal("83.00"))
                .build());

        // Cliente con lista de precios asignada
        Cliente cliente = clienteRepository.save(Cliente.builder()
                .nombre("Cliente Precio Test")
                .rfc("PRCT" + String.valueOf(System.currentTimeMillis()).substring(0, 9))
                .regimenFiscal(com.abarrotes.oakapi.model.enums.RegimenFiscal.GENERAL_LEY_PERSONAS_MORALES)
                .listaPrecio(listaCliente)
                .limiteCredito(new BigDecimal("10000"))
                .diasCredito(30)
                .activo(true)
                .build());
        clienteId = cliente.getId();
    }

    private void createListaCanal() {
        // Lista de precios del canal (default)
        ListaPrecio listaCanal = listaPrecioRepository.save(ListaPrecio.builder()
                .nombre("Lista Canal Default")
                .descripcion("Lista de precios por defecto del canal")
                .vigenciaInicio(LocalDate.now().minusDays(30))
                .activa(true)
                .esDefaultCanal(true)
                .build());
        listaCanalId = listaCanal.getId();

        // Detalles con escalones
        listaPrecioDetalleRepository.save(ListaPrecioDetalle.builder()
                .listaPrecio(listaCanal)
                .producto(productoRepository.findById(productoId).orElseThrow())
                .precioBase(new BigDecimal("90.00"))
                .volMin1(10)
                .precio1(new BigDecimal("85.00"))
                .volMin2(50)
                .precio2(new BigDecimal("80.00"))
                .volMin3(100)
                .precio3(new BigDecimal("75.00"))
                .build());
    }

    @Test
    void prioridad1PrecioBaseProducto() {
        // Sin listas asignadas (ni cliente ni canal), debe usar precio base del producto
        var precio = precioResolverService.resolvePrecio(null, productoId, BigDecimal.ONE, LocalDate.now());
        assertEquals(new BigDecimal("100.00"), precio.getPrecioUnitario());
        assertEquals("BASE", precio.getOrigen());
    }

    @Test
    void prioridad2ListaCanalDefault() {
        // Con lista de precios del canal default
        createListaCanal();
        var precio = precioResolverService.resolvePrecio(null, productoId, BigDecimal.ONE, LocalDate.now());
        assertEquals("LISTA_CANAL", precio.getOrigen());
        assertEquals(new BigDecimal("90.00"), precio.getPrecioUnitario());
    }

    @Test
    void prioridad3ListaCliente() {
        // Cliente con lista asignada
        var precio = precioResolverService.resolvePrecio(clienteId, productoId, BigDecimal.ONE, LocalDate.now());
        assertEquals("LISTA_CLIENTE", precio.getOrigen());
        assertEquals(new BigDecimal("88.00"), precio.getPrecioUnitario());
        assertEquals(listaPrecioId, precio.getListaPrecioId());
    }

    @Test
    void escalonesVolumenListaCanal() {
        createListaCanal();
        // Cantidad 1 -> precio base
        var precio1 = precioResolverService.resolvePrecio(null, productoId, BigDecimal.ONE, LocalDate.now());
        assertEquals(new BigDecimal("90.00"), precio1.getPrecioUnitario());
        assertEquals(0, precio1.getEscalonAplicado());

        // Cantidad 10 -> escalón 1
        var precio10 = precioResolverService.resolvePrecio(null, productoId, new BigDecimal("10"), LocalDate.now());
        assertEquals(new BigDecimal("85.00"), precio10.getPrecioUnitario());
        assertEquals(1, precio10.getEscalonAplicado());

        // Cantidad 50 -> escalón 2
        var precio50 = precioResolverService.resolvePrecio(null, productoId, new BigDecimal("50"), LocalDate.now());
        assertEquals(new BigDecimal("80.00"), precio50.getPrecioUnitario());
        assertEquals(2, precio50.getEscalonAplicado());

        // Cantidad 100 -> escalón 3
        var precio100 = precioResolverService.resolvePrecio(null, productoId, new BigDecimal("100"), LocalDate.now());
        assertEquals(new BigDecimal("75.00"), precio100.getPrecioUnitario());
        assertEquals(3, precio100.getEscalonAplicado());
    }

    @Test
    void escalonesVolumenListaCliente() {
        var precio1 = precioResolverService.resolvePrecio(clienteId, productoId, BigDecimal.ONE, LocalDate.now());
        assertEquals(new BigDecimal("88.00"), precio1.getPrecioUnitario());

        var precio5 = precioResolverService.resolvePrecio(clienteId, productoId, new BigDecimal("5"), LocalDate.now());
        assertEquals(new BigDecimal("83.00"), precio5.getPrecioUnitario());
        assertEquals(1, precio5.getEscalonAplicado());
    }
}