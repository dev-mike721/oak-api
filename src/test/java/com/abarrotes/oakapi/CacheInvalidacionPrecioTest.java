package com.abarrotes.oakapi;

import com.abarrotes.oakapi.dto.request.ClientePrecioEspecialRequestDto;
import com.abarrotes.oakapi.dto.request.ProductoRequestDto;
import com.abarrotes.oakapi.model.Categoria;
import com.abarrotes.oakapi.model.Cliente;
import com.abarrotes.oakapi.model.Producto;
import com.abarrotes.oakapi.model.enums.UnidadMedida;
import com.abarrotes.oakapi.repository.CategoriaRepository;
import com.abarrotes.oakapi.repository.ClienteRepository;
import com.abarrotes.oakapi.repository.ProductoRepository;
import com.abarrotes.oakapi.service.ClienteService;
import com.abarrotes.oakapi.service.impl.PrecioResolverServiceImpl;
import com.abarrotes.oakapi.service.ProductoService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.annotation.DirtiesContext;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Regresion B4: ClientePrecioEspecialServiceImpl fue eliminado por estar sin
 * inyeccion, pero era el UNICO lugar que invalidaba la cache de precios
 * resueltos. El camino vivo, ClienteServiceImpl, no la invalida: tras dar de
 * alta un precio especial el resolver seguia devolviendo el precio anterior
 * durante el TTL de 5 minutos, es decir, se cobraba el precio viejo.
 *
 * El TTL de 5 minutos hacia que esto fuera practicamente invisible en pruebas
 * manuales y muy costoso en caja.
 */
@SpringBootTest
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
@Import(TestDatabaseConfig.class)
class CacheInvalidacionPrecioTest {

    private static final String SUFIJO = String.valueOf(System.nanoTime());

    @Autowired
    private PrecioResolverServiceImpl precioResolverService;
    @Autowired
    private ClienteService clienteService;
    @Autowired
    private ProductoService productoService;
    @Autowired
    private ClienteRepository clienteRepository;
    @Autowired
    private ProductoRepository productoRepository;
    @Autowired
    private CategoriaRepository categoriaRepository;

    private Integer clienteId;
    private Integer productoId;

    private ClientePrecioEspecialRequestDto precioEspecial(BigDecimal precio) {
        ClientePrecioEspecialRequestDto dto = new ClientePrecioEspecialRequestDto();
        dto.setProductoId(productoId);
        dto.setPrecio(precio);
        dto.setVigenciaInicio(LocalDate.now().minusDays(1));
        return dto;
    }

    private ProductoRequestDto productoRequest(String nombre, BigDecimal precioBase) {
        ProductoRequestDto dto = new ProductoRequestDto();
        dto.setPlu("CACHE" + SUFIJO);
        dto.setNombre(nombre);
        dto.setCategoriaId(categoriaRepository.findAll().get(0).getId());
        dto.setUnidadMedida(UnidadMedida.PIEZA);
        dto.setPrecioBase(precioBase);
        return dto;
    }

    @BeforeEach
    void sembrar() {
        Categoria categoria = categoriaRepository.findAll().get(0);

        Producto producto = productoRepository.save(Producto.builder()
                .plu("CACHE" + SUFIJO)
                .nombre("Producto Cache Test")
                .categoria(categoria)
                .unidadMedida(UnidadMedida.PIEZA)
                .precioBase(new BigDecimal("100.00"))
                .esCompuesto(false)
                .activo(true)
                .build());
        productoId = producto.getId();

        Cliente cliente = clienteRepository.save(Cliente.builder()
                .nombre("Cliente Cache Test")
                .rfc("CCH" + String.valueOf(System.currentTimeMillis()).substring(0, 9))
                .regimenFiscal(com.abarrotes.oakapi.model.enums.RegimenFiscal.GENERAL_LEY_PERSONAS_MORALES)
                .limiteCredito(new BigDecimal("10000"))
                .diasCredito(30)
                .activo(true)
                .build());
        clienteId = cliente.getId();
    }

    private BigDecimal resolverAhora() {
        return precioResolverService
                .resolvePrecio(clienteId, productoId, BigDecimal.ONE, LocalDate.now())
                .getPrecioUnitario();
    }

    @Test
    void resolverUsaElPrecioBaseAlPrincipio() {
        assertEquals(0, new BigDecimal("100.00").compareTo(resolverAhora()));
    }

    /** El caso que rompia: crear el precio especial debe verse de inmediato. */
    @Test
    void crearPrecioEspecialInvalidaLaCacheAlInstante() {
        assertEquals(0, new BigDecimal("100.00").compareTo(resolverAhora()), "Cacheado a 100");

        clienteService.crearPrecioEspecial(clienteId, precioEspecial(new BigDecimal("75.00")));

        assertEquals(0, new BigDecimal("75.00").compareTo(resolverAhora()),
                "Debe aplicar el precio especial de inmediato, sin esperar el TTL de 5 minutos");
    }

    /** Dar de baja el precio especial tambien debe verse al instante. */
    @Test
    void eliminarPrecioEspecialInvalidaLaCacheAlInstante() {
        clienteService.crearPrecioEspecial(clienteId, precioEspecial(new BigDecimal("75.00")));
        assertEquals(0, new BigDecimal("75.00").compareTo(resolverAhora()));

        clienteService.eliminarPrecioEspecial(clienteId, productoId);

        assertEquals(0, new BigDecimal("100.00").compareTo(resolverAhora()),
                "Sin precio especial debe volver al precio base de inmediato");
    }

    /**
     * Segunda ruta del mismo tipo: cambiar el precio base del producto alimenta
     * la resolucion con origen BASE para todos los clientes sin lista ni
     * precio especial, y esa cache tampoco se invalidaba.
     */
    @Test
    void cambiarPrecioBaseInvalidaLaCacheDeTodosLosClientes() {
        assertEquals(0, new BigDecimal("100.00").compareTo(resolverAhora()));

        productoService.actualizarProducto(productoId, productoRequest("Producto Cache Test", new BigDecimal("120.00")));

        assertEquals(0, new BigDecimal("120.00").compareTo(resolverAhora()),
                "El nuevo precio base debe verse de inmediato");
    }

    /** Cambios que NO tocan el precio base no deben tirar la cache (evita thrashing). */
    @Test
    void cambiarSoloElNombreNoInvalidaLaCache() {
        resolverAhora();
        int entradasAntes = precioResolverService.getCache().asMap().size();

        productoService.actualizarProducto(productoId, productoRequest("Producto Renombrado", new BigDecimal("100.00")));

        assertEquals(entradasAntes, precioResolverService.getCache().asMap().size(),
                "Sin cambio de precio la entrada debe seguir cacheada");
    }
}