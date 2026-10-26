package com.abarrotes.oakapi.service.impl;

import com.abarrotes.oakapi.dto.response.PrecioResueltoResponseDto;
import com.abarrotes.oakapi.model.Cliente;
import com.abarrotes.oakapi.model.ListaPrecio;
import com.abarrotes.oakapi.model.ListaPrecioDetalle;
import com.abarrotes.oakapi.model.Producto;
import com.abarrotes.oakapi.repository.ClientePrecioEspecialRepository;
import com.abarrotes.oakapi.repository.ClienteRepository;
import com.abarrotes.oakapi.repository.ListaPrecioDetalleRepository;
import com.abarrotes.oakapi.repository.ListaPrecioRepository;
import com.abarrotes.oakapi.repository.ProductoRepository;
import com.abarrotes.oakapi.repository.ClientePrecioEspecialRepository;
import com.abarrotes.oakapi.service.PrecioResolverService;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class PrecioResolverServiceImpl implements PrecioResolverService {

    private final ClienteRepository clienteRepository;
    private final ClientePrecioEspecialRepository clientePrecioEspecialRepository;
    private final ListaPrecioRepository listaPrecioRepository;
    private final ListaPrecioDetalleRepository listaPrecioDetalleRepository;
    private final ProductoRepository productoRepository;

    // Cache simple con Caffeine: key = "clienteId:productoId:cantidad:fecha", TTL 5 min
    private final Cache<String, PrecioResueltoResponseDto> cache = Caffeine.newBuilder()
            .maximumSize(10_000)
            .expireAfterWrite(5, TimeUnit.MINUTES)
            .build();

    public Cache<String, PrecioResueltoResponseDto> getCache() {
        return cache;
    }

    /**
     * Invalida las entradas de un cliente y producto. La clave es
     * "clienteId:productoId:cantidad:fecha", asi que alcanza con el prefijo.
     */
    public void invalidarClienteProducto(Integer clienteId, Integer productoId) {
        if (clienteId == null || productoId == null) {
            return;
        }
        String prefix = clienteId + ":" + productoId + ":";
        cache.asMap().keySet().removeIf(k -> k.startsWith(prefix));
    }

    /**
     * Invalida todas las entradas de un producto, para cualquier cliente.
     * Necesario cuando cambia el precio base, que aplica a todos los que no
     * tienen lista de precios ni precio especial. La clave empieza con el
     * cliente, asi que hay que comparar el segundo segmento.
     */
    public void invalidarProducto(Integer productoId) {
        if (productoId == null) {
            return;
        }
        String objetivo = String.valueOf(productoId);
        cache.asMap().keySet().removeIf(k -> {
            String[] partes = k.split(":");
            return partes.length >= 2 && partes[1].equals(objetivo);
        });
    }

    @Override
    public PrecioResueltoResponseDto resolvePrecio(Integer clienteId, Integer productoId, java.math.BigDecimal cantidad, LocalDate fecha) {
        String cacheKey = buildCacheKey(clienteId, productoId, cantidad, fecha);
        PrecioResueltoResponseDto cached = cache.getIfPresent(cacheKey);
        if (cached != null) {
            return cached;
        }

        PrecioResueltoResponseDto result = resolvePrecioInternal(clienteId, productoId, cantidad, fecha);
        cache.put(cacheKey, result);
        return result;
    }

    private PrecioResueltoResponseDto resolvePrecioInternal(Integer clienteId, Integer productoId, java.math.BigDecimal cantidad, LocalDate fecha) {
        LocalDateTime fechaDateTime = fecha.atStartOfDay();

        // 1. Precio especial cliente-producto (prioridad 1)
        Optional<com.abarrotes.oakapi.model.ClientePrecioEspecial> precioEspecial = clientePrecioEspecialRepository
                .findVigenteByClienteAndProducto(clienteId, productoId, fecha.atStartOfDay());
        if (precioEspecial.isPresent()) {
            return PrecioResueltoResponseDto.builder()
                    .precioUnitario(precioEspecial.get().getPrecio())
                    .origen("ESPECIAL")
                    .listaPrecioId(null)
                    .escalonAplicado(0)
                    .build();
        }

        // 2. Lista de precios del cliente (prioridad 2)
        Cliente cliente = null;
        if (clienteId != null) {
            cliente = clienteRepository.findById(clienteId).orElse(null);
        }

        if (cliente != null && cliente.getListaPrecio() != null) {
            ListaPrecioDetalle detalle = listaPrecioDetalleRepository
                    .findByListaPrecioIdAndProductoId(cliente.getListaPrecio().getId(), productoId)
                    .orElse(null);
            if (detalle != null) {
                BigDecimal precio = calcularPrecioConEscalones(detalle, cantidad);
                return PrecioResueltoResponseDto.builder()
                        .precioUnitario(precio)
                        .origen("LISTA_CLIENTE")
                        .listaPrecioId(cliente.getListaPrecio().getId())
                        .escalonAplicado(determinarEscalonAplicado(detalle, cantidad))
                        .build();
            }
        }

        // 3. Lista de precios canal default (prioridad 3) - usar la lista marcada como default del canal
        Optional<ListaPrecio> listaDefault = listaPrecioRepository.findVigentes(fecha).stream()
                .filter(ListaPrecio::getActiva)
                .filter(ListaPrecio::getEsDefaultCanal)
                .findFirst();
        if (listaDefault.isPresent()) {
            ListaPrecioDetalle detalle = listaPrecioDetalleRepository
                    .findByListaPrecioIdAndProductoId(listaDefault.get().getId(), productoId)
                    .orElse(null);
            if (detalle != null) {
                BigDecimal precio = calcularPrecioConEscalones(detalle, cantidad);
                return PrecioResueltoResponseDto.builder()
                        .precioUnitario(precio)
                        .origen("LISTA_CANAL")
                        .listaPrecioId(listaDefault.get().getId())
                        .escalonAplicado(determinarEscalonAplicado(detalle, cantidad))
                        .build();
            }
        }

        // 4. Precio base del producto (prioridad 4)
        Producto producto = productoRepository.findByIdAndActivoTrue(productoId)
                .orElse(null);
        BigDecimal precioBase = producto != null ? producto.getPrecioBase() : BigDecimal.ZERO;

        return PrecioResueltoResponseDto.builder()
                .precioUnitario(precioBase)
                .origen("BASE")
                .listaPrecioId(null)
                .escalonAplicado(0)
                .build();
    }

    private BigDecimal calcularPrecioConEscalones(ListaPrecioDetalle detalle, java.math.BigDecimal cantidad) {
        // Escalon 3
        if (detalle.getVolMin3() != null && cantidad.compareTo(java.math.BigDecimal.valueOf(detalle.getVolMin3())) >= 0) {
            return detalle.getPrecio3();
        }
        // Escalon 2
        if (detalle.getVolMin2() != null && cantidad.compareTo(java.math.BigDecimal.valueOf(detalle.getVolMin2())) >= 0) {
            return detalle.getPrecio2();
        }
        // Escalon 1
        if (detalle.getVolMin1() != null && cantidad.compareTo(java.math.BigDecimal.valueOf(detalle.getVolMin1())) >= 0) {
            return detalle.getPrecio1();
        }
        // Precio base
        return detalle.getPrecioBase();
    }

    private int determinarEscalonAplicado(ListaPrecioDetalle detalle, java.math.BigDecimal cantidad) {
        if (detalle.getVolMin3() != null && cantidad.compareTo(java.math.BigDecimal.valueOf(detalle.getVolMin3())) >= 0) {
            return 3;
        }
        if (detalle.getVolMin2() != null && cantidad.compareTo(java.math.BigDecimal.valueOf(detalle.getVolMin2())) >= 0) {
            return 2;
        }
        if (detalle.getVolMin1() != null && cantidad.compareTo(java.math.BigDecimal.valueOf(detalle.getVolMin1())) >= 0) {
            return 1;
        }
        return 0;
    }

    private String buildCacheKey(Integer clienteId, Integer productoId, java.math.BigDecimal cantidad, LocalDate fecha) {
        return String.format("%s:%s:%s:%s",
                clienteId != null ? clienteId : "none",
                productoId,
                cantidad != null ? cantidad.toPlainString() : "none",
                fecha != null ? fecha.toString() : "none");
    }
}
