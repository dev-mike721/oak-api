package com.abarrotes.oakapi;

import com.abarrotes.oakapi.model.Caja;
import com.abarrotes.oakapi.model.Categoria;
import com.abarrotes.oakapi.model.Cliente;
import com.abarrotes.oakapi.model.Producto;
import com.abarrotes.oakapi.model.TurnoCaja;
import com.abarrotes.oakapi.model.Usuario;
import com.abarrotes.oakapi.model.enums.EstadoPresupuesto;
import com.abarrotes.oakapi.model.enums.MetodoPago;
import com.abarrotes.oakapi.model.enums.RegimenFiscal;
import com.abarrotes.oakapi.model.enums.UnidadMedida;
import com.abarrotes.oakapi.repository.CategoriaRepository;
import com.abarrotes.oakapi.repository.ClienteRepository;
import com.abarrotes.oakapi.repository.CajaRepository;
import com.abarrotes.oakapi.repository.LoteRepository;
import com.abarrotes.oakapi.repository.PresupuestoRepository;
import com.abarrotes.oakapi.repository.ProductoRepository;
import com.abarrotes.oakapi.repository.TurnoCajaRepository;
import com.abarrotes.oakapi.repository.UsuarioRepository;
import org.springframework.context.annotation.Import;
import com.abarrotes.oakapi.service.PresupuestoService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
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
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
@Import(TestDatabaseConfig.class)
class PresupuestoLifecycleIntegrationTest {

@Autowired
    private PresupuestoService presupuestoService;
    @Autowired
    private PresupuestoRepository presupuestoRepository;
    @Autowired
    private ClienteRepository clienteRepository;
    @Autowired
    private ProductoRepository productoRepository;
    @Autowired
    private CategoriaRepository categoriaRepository;
    @Autowired
    private UsuarioRepository usuarioRepository;
    @Autowired
    private CajaRepository cajaRepository;
    @Autowired
    private TurnoCajaRepository turnoRepository;
    @Autowired
    private LoteRepository loteRepository;
    @Autowired
    private com.abarrotes.oakapi.repository.LogAuditoriaRepository logAuditoriaRepository;

    private Integer clienteId;
    private Integer productoId;
    private Integer usuarioId;
    private Integer cajaId;
    private TurnoCaja turno;

    @BeforeEach
    void setUp() {
        Categoria categoria = categoriaRepository.findAll().get(0);

        Producto producto = productoRepository.save(Producto.builder()
                .plu("PRES" + System.nanoTime())
                .nombre("Producto Presupuesto Test")
                .categoria(categoria)
                .unidadMedida(UnidadMedida.PIEZA)
                .precioBase(new BigDecimal("50.00"))
                .esCompuesto(false)
                .activo(true)
                .build());
        productoId = producto.getId();

        // Crear lote con stock para el producto
        com.abarrotes.oakapi.model.Lote lote = com.abarrotes.oakapi.model.Lote.builder()
                .producto(producto)
                .codigoLote("LOTE-TEST-" + System.nanoTime())
                .costoCompra(new BigDecimal("30.00"))
                .cantidadInicial(new BigDecimal("100"))
                .cantidadActual(new BigDecimal("100"))
                .fechaRecepcion(LocalDate.now())
                .fechaCaducidad(LocalDate.now().plusDays(365))
                .build();
        loteRepository.save(lote);

        Usuario usuario = usuarioRepository.save(Usuario.builder()
                .nombre("Tester Presupuesto")
                .username("user_" + System.nanoTime())
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
        cajaId = caja.getId();

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

        // Set up security context for the test user
        var auth = new UsernamePasswordAuthenticationToken(
                usuario.getUsername(),
                null,
                List.of(new SimpleGrantedAuthority("ROLE_ADMINISTRADOR"))
        );
        SecurityContextHolder.getContext().setAuthentication(auth);

        Cliente cliente = clienteRepository.save(Cliente.builder()
                .nombre("Cliente Presupuesto Test")
                .rfc("PRST" + String.valueOf(System.currentTimeMillis()).substring(0, 9))
                .regimenFiscal(com.abarrotes.oakapi.model.enums.RegimenFiscal.GENERAL_LEY_PERSONAS_MORALES)
                .limiteCredito(new BigDecimal("10000"))
                .diasCredito(30)
                .activo(true)
                .build());
        clienteRepository.flush();
        clienteId = cliente.getId();
    }

    @Test
    void crearPresupuestoEnBorrador() {
        var request = com.abarrotes.oakapi.dto.request.PresupuestoRequestDto.builder()
                .clienteId(clienteId)
                .lineas(List.of(
                        com.abarrotes.oakapi.dto.request.PresupuestoDetalleRequestDto.builder()
                                .productoId(productoId)
                                .cantidad(new BigDecimal("2"))
                                .build()
                ))
                .vigenciaDias(30)
                .build();

        var presupuesto = presupuestoService.crear(request);

        assertEquals(EstadoPresupuesto.BORRADOR, presupuesto.getEstado());
        assertEquals(new BigDecimal("100.00"), presupuesto.getSubtotal()); // 2 * 50
        assertEquals(30, presupuesto.getVigenciaDias());
    }

    @Test
    void enviarPresupuestoCambiaEstadoAEnviado() {
        var presupuesto = crearPresupuestoBasico();

        var enviado = presupuestoService.enviar(presupuesto.getId());

        assertEquals(EstadoPresupuesto.ENVIADO, enviado.getEstado());
    }

    @Test
    void aprobarPresupuestoCambiaEstadoAAprobado() {
        var presupuesto = crearPresupuestoBasico();
        presupuestoService.enviar(presupuesto.getId());

        var aprobado = presupuestoService.aprobar(presupuesto.getId());

        assertEquals(EstadoPresupuesto.APROBADO, aprobado.getEstado());
        assertNotNull(aprobado.getAprobadoPor());
        assertNotNull(aprobado.getAprobadoEn());
    }

    @Test
    void convertirPresupuestoAprobadoACreaVenta() {
        var presupuesto = crearPresupuestoBasico();
        presupuestoService.enviar(presupuesto.getId());
        presupuestoService.aprobar(presupuesto.getId());

        var request = com.abarrotes.oakapi.dto.request.ConvertirPresupuestoRequestDto.builder()
                .metodoPago(MetodoPago.EFECTIVO)
                .build();

        var convertido = presupuestoService.convertirAVenta(presupuesto.getId(), request);

        assertEquals(EstadoPresupuesto.CONVERTIDO, convertido.getEstado());
        assertNotNull(convertido.getVentaUuid());
    }

    @Test
    void cancelarPresupuestoCambiaEstadoACancelado() {
        var presupuesto = crearPresupuestoBasico();

        var cancelado = presupuestoService.cancelar(presupuesto.getId());

        assertEquals(EstadoPresupuesto.CANCELADO, cancelado.getEstado());
    }

    @Test
    void noSePuedeConvertirPresupuestoNoAprobado() {
        var presupuesto = crearPresupuestoBasico();

        var request = com.abarrotes.oakapi.dto.request.ConvertirPresupuestoRequestDto.builder()
                .metodoPago(MetodoPago.EFECTIVO)
                .build();

        assertThrows(com.abarrotes.oakapi.exception.BusinessRuleException.class,
                () -> presupuestoService.convertirAVenta(presupuesto.getId(), request));
    }

    private com.abarrotes.oakapi.dto.response.PresupuestoResponseDto crearPresupuestoBasico() {
        var request = com.abarrotes.oakapi.dto.request.PresupuestoRequestDto.builder()
                .clienteId(clienteId)
                .lineas(List.of(
                        com.abarrotes.oakapi.dto.request.PresupuestoDetalleRequestDto.builder()
                                .productoId(productoId)
                                .cantidad(new BigDecimal("2"))
                                .build()
                ))
                .vigenciaDias(30)
                .build();

        return presupuestoService.crear(request);
    }

    /**
     * Decision de negocio: se puede aprobar un presupuesto que sigue en BORRADOR,
     * sin necesidad de enviarlo antes. Es deliberado: el gerente o administrador
     * que genera el presupuesto puede aprobarlo en el acto.
     *
     * Este test existe para que nadie restrinja este comportamiento por error.
     */
    @Test
    void apruebaPresupuestoDirectoEnBorrador() {
        var presupuesto = crearPresupuestoBasico();

        assertEquals(EstadoPresupuesto.BORRADOR, presupuesto.getEstado(),
                "El presupuesto arranca en BORRADOR");

        var aprobado = presupuestoService.aprobar(presupuesto.getId());

        assertEquals(EstadoPresupuesto.APROBADO, aprobado.getEstado());
        assertNotNull(aprobado.getAprobadoPor());
        assertNotNull(aprobado.getAprobadoEn());
    }

    /** Aprobacion deja traza de quien autorizo y desde que estado. */
    @Test
    void laAprobacionQuedaAuditada() {
        var presupuesto = crearPresupuestoBasico();

        presupuestoService.aprobar(presupuesto.getId());

        var logs = logAuditoriaRepository.findByAccionOrderByFechaHoraDesc("PRESUPUESTO_APROBADO");
        assertFalse(logs.isEmpty(), "La aprobacion debe quedar auditada");
        assertTrue(logs.get(0).getDetalles().contains(presupuesto.getId()));
        assertTrue(logs.get(0).getDetalles().contains("BORRADOR"),
                "Debe registrar el estado previo del presupuesto");
    }

    /** Un presupuesto ya aprobado no se puede volver a aprobar. */
    @Test
    void noSePuedeAprobarUnPresupuestoYaAprobado() {
        var presupuesto = crearPresupuestoBasico();
        presupuestoService.aprobar(presupuesto.getId());

        assertThrows(com.abarrotes.oakapi.exception.BusinessRuleException.class,
                () -> presupuestoService.aprobar(presupuesto.getId()));
    }

    /** Un presupuesto convertido ya no se puede aprobar. */
    @Test
    void noSePuedeAprobarUnPresupuestoConvertido() {
        var presupuesto = crearPresupuestoBasico();
        presupuestoService.enviar(presupuesto.getId());
        presupuestoService.aprobar(presupuesto.getId());
        presupuestoService.convertirAVenta(presupuesto.getId(),
                com.abarrotes.oakapi.dto.request.ConvertirPresupuestoRequestDto.builder()
                        .metodoPago(MetodoPago.EFECTIVO)
                        .build());

        assertThrows(com.abarrotes.oakapi.exception.BusinessRuleException.class,
                () -> presupuestoService.aprobar(presupuesto.getId()));
    }
}
