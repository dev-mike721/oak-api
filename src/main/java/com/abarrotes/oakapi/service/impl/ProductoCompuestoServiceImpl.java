package com.abarrotes.oakapi.service.impl;

import com.abarrotes.oakapi.dto.request.ComponenteRequestDto;
import com.abarrotes.oakapi.dto.request.ProductoCompuestoRequestDto;
import com.abarrotes.oakapi.dto.response.ComponenteResponseDto;
import com.abarrotes.oakapi.dto.response.ProductoCompuestoResponseDto;
import com.abarrotes.oakapi.exception.BusinessRuleException;
import com.abarrotes.oakapi.exception.DuplicateResourceException;
import com.abarrotes.oakapi.exception.ResourceNotFoundException;
import com.abarrotes.oakapi.model.Producto;
import com.abarrotes.oakapi.model.ProductoCompuesto;
import com.abarrotes.oakapi.model.ProductoCompuestoId;
import com.abarrotes.oakapi.repository.ProductoCompuestoRepository;
import com.abarrotes.oakapi.repository.ProductoRepository;
import com.abarrotes.oakapi.service.ProductoCompuestoService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class ProductoCompuestoServiceImpl implements ProductoCompuestoService {

    private final ProductoRepository productoRepository;
    private final ProductoCompuestoRepository productoCompuestoRepository;

    @Override
    @Transactional(readOnly = true)
    public ProductoCompuestoResponseDto obtenerReceta(Integer productoPadreId) {
        Producto padre = validarPadre(productoPadreId);
        return mapToResponse(padre, productoCompuestoRepository.findByProductoPadreId(productoPadreId));
    }

    @Override
    @Transactional
    public ProductoCompuestoResponseDto agregarComponente(Integer productoPadreId, ComponenteRequestDto request) {
        Producto padre = validarPadre(productoPadreId);
        Producto hijo = validarHijo(request.getProductoHijoId());
        validarRelacion(padre, hijo);
        validarCantidad(request.getCantidad());

        if (productoCompuestoRepository.existsByIdProductoPadreIdAndIdProductoHijoId(padre.getId(), hijo.getId())) {
            throw new DuplicateResourceException("El producto " + hijo.getNombre() + " ya es componente de esta receta");
        }

        productoCompuestoRepository.save(ProductoCompuesto.builder()
                .id(new ProductoCompuestoId(padre.getId(), hijo.getId()))
                .productoPadre(padre)
                .productoHijo(hijo)
                .cantidad(request.getCantidad())
                .build());

        return mapToResponse(padre, productoCompuestoRepository.findByProductoPadreId(productoPadreId));
    }

    @Override
    @Transactional
    public ProductoCompuestoResponseDto reemplazarReceta(Integer productoPadreId, ProductoCompuestoRequestDto request) {
        if (!request.getProductoPadreId().equals(productoPadreId)) {
            throw new BusinessRuleException("El producto compuesto del cuerpo no coincide con el de la URL");
        }

        Producto padre = validarPadre(productoPadreId);

        Map<Integer, Producto> hijosNuevos = new LinkedHashMap<>();
        Map<Integer, BigDecimal> cantidades = new HashMap<>();
        for (ComponenteRequestDto componente : request.getComponentes()) {
            Producto hijo = validarHijo(componente.getProductoHijoId());
            validarRelacion(padre, hijo);
            validarCantidad(componente.getCantidad());
            if (hijosNuevos.putIfAbsent(hijo.getId(), hijo) != null) {
                throw new DuplicateResourceException("El producto " + hijo.getNombre() + " está duplicado en la receta");
            }
            cantidades.put(hijo.getId(), componente.getCantidad());
        }

        List<ProductoCompuesto> recetaActual = productoCompuestoRepository.findByProductoPadreId(productoPadreId);
        Iterator<ProductoCompuesto> iterator = recetaActual.iterator();
        while (iterator.hasNext()) {
            ProductoCompuesto pc = iterator.next();
            Integer hijoId = pc.getProductoHijo().getId();
            if (hijosNuevos.containsKey(hijoId)) {
                pc.setCantidad(cantidades.get(hijoId));
                hijosNuevos.remove(hijoId);
            } else {
                productoCompuestoRepository.delete(pc);
                iterator.remove();
            }
        }

        hijosNuevos.forEach((hijoId, hijo) -> productoCompuestoRepository.save(ProductoCompuesto.builder()
                .id(new ProductoCompuestoId(padre.getId(), hijoId))
                .productoPadre(padre)
                .productoHijo(hijo)
                .cantidad(cantidades.get(hijoId))
                .build()));

        return mapToResponse(padre, productoCompuestoRepository.findByProductoPadreId(productoPadreId));
    }

    @Override
    @Transactional
    public void eliminarComponente(Integer productoPadreId, Integer productoHijoId) {
        validarPadre(productoPadreId);
        ProductoCompuestoId id = new ProductoCompuestoId(productoPadreId, productoHijoId);
        ProductoCompuesto pc = productoCompuestoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "El producto " + productoHijoId + " no es componente de la receta del producto " + productoPadreId));
        productoCompuestoRepository.delete(pc);
    }

    @Override
    @Transactional
    public void vaciarReceta(Integer productoPadreId) {
        validarPadre(productoPadreId);
        productoCompuestoRepository.deleteByIdProductoPadreId(productoPadreId);
    }

    private Producto validarPadre(Integer productoPadreId) {
        Producto padre = productoRepository.findByIdAndActivoTrue(productoPadreId)
                .orElseThrow(() -> new ResourceNotFoundException("Producto no encontrado con ID: " + productoPadreId));
        if (!Boolean.TRUE.equals(padre.getEsCompuesto())) {
            throw new BusinessRuleException("El producto " + padre.getNombre() + " no está marcado como compuesto");
        }
        return padre;
    }

    private Producto validarHijo(Integer productoHijoId) {
        Producto hijo = productoRepository.findByIdAndActivoTrue(productoHijoId)
                .orElseThrow(() -> new ResourceNotFoundException("Producto componente no encontrado con ID: " + productoHijoId));
        if (Boolean.TRUE.equals(hijo.getEsCompuesto())) {
            throw new BusinessRuleException("Un producto compuesto no puede ser componente de otro: " + hijo.getNombre());
        }
        return hijo;
    }

    private void validarRelacion(Producto padre, Producto hijo) {
        if (padre.getId().equals(hijo.getId())) {
            throw new BusinessRuleException("Un producto no puede ser componente de sí mismo");
        }
    }

    private void validarCantidad(BigDecimal cantidad) {
        if (cantidad == null || cantidad.compareTo(BigDecimal.ZERO) <= 0) {
            throw new BusinessRuleException("La cantidad debe ser mayor a cero");
        }
    }

    private ProductoCompuestoResponseDto mapToResponse(Producto padre, List<ProductoCompuesto> receta) {
        List<ComponenteResponseDto> componentes = receta.stream()
                .map(pc -> {
                    Producto hijo = pc.getProductoHijo();
                    BigDecimal subtotal = hijo.getPrecioBase().multiply(pc.getCantidad())
                            .setScale(2, RoundingMode.HALF_UP);
                    return ComponenteResponseDto.builder()
                            .productoHijoId(hijo.getId())
                            .plu(hijo.getPlu())
                            .nombre(hijo.getNombre())
                            .unidadMedida(hijo.getUnidadMedida())
                            .cantidad(pc.getCantidad())
                            .precioUnitario(hijo.getPrecioBase())
                            .subtotal(subtotal)
                            .build();
                })
                .toList();

        BigDecimal precioSugerido = componentes.stream()
                .map(ComponenteResponseDto::getSubtotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .setScale(2, RoundingMode.HALF_UP);

        return ProductoCompuestoResponseDto.builder()
                .productoPadreId(padre.getId())
                .plu(padre.getPlu())
                .nombre(padre.getNombre())
                .componentes(componentes)
                .precioSugerido(precioSugerido)
                .build();
    }
}
