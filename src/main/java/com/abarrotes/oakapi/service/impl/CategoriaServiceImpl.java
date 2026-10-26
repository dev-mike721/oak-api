package com.abarrotes.oakapi.service.impl;

import com.abarrotes.oakapi.dto.request.CategoriaRequestDto;
import com.abarrotes.oakapi.dto.response.CategoriaDto;
import com.abarrotes.oakapi.exception.BusinessRuleException;
import com.abarrotes.oakapi.exception.DuplicateResourceException;
import com.abarrotes.oakapi.exception.ResourceNotFoundException;
import com.abarrotes.oakapi.model.Categoria;
import com.abarrotes.oakapi.model.Producto;
import com.abarrotes.oakapi.repository.CategoriaRepository;
import com.abarrotes.oakapi.repository.ProductoRepository;
import com.abarrotes.oakapi.service.CategoriaService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CategoriaServiceImpl implements CategoriaService {

    private final CategoriaRepository categoriaRepository;
    private final ProductoRepository productoRepository;

    @Override
    @Transactional(readOnly = true)
    public List<CategoriaDto> obtenerTodas() {
        return categoriaRepository.findAll().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public CategoriaDto obtenerPorId(Integer id) {
        return mapToResponse(obtenerEntidad(id));
    }

    @Override
    @Transactional
    public CategoriaDto crear(CategoriaRequestDto request) {
        if (categoriaRepository.existsByNombre(request.getNombre())) {
            throw new DuplicateResourceException("Ya existe una categoría con el nombre: " + request.getNombre());
        }

        Categoria categoria = Categoria.builder()
                .nombre(request.getNombre())
                .descripcion(request.getDescripcion())
                .build();

        return mapToResponse(categoriaRepository.save(categoria));
    }

    @Override
    @Transactional
    public CategoriaDto actualizar(Integer id, CategoriaRequestDto request) {
        Categoria categoria = obtenerEntidad(id);

        if (!categoria.getNombre().equals(request.getNombre())
                && categoriaRepository.existsByNombre(request.getNombre())) {
            throw new DuplicateResourceException("Ya existe otra categoría con el nombre: " + request.getNombre());
        }

        categoria.setNombre(request.getNombre());
        categoria.setDescripcion(request.getDescripcion());

        return mapToResponse(categoriaRepository.save(categoria));
    }

    @Override
    @Transactional
    public void eliminar(Integer id) {
        Categoria categoria = obtenerEntidad(id);

        List<Producto> productos = productoRepository.findByCategoriaId(id);
        if (!productos.isEmpty()) {
            throw new BusinessRuleException(
                    "No se puede eliminar la categoría '" + categoria.getNombre()
                            + "' porque tiene " + productos.size() + " producto(s) asociado(s)");
        }

        categoriaRepository.delete(categoria);
    }

    private Categoria obtenerEntidad(Integer id) {
        return categoriaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Categoría no encontrada con ID: " + id));
    }

    private CategoriaDto mapToResponse(Categoria categoria) {
        return CategoriaDto.builder()
                .id(categoria.getId())
                .nombre(categoria.getNombre())
                .descripcion(categoria.getDescripcion())
                .build();
    }
}