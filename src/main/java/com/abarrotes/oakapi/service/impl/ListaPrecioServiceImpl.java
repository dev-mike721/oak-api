package com.abarrotes.oakapi.service.impl;

import com.abarrotes.oakapi.dto.request.ListaPrecioDetalleRequestDto;
import com.abarrotes.oakapi.dto.request.ListaPrecioRequestDto;
import com.abarrotes.oakapi.dto.response.ListaPrecioDetalleResponseDto;
import com.abarrotes.oakapi.dto.response.ListaPrecioResponseDto;
import com.abarrotes.oakapi.exception.BusinessRuleException;
import com.abarrotes.oakapi.exception.DuplicateResourceException;
import com.abarrotes.oakapi.exception.ResourceNotFoundException;
import com.abarrotes.oakapi.model.Cliente;
import com.abarrotes.oakapi.model.ListaPrecio;
import com.abarrotes.oakapi.model.ListaPrecioDetalle;
import com.abarrotes.oakapi.model.Producto;
import com.abarrotes.oakapi.repository.ClienteRepository;
import com.abarrotes.oakapi.repository.ListaPrecioDetalleRepository;
import com.abarrotes.oakapi.repository.ListaPrecioRepository;
import com.abarrotes.oakapi.repository.ProductoRepository;
import com.abarrotes.oakapi.service.ListaPrecioService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ListaPrecioServiceImpl implements ListaPrecioService {

    private final ListaPrecioRepository listaPrecioRepository;
    private final ListaPrecioDetalleRepository listaPrecioDetalleRepository;
    private final ProductoRepository productoRepository;
    private final ClienteRepository clienteRepository;
    private final PrecioResolverServiceImpl precioResolverServiceImpl;

    @Override
    @Transactional(readOnly = true)
    public Page<ListaPrecioResponseDto> obtenerTodas(Pageable pageable) {
        return listaPrecioRepository.findAll(pageable).map(this::mapToResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ListaPrecioResponseDto> obtenerVigentes(LocalDate fecha, Pageable pageable) {
        return listaPrecioRepository.findVigentes(fecha, pageable).map(this::mapToResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public ListaPrecioResponseDto obtenerPorId(Integer id) {
        ListaPrecio lista = listaPrecioRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Lista de precios no encontrada con ID: " + id));
        return mapToResponse(lista);
    }

    @Override
    @Transactional
    public ListaPrecioResponseDto crear(ListaPrecioRequestDto request) {
        if (listaPrecioRepository.existsByNombreAndActivaTrue(request.getNombre())) {
            throw new DuplicateResourceException("Ya existe una lista de precios activa con ese nombre");
        }

        ListaPrecio lista = ListaPrecio.builder()
                .nombre(request.getNombre())
                .descripcion(request.getDescripcion())
                .vigenciaInicio(request.getVigenciaInicio())
                .vigenciaFin(request.getVigenciaFin())
                .activa(request.getActiva() != null ? request.getActiva() : true)
                .esDefaultCanal(Boolean.TRUE.equals(request.getEsDefaultCanal()))
                .build();

        if (lista.getEsDefaultCanal()) {
            desmarcarDefaultCanal();
        }

        ListaPrecio saved = listaPrecioRepository.save(lista);
        invalidateCacheForListaPrecio(lista.getId());
        return mapToResponse(saved);
    }

    @Override
    @Transactional
    public ListaPrecioResponseDto actualizar(Integer id, ListaPrecioRequestDto request) {
        ListaPrecio lista = listaPrecioRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Lista de precios no encontrada con ID: " + id));

        if (!lista.getNombre().equals(request.getNombre()) && listaPrecioRepository.existsByNombreAndActivaTrue(request.getNombre())) {
            throw new DuplicateResourceException("Ya existe otra lista de precios activa con ese nombre");
        }

        lista.setNombre(request.getNombre());
        lista.setDescripcion(request.getDescripcion());
        lista.setVigenciaInicio(request.getVigenciaInicio());
        lista.setVigenciaFin(request.getVigenciaFin());
        lista.setActiva(request.getActiva() != null ? request.getActiva() : true);
        lista.setEsDefaultCanal(Boolean.TRUE.equals(request.getEsDefaultCanal()));

        if (lista.getEsDefaultCanal()) {
            desmarcarDefaultCanal();
        }

        invalidateCacheForListaPrecio(lista.getId());
        return mapToResponse(lista);
    }

    /** Garantiza que sólo una lista de precios sea la de default del canal. */
    private void desmarcarDefaultCanal() {
        List<ListaPrecio> defaults = listaPrecioRepository.findByEsDefaultCanalTrue();
        for (ListaPrecio otra : defaults) {
            if (Boolean.TRUE.equals(otra.getEsDefaultCanal())) {
                otra.setEsDefaultCanal(false);
                listaPrecioRepository.save(otra);
                invalidateCacheForListaPrecio(otra.getId());
            }
        }
    }

    @Override
    @Transactional
    public void eliminar(Integer id) {
        ListaPrecio lista = listaPrecioRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Lista de precios no encontrada con ID: " + id));
        int listaPrecioId = lista.getId();
        lista.setActiva(false);
        invalidateCacheForListaPrecio(listaPrecioId);
    }

    @Override
    @Transactional
    public ListaPrecioDetalleResponseDto agregarDetalle(Integer listaPrecioId, ListaPrecioDetalleRequestDto request) {
        ListaPrecio lista = listaPrecioRepository.findById(listaPrecioId)
                .orElseThrow(() -> new ResourceNotFoundException("Lista de precios no encontrada con ID: " + listaPrecioId));

        Producto producto = productoRepository.findByIdAndActivoTrue(request.getProductoId())
                .orElseThrow(() -> new ResourceNotFoundException("Producto no encontrado con ID: " + request.getProductoId()));

        if (listaPrecioDetalleRepository.existsByListaPrecioIdAndProductoId(listaPrecioId, request.getProductoId())) {
            throw new DuplicateResourceException("El producto ya existe en esta lista de precios");
        }

        validarEscalones(request);

        ListaPrecioDetalle detalle = ListaPrecioDetalle.builder()
                .listaPrecio(lista)
                .producto(producto)
                .precioBase(request.getPrecioBase())
                .volMin1(request.getVolMin1())
                .precio1(request.getPrecio1())
                .volMin2(request.getVolMin2())
                .precio2(request.getPrecio2())
                .volMin3(request.getVolMin3())
                .precio3(request.getPrecio3())
                .build();

        ListaPrecioDetalle saved = listaPrecioDetalleRepository.save(detalle);
        invalidateCacheForListaPrecioDetalle(listaPrecioId, request.getProductoId());
        return mapToDetalleResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ListaPrecioDetalleResponseDto> obtenerDetalles(Integer listaPrecioId) {
        listaPrecioRepository.findById(listaPrecioId)
                .orElseThrow(() -> new ResourceNotFoundException("Lista de precios no encontrada con ID: " + listaPrecioId));

        return listaPrecioDetalleRepository.findByListaPrecioId(listaPrecioId).stream()
                .map(this::mapToDetalleResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void eliminarDetalle(Integer listaPrecioId, Integer productoId) {
        listaPrecioDetalleRepository.deleteById(
                new com.abarrotes.oakapi.model.ListaPrecioDetalleId(listaPrecioId, productoId)
        );
        invalidateCacheForListaPrecioDetalle(listaPrecioId, productoId);
    }

    private void validarEscalones(ListaPrecioDetalleRequestDto request) {
        if (request.getVolMin1() != null && request.getPrecio1() == null) {
            throw new BusinessRuleException("Si define vol_min_1 debe definir precio_1");
        }
        if (request.getVolMin2() != null && request.getPrecio2() == null) {
            throw new BusinessRuleException("Si define vol_min_2 debe definir precio_2");
        }
        if (request.getVolMin3() != null && request.getPrecio3() == null) {
            throw new BusinessRuleException("Si define vol_min_3 debe definir precio_3");
        }
        if (request.getVolMin1() != null && request.getVolMin2() != null && request.getVolMin1() >= request.getVolMin2()) {
            throw new BusinessRuleException("vol_min_1 debe ser menor que vol_min_2");
        }
        if (request.getVolMin2() != null && request.getVolMin3() != null && request.getVolMin2() >= request.getVolMin3()) {
            throw new BusinessRuleException("vol_min_2 debe ser menor que vol_min_3");
        }
    }

    private void invalidateCacheForListaPrecio(Integer listaPrecioId) {
        List<Cliente> clientes = clienteRepository.findByListaPrecioId(listaPrecioId);
        for (Cliente cliente : clientes) {
            String prefix = cliente.getId() + ":";
            precioResolverServiceImpl.getCache().asMap().keySet().removeIf(k -> k.startsWith(prefix));
        }
        // También invalidar para clientes sin listaPrecio (clienteId = "none" en cache key)
        String prefixNone = "none:";
        precioResolverServiceImpl.getCache().asMap().keySet().removeIf(k -> k.startsWith(prefixNone));
    }

    private void invalidateCacheForListaPrecioDetalle(Integer listaPrecioId, Integer productoId) {
        List<Cliente> clientes = clienteRepository.findByListaPrecioId(listaPrecioId);
        for (Cliente cliente : clientes) {
            String prefix = cliente.getId() + ":" + productoId + ":";
            precioResolverServiceImpl.getCache().asMap().keySet().removeIf(k -> k.startsWith(prefix));
        }
        // También invalidar para clientes sin listaPrecio
        String prefixNone = "none:" + productoId + ":";
        precioResolverServiceImpl.getCache().asMap().keySet().removeIf(k -> k.startsWith(prefixNone));
    }

    private ListaPrecioResponseDto mapToResponse(ListaPrecio l) {
        return ListaPrecioResponseDto.builder()
                .id(l.getId())
                .nombre(l.getNombre())
                .descripcion(l.getDescripcion())
                .vigenciaInicio(l.getVigenciaInicio())
                .vigenciaFin(l.getVigenciaFin())
                .activa(l.getActiva())
                .esDefaultCanal(l.getEsDefaultCanal())
                .createdAt(l.getCreatedAt())
                .updatedAt(l.getUpdatedAt())
                .build();
    }

    private ListaPrecioDetalleResponseDto mapToDetalleResponse(ListaPrecioDetalle d) {
        return ListaPrecioDetalleResponseDto.builder()
                .listaPrecioId(d.getListaPrecio().getId())
                .productoId(d.getProducto().getId())
                .productoNombre(d.getProducto().getNombre())
                .productoPlu(d.getProducto().getPlu())
                .precioBase(d.getPrecioBase())
                .volMin1(d.getVolMin1())
                .precio1(d.getPrecio1())
                .volMin2(d.getVolMin2())
                .precio2(d.getPrecio2())
                .volMin3(d.getVolMin3())
                .precio3(d.getPrecio3())
                .createdAt(d.getCreatedAt())
                .updatedAt(d.getUpdatedAt())
                .build();
    }
}