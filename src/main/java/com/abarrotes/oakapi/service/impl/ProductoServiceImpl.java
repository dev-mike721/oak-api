package com.abarrotes.oakapi.service.impl;
import com.abarrotes.oakapi.dto.request.ProductoRequestDto;
import com.abarrotes.oakapi.dto.response.ProductoResponseDto;
import com.abarrotes.oakapi.exception.BusinessRuleException;
import com.abarrotes.oakapi.exception.DuplicateResourceException;
import com.abarrotes.oakapi.exception.ResourceNotFoundException;
import com.abarrotes.oakapi.model.Categoria;
import com.abarrotes.oakapi.model.Producto;
import com.abarrotes.oakapi.repository.CategoriaRepository;
import com.abarrotes.oakapi.repository.ProductoRepository;
import com.abarrotes.oakapi.service.ProductoService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ProductoServiceImpl implements ProductoService {

    private final ProductoRepository productoRepository;
    private final CategoriaRepository categoriaRepository;
    private final PrecioResolverServiceImpl precioResolverServiceImpl;

    @Override
    @Transactional(readOnly = true)
    public List<ProductoResponseDto> obtenerTodos() {
        return productoRepository.findByActivoTrue().stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public ProductoResponseDto buscarPorId(Integer id) {
        Producto producto = productoRepository.findByIdAndActivoTrue(id)
                .orElseThrow(() -> new ResourceNotFoundException("Producto no encontrado con ID: " + id));
        return mapToResponse(producto);
    }

    @Override
    @Transactional(readOnly = true)
    public ProductoResponseDto buscarPorPlu(String plu) {
        Producto producto = productoRepository.findByPluAndActivoTrue(plu)
                .orElseThrow(() -> new ResourceNotFoundException("Producto no encontrado con el PLU: " + plu));
        return mapToResponse(producto);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProductoResponseDto> buscarPorCategoria(Integer categoriaId) {
        return productoRepository.findByCategoriaIdAndActivoTrue(categoriaId).stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProductoResponseDto> buscarPorNombre(String nombre) {
        return productoRepository.findByNombreContainingIgnoreCaseAndActivoTrue(nombre).stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Override
    @Transactional
    public ProductoResponseDto crearProducto(ProductoRequestDto request) {
        if (productoRepository.existsByPlu(request.getPlu())) {
            throw new DuplicateResourceException("Ya existe un producto con el PLU: " + request.getPlu());
        }
        Categoria categoria = categoriaRepository.findById(request.getCategoriaId())
                .orElseThrow(() -> new ResourceNotFoundException("Categoría no encontrada con ID: " + request.getCategoriaId()));

        boolean esCompuesto = Boolean.TRUE.equals(request.getEsCompuesto());
        validarPrecioBase(request.getPrecioBase(), esCompuesto);

        Producto producto = Producto.builder()
                .plu(request.getPlu())
                .nombre(request.getNombre())
                .descripcion(request.getDescripcion())
                .categoria(categoria)
                .unidadMedida(request.getUnidadMedida())
                .permiteDecimales(request.getPermiteDecimales() != null ? request.getPermiteDecimales() : false)
                .precioBase(request.getPrecioBase())
                .esCompuesto(esCompuesto)
                .activo(true)
                .imagenUrl(request.getImagenUrl())
                .build();

        return mapToResponse(productoRepository.save(producto));
    }

    @Override
    @Transactional
    public ProductoResponseDto actualizarProducto(Integer id, ProductoRequestDto request) {
        Producto producto = productoRepository.findByIdAndActivoTrue(id)
                .orElseThrow(() -> new ResourceNotFoundException("Producto no encontrado con ID: " + id));

        if (productoRepository.existsByPluAndIdNot(request.getPlu(), id)) {
            throw new DuplicateResourceException("Ya existe otro producto con el PLU: " + request.getPlu());
        }

        Categoria categoria = categoriaRepository.findById(request.getCategoriaId())
                .orElseThrow(() -> new ResourceNotFoundException("Categoría no encontrada con ID: " + request.getCategoriaId()));

        boolean esCompuesto = Boolean.TRUE.equals(request.getEsCompuesto());
        validarPrecioBase(request.getPrecioBase(), esCompuesto);

        producto.setPlu(request.getPlu());
        producto.setNombre(request.getNombre());
        producto.setDescripcion(request.getDescripcion());
        producto.setCategoria(categoria);
        producto.setUnidadMedida(request.getUnidadMedida());
        producto.setPermiteDecimales(request.getPermiteDecimales() != null ? request.getPermiteDecimales() : false);
        // El precio base alimenta la resolucion con origen BASE. Si cambia, hay que
        // tirar las entradas cacheadas o se cobra el precio anterior hasta 5 minutos.
        boolean cambioPrecioBase = producto.getPrecioBase() == null
                || producto.getPrecioBase().compareTo(request.getPrecioBase()) != 0;

        producto.setPrecioBase(request.getPrecioBase());
        producto.setEsCompuesto(esCompuesto);
        if (request.getImagenUrl() != null && !request.getImagenUrl().isBlank()) {
            producto.setImagenUrl(request.getImagenUrl());
        }

        if (cambioPrecioBase) {
            precioResolverServiceImpl.invalidarProducto(producto.getId());
        }

        return mapToResponse(productoRepository.save(producto));
    }

    @Override
    @Transactional
    public void eliminarProducto(Integer id) {
        Producto producto = productoRepository.findByIdAndActivoTrue(id)
                .orElseThrow(() -> new ResourceNotFoundException("Producto no encontrado con ID: " + id));
        producto.setActivo(false);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProductoResponseDto> obtenerInactivos() {
        return productoRepository.findByActivoFalse().stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Override
    @Transactional
    public ProductoResponseDto reactivarProducto(Integer id) {
        Producto producto = productoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Producto no encontrado con ID: " + id));
        if (Boolean.TRUE.equals(producto.getActivo())) {
            throw new BusinessRuleException("El producto ya se encuentra activo");
        }
        producto.setActivo(true);
        return mapToResponse(producto);
    }

    private void validarPrecioBase(BigDecimal precioBase, boolean esCompuesto) {
        if (!esCompuesto && (precioBase == null || precioBase.compareTo(BigDecimal.ZERO) <= 0)) {
            throw new BusinessRuleException("Los productos simples deben tener un precio base mayor a cero");
        }
    }

    private ProductoResponseDto mapToResponse(Producto p) {
        return ProductoResponseDto.builder()
                .id(p.getId())
                .plu(p.getPlu())
                .nombre(p.getNombre())
                .descripcion(p.getDescripcion())
                .categoriaId(p.getCategoria() != null ? p.getCategoria().getId() : null)
                .categoriaNombre(p.getCategoria() != null ? p.getCategoria().getNombre() : "Sin Categoría")
                .unidadMedida(p.getUnidadMedida())
                .permiteDecimales(p.getPermiteDecimales())
                .precioBase(p.getPrecioBase())
                .esCompuesto(p.getEsCompuesto())
                .activo(p.getActivo())
                .imagenUrl(p.getImagenUrl())
                .build();
    }
}