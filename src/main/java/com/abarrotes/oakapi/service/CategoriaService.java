package com.abarrotes.oakapi.service;

import com.abarrotes.oakapi.dto.request.CategoriaRequestDto;
import com.abarrotes.oakapi.dto.response.CategoriaDto;

import java.util.List;

public interface CategoriaService {

    List<CategoriaDto> obtenerTodas();

    CategoriaDto obtenerPorId(Integer id);

    CategoriaDto crear(CategoriaRequestDto request);

    CategoriaDto actualizar(Integer id, CategoriaRequestDto request);

    void eliminar(Integer id);
}