package com.abarrotes.oakapi.service.impl;

import com.abarrotes.oakapi.dto.request.SerieEstadoRequestDto;
import com.abarrotes.oakapi.dto.request.SerieRequestDto;
import com.abarrotes.oakapi.dto.response.SerieResponseDto;
import com.abarrotes.oakapi.dto.response.SeriesDisponiblesResponseDto;
import com.abarrotes.oakapi.exception.BusinessRuleException;
import com.abarrotes.oakapi.exception.DuplicateResourceException;
import com.abarrotes.oakapi.exception.ResourceNotFoundException;
import com.abarrotes.oakapi.model.Lote;
import com.abarrotes.oakapi.model.Producto;
import com.abarrotes.oakapi.model.Serie;
import com.abarrotes.oakapi.model.Venta;
import com.abarrotes.oakapi.model.enums.EstadoSerie;
import com.abarrotes.oakapi.repository.LoteRepository;
import com.abarrotes.oakapi.repository.ProductoRepository;
import com.abarrotes.oakapi.repository.SerieRepository;
import com.abarrotes.oakapi.repository.VentaRepository;
import com.abarrotes.oakapi.service.SerieService;
import lombok.RequiredArgsConstructor;

import java.util.ArrayList;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SerieServiceImpl implements SerieService {

    private final SerieRepository serieRepository;
    private final ProductoRepository productoRepository;
    private final LoteRepository loteRepository;
    private final VentaRepository ventaRepository;

    @Override
    @Transactional
    public List<SerieResponseDto> registrarSeriesEnCompra(Integer productoId, Integer loteId, List<String> codigos) {
        Producto producto = productoRepository.findByIdAndActivoTrue(productoId)
                .orElseThrow(() -> new ResourceNotFoundException("Producto no encontrado con ID: " + productoId));

        Lote lote = null;
        if (loteId != null) {
            lote = loteRepository.findById(loteId)
                    .orElseThrow(() -> new ResourceNotFoundException("Lote no encontrado con ID: " + loteId));
        }

        // Validar códigos duplicados en la lista
        if (codigos.size() != codigos.stream().distinct().count()) {
            throw new BusinessRuleException("Hay códigos de serie duplicados en la lista proporcionada");
        }

        // Validar que no existan en BD
        for (String codigo : codigos) {
            String codigoFinal = codigo;
            if (serieRepository.existsByCodigoSerie(codigoFinal)) {
                throw new DuplicateResourceException("La serie " + codigoFinal + " ya existe en el sistema");
            }
        }

        List<Serie> series = new ArrayList<>();
        for (String codigo : codigos) {
            String codigoFinal = codigo;
            if (serieRepository.existsByCodigoSerie(codigoFinal)) {
                throw new DuplicateResourceException("La serie " + codigoFinal + " ya existe en el sistema");
            }
            series.add(Serie.builder()
                    .producto(producto)
                    .lote(lote)
                    .codigoSerie(codigoFinal)
                    .estado(EstadoSerie.DISPONIBLE)
                    .build());
        }

        List<Serie> guardadas = serieRepository.saveAll(series);
        return guardadas.stream().map(this::mapToResponse).collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<SerieResponseDto> obtenerDisponiblesParaVenta(Integer productoId, Integer cantidad) {
        List<Serie> series = serieRepository.findDisponiblesFIFOByProducto(productoId, EstadoSerie.DISPONIBLE);
        if (cantidad != null && series.size() > cantidad) {
            series = series.subList(0, cantidad);
        }
        return series.stream().map(this::mapToResponse).collect(Collectors.toList());
    }

    @Override
    @Transactional
    public List<SerieResponseDto> venderSeries(Integer productoId, List<String> codigos, String ventaId) {
        Venta venta = null;
        if (ventaId != null) {
            venta = ventaRepository.findById(ventaId)
                    .orElseThrow(() -> new ResourceNotFoundException("Venta no encontrada con ID: " + ventaId));
        }

        List<Serie> series;
        if (codigos != null && !codigos.isEmpty()) {
            // Venta con selección manual de series
            series = codigos.stream()
                    .map(codigo -> serieRepository.findByCodigoSerie(codigo)
                            .orElseThrow(() -> new ResourceNotFoundException("Serie no encontrada: " + codigo)))
                    .collect(Collectors.toList());

for (Serie serie : series) {
                if (productoId != null && !serie.getProducto().getId().equals(productoId)) {
                    throw new BusinessRuleException(
                            "La serie " + serie.getCodigoSerie() + " pertenece al producto "
                                    + serie.getProducto().getNombre() + " y no a " + productoId);
                }
                if (serie.getEstado() != EstadoSerie.DISPONIBLE) {
                    throw new BusinessRuleException("La serie " + serie.getCodigoSerie() + " no está disponible (estado: " + serie.getEstado() + ")");
                }
            }
        } else {
            throw new BusinessRuleException("Debe especificar las series a vender");
        }

        LocalDateTime ahora = LocalDateTime.now();
        for (Serie serie : series) {
            serie.setEstado(EstadoSerie.VENDIDO);
            serie.setVenta(venta);
            serie.setFechaVenta(LocalDateTime.now());
        }

        List<Serie> actualizadas = serieRepository.saveAll(series);
        return actualizadas.stream().map(this::mapToResponse).collect(Collectors.toList());
    }

    @Override
    @Transactional
    public List<SerieResponseDto> liberarSeriesPorCancelacion(String ventaId) {
        List<Serie> series = serieRepository.findByVentaId(ventaId);
        if (series.isEmpty()) {
            return List.of();
        }

        for (Serie serie : series) {
            serie.setEstado(EstadoSerie.DISPONIBLE);
            serie.setVenta(null);
            serie.setFechaVenta(null);
        }

        List<Serie> actualizadas = serieRepository.saveAll(series);
        return actualizadas.stream().map(this::mapToResponse).collect(Collectors.toList());
    }

    @Override
    @Transactional
    public SerieResponseDto cambiarEstado(Integer serieId, SerieEstadoRequestDto request) {
        Serie serie = serieRepository.findById(serieId)
                .orElseThrow(() -> new ResourceNotFoundException("Serie no encontrada con ID: " + serieId));

        EstadoSerie estadoAnterior = serie.getEstado();
        EstadoSerie nuevoEstado = request.getEstado();

        // Validar transiciones permitidas
        validarTransicionEstado(estadoAnterior, nuevoEstado);

        serie.setEstado(nuevoEstado);
        if (nuevoEstado == EstadoSerie.GARANTIA) {
            serie.setFechaGarantia(LocalDateTime.now());
        } else if (nuevoEstado == EstadoSerie.DISPONIBLE) {
            serie.setFechaGarantia(null);
        }

        return mapToResponse(serieRepository.save(serie));
    }

    @Override
    @Transactional(readOnly = true)
    public List<com.abarrotes.oakapi.dto.response.SeriesDisponiblesResponseDto> obtenerDisponibles(Integer productoId, Integer limite) {
        List<Serie> series = serieRepository.findDisponiblesFIFOByProducto(productoId, EstadoSerie.DISPONIBLE);
        if (limite != null && series.size() > limite) {
            series = series.subList(0, limite);
        }
        return series.stream().map(s -> com.abarrotes.oakapi.dto.response.SeriesDisponiblesResponseDto.builder()
                .id(s.getId())
                .codigoSerie(s.getCodigoSerie())
                .loteId(s.getLote() != null ? s.getLote().getId() : null)
                .loteCodigo(s.getLote() != null ? s.getLote().getCodigoLote() : null)
                .fechaRecepcion(s.getLote() != null ? s.getLote().getFechaRecepcion() : null)
                .build())
                .collect(Collectors.toList());
    }

    private void validarTransicionEstado(EstadoSerie desde, EstadoSerie hacia) {
        if (desde == EstadoSerie.VENDIDO && hacia != EstadoSerie.DISPONIBLE && hacia != EstadoSerie.GARANTIA) {
            throw new BusinessRuleException("Una serie VENDIDA solo puede pasar a DISPONIBLE o GARANTIA");
        }
        if (desde == EstadoSerie.DISPONIBLE && hacia == EstadoSerie.GARANTIA) {
            throw new BusinessRuleException("Una serie DISPONIBLE no puede pasar directamente a GARANTIA");
        }
    }

    private SerieResponseDto mapToResponse(Serie s) {
        return SerieResponseDto.builder()
                .id(s.getId())
                .productoId(s.getProducto().getId())
                .productoNombre(s.getProducto().getNombre())
                .productoPlu(s.getProducto().getPlu())
                .loteId(s.getLote() != null ? s.getLote().getId() : null)
                .loteCodigo(s.getLote() != null ? s.getLote().getCodigoLote() : null)
                .codigoSerie(s.getCodigoSerie())
                .estado(s.getEstado())
                .ventaId(s.getVenta() != null ? s.getVenta().getUuid() : null)
                .fechaVenta(s.getFechaVenta())
                .fechaGarantia(s.getFechaGarantia())
                .createdAt(s.getCreatedAt())
                .updatedAt(s.getUpdatedAt())
                .build();
    }
}