package com.abarrotes.oakapi;

import com.abarrotes.oakapi.model.Categoria;
import com.abarrotes.oakapi.model.Cliente;
import com.abarrotes.oakapi.model.Producto;
import com.abarrotes.oakapi.model.Usuario;
import com.abarrotes.oakapi.model.Venta;
import com.abarrotes.oakapi.model.enums.EstadoVenta;
import com.abarrotes.oakapi.model.enums.MetodoPago;
import com.abarrotes.oakapi.model.enums.RegimenFiscal;
import com.abarrotes.oakapi.model.enums.UnidadMedida;
import com.abarrotes.oakapi.model.Caja;
import com.abarrotes.oakapi.model.TurnoCaja;
import com.abarrotes.oakapi.repository.CajaRepository;
import com.abarrotes.oakapi.repository.CategoriaRepository;
import com.abarrotes.oakapi.repository.ClienteRepository;
import com.abarrotes.oakapi.repository.ProductoRepository;
import com.abarrotes.oakapi.repository.TurnoCajaRepository;
import com.abarrotes.oakapi.repository.UsuarioRepository;
import com.abarrotes.oakapi.repository.VentaRepository;
import org.springframework.context.annotation.Import;
import com.abarrotes.oakapi.repository.PagoCreditoRepository;
import com.abarrotes.oakapi.repository.PagoCreditoAplicacionRepository;
import com.abarrotes.oakapi.service.PagoCreditoService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.test.annotation.Rollback;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
@Rollback(false)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
@Import(TestDatabaseConfig.class)
@WithMockUser(username = "testadmin", roles = "ADMINISTRADOR")
class PagoCreditoFifoIntegrationTest {

    @Autowired
    private PagoCreditoService pagoCreditoService;
    @Autowired
    private PagoCreditoRepository pagoCreditoRepository;
    @Autowired
    private PagoCreditoAplicacionRepository pagoCreditoAplicacionRepository;
    @Autowired
    private ClienteRepository clienteRepository;
    @Autowired
    private VentaRepository ventaRepository;
    @Autowired
    private ProductoRepository productoRepository;
    @Autowired
    private CategoriaRepository categoriaRepository;
    @Autowired
    private UsuarioRepository usuarioRepository;

    private Integer clienteId;
    private Integer productoId;
    private Integer usuarioId;

    @BeforeEach
    void setUp() {
        Categoria categoria = categoriaRepository.findAll().get(0);

        Producto producto = productoRepository.save(Producto.builder()
                .plu("PAGO" + System.nanoTime())
                .nombre("Producto Pago Test")
                .categoria(categoria)
                .unidadMedida(UnidadMedida.PIEZA)
                .precioBase(new BigDecimal("100.00"))
                .esCompuesto(false)
                .activo(true)
                .build());
        productoRepository.flush();
        productoId = producto.getId();

        Usuario usuario = usuarioRepository.save(Usuario.builder()
                .nombre("Tester Pago")
                .username("testadmin")
                .passwordHash("$2a$10$test")
                .rol(com.abarrotes.oakapi.model.enums.Rol.ADMINISTRADOR)
                .activo(true)
                .build());
        usuarioRepository.flush();
        usuarioId = usuario.getId();

        // Crear caja y turno para las ventas
        Caja caja = new Caja();
        caja.setNombre("Caja Test");
        caja.setUbicacion("Test");
        caja.setActiva(true);
        caja = cajaRepository.save(caja);
        cajaRepository.flush();

        TurnoCaja turno = TurnoCaja.builder()
                .usuario(usuarioRepository.findById(usuarioId).orElseThrow())
                .caja(caja)
                .montoInicial(BigDecimal.ZERO)
                .estado(com.abarrotes.oakapi.model.enums.EstadoTurno.ABIERTO)
                .estadoSolicitud(com.abarrotes.oakapi.model.enums.EstadoSolicitudTurno.AUTORIZADO)
                .solicitadoPor(usuarioRepository.findById(usuarioId).orElseThrow())
                .fechaApertura(LocalDateTime.now())
                .build();
        this.turno = turnoRepository.save(turno);
        turnoRepository.flush();

        Cliente cliente = clienteRepository.save(Cliente.builder()
                .nombre("Cliente Pago Test")
                .rfc("PAGO" + String.valueOf(System.currentTimeMillis()).substring(0, 9))
                .regimenFiscal(com.abarrotes.oakapi.model.enums.RegimenFiscal.GENERAL_LEY_PERSONAS_MORALES)
                .limiteCredito(new BigDecimal("10000"))
                .diasCredito(30)
                .activo(true)
                .build());
        clienteRepository.flush();
        clienteId = cliente.getId();
    }

    private TurnoCaja turno;
    @Autowired
    private CajaRepository cajaRepository;
    @Autowired
    private TurnoCajaRepository turnoRepository;

    private Venta crearVentaCredito(BigDecimal total, LocalDate fechaVencimiento) {
        return crearVentaCredito(total, fechaVencimiento, LocalDateTime.now().minusDays(10));
    }

    private Venta crearVentaCredito(BigDecimal total, LocalDate fechaVencimiento, LocalDateTime fechaHora) {
        return com.abarrotes.oakapi.model.Venta.builder()
                .uuid(java.util.UUID.randomUUID().toString())
                .cliente(clienteRepository.findById(clienteId).orElseThrow())
                .usuario(usuarioRepository.findById(usuarioId).orElseThrow())
                .turno(turno)
                .fechaHora(fechaHora)
                .metodoPago(com.abarrotes.oakapi.model.enums.MetodoPago.CREDITO)
                .total(total)
                .estado(com.abarrotes.oakapi.model.enums.EstadoVenta.COMPLETADA)
                .saldoPendiente(total)
                .fechaVencimiento(fechaVencimiento)
                .build();
    }

    @Test
    void pagoParcialSeAplicaAFacturaMasAntiguaFIFO() {
        // Crear 3 facturas vencidas con fechas diferentes
        Venta v1 = ventaRepository.save(crearVentaCredito(new BigDecimal("1000.00"), LocalDate.now().minusDays(30))); // más antigua
        ventaRepository.flush();
        Venta v2 = ventaRepository.save(crearVentaCredito(new BigDecimal("500.00"), LocalDate.now().minusDays(15)));
        ventaRepository.flush();
        Venta v3 = ventaRepository.save(crearVentaCredito(new BigDecimal("2000.00"), LocalDate.now().minusDays(5)));  // más reciente
        ventaRepository.flush();

        // Registrar pago de 2000
        var pago = pagoCreditoService.registrarPago(com.abarrotes.oakapi.dto.request.PagoCreditoRequestDto.builder()
                .clienteId(clienteId)
                .monto(new BigDecimal("2000.00"))
                .metodoPago(com.abarrotes.oakapi.model.enums.MetodoPagoCredito.EFECTIVO)
                .build());

        // Aplicar FIFO
        pagoCreditoService.aplicarFIFO(pago.getId());

        // Verificar aplicaciones (v1: 1000, v2: 500, v3: 500 = 3 aplicaciones)
        var aplicaciones = pagoCreditoAplicacionRepository.findByPagoId(pago.getId());
        assertEquals(3, aplicaciones.size());

        // Verificar que v1 se cubrió totalmente (1000)
        Venta v1Actualizada = ventaRepository.findById(v1.getUuid()).orElseThrow();
        assertEquals(0, v1Actualizada.getSaldoPendiente().compareTo(BigDecimal.ZERO));

        // Verificar que v2 se cubrió totalmente (500)
        Venta v2Actualizada = ventaRepository.findById(v2.getUuid()).orElseThrow();
        assertEquals(0, v2Actualizada.getSaldoPendiente().compareTo(BigDecimal.ZERO));

        // Verificar que v3 se cubrió parcialmente (500 de 2000 = 1500 restante)
        Venta v3Actualizada = ventaRepository.findById(v3.getUuid()).orElseThrow();
        assertEquals(0, new BigDecimal("1500.00").compareTo(v3Actualizada.getSaldoPendiente()));
    }

    @Test
    void pagoExactoCierraTodasLasFacturas() {
        Venta v1 = ventaRepository.save(crearVentaCredito(new BigDecimal("1000.00"), LocalDate.now().minusDays(30)));
        ventaRepository.flush();
        Venta v2 = ventaRepository.save(crearVentaCredito(new BigDecimal("500.00"), LocalDate.now().minusDays(15)));
        ventaRepository.flush();
        Venta v3 = ventaRepository.save(crearVentaCredito(new BigDecimal("2000.00"), LocalDate.now().minusDays(5)));
        ventaRepository.flush();

        var pago = pagoCreditoService.registrarPago(com.abarrotes.oakapi.dto.request.PagoCreditoRequestDto.builder()
                .clienteId(clienteId)
                .monto(new BigDecimal("3500.00"))
                .metodoPago(com.abarrotes.oakapi.model.enums.MetodoPagoCredito.EFECTIVO)
                .build());

        pagoCreditoService.aplicarFIFO(pago.getId());

        Venta v1Actualizada = ventaRepository.findById(v1.getUuid()).orElseThrow();
        Venta v2Actualizada = ventaRepository.findById(v2.getUuid()).orElseThrow();
        Venta v3Actualizada = ventaRepository.findById(v3.getUuid()).orElseThrow();

        assertEquals(0, v1Actualizada.getSaldoPendiente().compareTo(BigDecimal.ZERO));
        assertEquals(0, v2Actualizada.getSaldoPendiente().compareTo(BigDecimal.ZERO));
        assertEquals(0, v3Actualizada.getSaldoPendiente().compareTo(BigDecimal.ZERO));

        var pagoActualizado = pagoCreditoRepository.findById(pago.getId()).orElseThrow();
        assertTrue(pagoActualizado.getAplicado());
    }

    @Test
    void anularPagoRestauraSaldos() {
        Venta v1 = ventaRepository.save(crearVentaCredito(new BigDecimal("1000.00"), LocalDate.now().minusDays(10)));
        ventaRepository.flush();

        var pago = pagoCreditoService.registrarPago(com.abarrotes.oakapi.dto.request.PagoCreditoRequestDto.builder()
                .clienteId(clienteId)
                .monto(new BigDecimal("1000.00"))
                .metodoPago(com.abarrotes.oakapi.model.enums.MetodoPagoCredito.EFECTIVO)
                .build());

        pagoCreditoService.aplicarFIFO(pago.getId());

        // Verificar que se aplicó
        Venta v1Aplicada = ventaRepository.findById(v1.getUuid()).orElseThrow();
        assertEquals(0, v1Aplicada.getSaldoPendiente().compareTo(BigDecimal.ZERO));

        // Anular pago
        pagoCreditoService.anularPago(pago.getId());

        // Verificar que se restauró el saldo
        Venta v1Restaurada = ventaRepository.findById(v1.getUuid()).orElseThrow();
        assertEquals(0, new BigDecimal("1000.00").compareTo(v1Restaurada.getSaldoPendiente()));

        var pagoAnulado = pagoCreditoRepository.findById(pago.getId()).orElseThrow();
        assertTrue(pagoAnulado.getAnulado());
    }

    @Test
    void cuentaCorrienteMuestraVentasYPagos() {
        Venta v1 = ventaRepository.save(crearVentaCredito(new BigDecimal("1000.00"), LocalDate.now().minusDays(10)));
        ventaRepository.flush();

        var pago = pagoCreditoService.registrarPago(com.abarrotes.oakapi.dto.request.PagoCreditoRequestDto.builder()
                .clienteId(clienteId)
                .monto(new BigDecimal("500.00"))
                .metodoPago(com.abarrotes.oakapi.model.enums.MetodoPagoCredito.EFECTIVO)
                .build());

        pagoCreditoService.aplicarFIFO(pago.getId());

        var cuenta = pagoCreditoService.obtenerCuentaCorriente(clienteId);

        assertEquals(clienteId, cuenta.getClienteId());
        assertEquals(1, cuenta.getVentasPendientes().size());
        assertEquals(1, cuenta.getPagos().size());
        assertEquals(0, new BigDecimal("500.00").compareTo(cuenta.getTotalVentasPendientes()));
        assertEquals(0, new BigDecimal("500.00").compareTo(cuenta.getTotalPagosAplicados()));
    }

    /**
     * Regresion B18: aplicarFIFO filtraba por "fecha_vencimiento <= CURDATE()", asi que
     * un pago anticipado no liquidaba facturas aun no vencidas y el monto se quedaba
     * sin aplicar, aunque obtenerCuentaCorriente si mostraba esa deuda.
     */
    @Test
    void pagoAnticipadoLiquidaFacturaNoVencida() {
        // Factura que vence en 30 dias: todavia no esta vencida
        Venta futura = ventaRepository.save(crearVentaCredito(new BigDecimal("1000.00"),
                LocalDate.now().plusDays(30)));
        ventaRepository.flush();

        var pago = pagoCreditoService.registrarPago(com.abarrotes.oakapi.dto.request.PagoCreditoRequestDto.builder()
                .clienteId(clienteId)
                .monto(new BigDecimal("1000.00"))
                .metodoPago(com.abarrotes.oakapi.model.enums.MetodoPagoCredito.EFECTIVO)
                .build());

        pagoCreditoService.aplicarFIFO(pago.getId());

        Venta actualizada = ventaRepository.findById(futura.getUuid()).orElseThrow();
        assertEquals(0, BigDecimal.ZERO.compareTo(actualizada.getSaldoPendiente()),
                "El anticipo debe liquidar la factura aunque aun no venza");

        var aplicaciones = pagoCreditoAplicacionRepository.findByPagoId(pago.getId());
        assertEquals(1, aplicaciones.size());
    }

    /** FIFO liquida primero la factura que vence antes, aunque ninguna este vencida aun. */
    @Test
    void anticipadoAplicaPrimeroALaFacturaQueVenceAntes() {
        // Vence en 10 dias: es la mas urgente, aunque se emitio hace 2 dias
        Venta masUrgente = ventaRepository.save(crearVentaCredito(new BigDecimal("900.00"),
                LocalDate.now().plusDays(10), LocalDateTime.now().minusDays(2)));
        ventaRepository.flush();
        // Vence en 45 dias: se emitio antes, pero no es lo mas urgente
        Venta menosUrgente = ventaRepository.save(crearVentaCredito(new BigDecimal("300.00"),
                LocalDate.now().plusDays(45), LocalDateTime.now().minusDays(20)));
        ventaRepository.flush();

        var pago = pagoCreditoService.registrarPago(com.abarrotes.oakapi.dto.request.PagoCreditoRequestDto.builder()
                .clienteId(clienteId)
                .monto(new BigDecimal("300.00"))
                .metodoPago(com.abarrotes.oakapi.model.enums.MetodoPagoCredito.EFECTIVO)
                .build());

        pagoCreditoService.aplicarFIFO(pago.getId());

        assertEquals(0, new BigDecimal("600.00").compareTo(
                        ventaRepository.findById(masUrgente.getUuid()).orElseThrow().getSaldoPendiente()),
                "La factura que vence antes se amortiza primero (900 - 300)");
        assertEquals(0, new BigDecimal("300.00").compareTo(
                        ventaRepository.findById(menosUrgente.getUuid()).orElseThrow().getSaldoPendiente()),
                "La segunda factura no se toca hasta agotar el monto");
    }

    /** Una venta sin fecha de vencimiento no debe romper el ordenamiento. */
    @Test
    void ventaSinFechaVencimientoNoRompeElFifo() {
        Venta sinFecha = ventaRepository.save(crearVentaCredito(new BigDecimal("500.00"), null));
        ventaRepository.flush();

        var pago = pagoCreditoService.registrarPago(com.abarrotes.oakapi.dto.request.PagoCreditoRequestDto.builder()
                .clienteId(clienteId)
                .monto(new BigDecimal("500.00"))
                .metodoPago(com.abarrotes.oakapi.model.enums.MetodoPagoCredito.EFECTIVO)
                .build());

        pagoCreditoService.aplicarFIFO(pago.getId());

        assertEquals(0, BigDecimal.ZERO.compareTo(
                ventaRepository.findById(sinFecha.getUuid()).orElseThrow().getSaldoPendiente()));
    }
}
