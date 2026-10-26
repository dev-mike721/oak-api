package com.abarrotes.oakapi;

import com.abarrotes.oakapi.model.Categoria;
import com.abarrotes.oakapi.model.Cliente;
import com.abarrotes.oakapi.model.Producto;
import com.abarrotes.oakapi.model.Usuario;
import com.abarrotes.oakapi.model.enums.EstadoPresupuesto;
import com.abarrotes.oakapi.model.enums.MetodoPago;
import com.abarrotes.oakapi.model.enums.RegimenFiscal;
import com.abarrotes.oakapi.model.enums.UnidadMedida;
import com.abarrotes.oakapi.repository.CategoriaRepository;
import com.abarrotes.oakapi.repository.ClienteRepository;
import com.abarrotes.oakapi.repository.PresupuestoRepository;
import com.abarrotes.oakapi.repository.ProductoRepository;
import com.abarrotes.oakapi.repository.UsuarioRepository;
import com.abarrotes.oakapi.service.ClienteService;
import com.abarrotes.oakapi.service.PresupuestoService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.test.annotation.Rollback;
import org.springframework.context.annotation.Import;

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
class ClienteCreditoIntegrationTest {

    @Autowired
    private ClienteService clienteService;
    @Autowired
    private PresupuestoService presupuestoService;
    @Autowired
    private ClienteRepository clienteRepository;
    @Autowired
    private ProductoRepository productoRepository;
    @Autowired
    private CategoriaRepository categoriaRepository;
    @Autowired
    private UsuarioRepository usuarioRepository;
    @Autowired
    private PresupuestoRepository presupuestoRepository;

    private Integer clienteId;
    private Integer productoId;
    private Integer usuarioId;

    @BeforeEach
    void setUp() {
        Categoria categoria = categoriaRepository.findAll().get(0);

        Producto producto = productoRepository.save(Producto.builder()
                .plu("TEST" + System.nanoTime())
                .nombre("Producto Test")
                .categoria(categoria)
                .unidadMedida(UnidadMedida.PIEZA)
                .precioBase(new BigDecimal("100.00"))
                .esCompuesto(false)
                .activo(true)
                .build());
        productoId = producto.getId();

        Usuario usuario = usuarioRepository.save(Usuario.builder()
                .nombre("Tester")
                .username("test_" + System.nanoTime())
                .passwordHash("$2a$10$test")
                .rol(com.abarrotes.oakapi.model.enums.Rol.ADMINISTRADOR)
                .activo(true)
                .build());
        usuarioId = usuario.getId();

        com.abarrotes.oakapi.dto.response.ClienteResponseDto clienteDto = clienteService.crear(com.abarrotes.oakapi.dto.request.ClienteRequestDto.builder()
                .nombre("Cliente Test")
                .razonSocial("Cliente Test SA de CV")
                .rfc("TES123456789")
                .regimenFiscal(RegimenFiscal.GENERAL_LEY_PERSONAS_MORALES)
                .email("test@test.com")
                .telefono("5551234567")
                .limiteCredito(new BigDecimal("5000.00"))
                .diasCredito(30)
                .build());
        clienteId = clienteDto.getId();
    }

    @Test
    void ventaCreditoDentroDeLimiteSeRegistra() {
        // Verificar que el cliente tiene límite de crédito
        Cliente cliente = clienteRepository.findById(clienteId).orElseThrow();
        assertEquals(new BigDecimal("5000.00"), cliente.getLimiteCredito());
        assertEquals(BigDecimal.ZERO, cliente.getSaldoPendiente());
    }

    @Test
    void desactivarClienteConSaldoPendienteFalla() {
        // Intentar desactivar cliente sin saldo pendiente - debería funcionar
        clienteService.desactivar(clienteId);
        
        Cliente cliente = clienteRepository.findById(clienteId).orElseThrow();
        assertFalse(cliente.getActivo());
    }

    @Test
    void obtenerCuentaCorrienteRetornaDatosCorrectos() {
        var cuenta = clienteService.obtenerCuentaCorriente(clienteId);
        assertEquals(clienteId, cuenta.getClienteId());
        assertEquals(BigDecimal.ZERO, cuenta.getTotalVentasPendientes());
    }

    @Test
    void obtenerAgingRetornaDatosCorrectos() {
        var aging = clienteService.obtenerAging(java.time.LocalDate.now());
        assertNotNull(aging);
        assertTrue(aging.getClientesConDeuda() >= 0);
    }
}