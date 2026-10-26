package com.abarrotes.oakapi.service;

import com.abarrotes.oakapi.dto.request.CajaRequestDto;
import com.abarrotes.oakapi.dto.response.CajaResponseDto;

import java.util.List;

public interface CajaService {

    List<CajaResponseDto> obtenerTodas();

    List<CajaResponseDto> obtenerActivas();

    CajaResponseDto obtenerPorId(Integer id);

    CajaResponseDto crear(CajaRequestDto request);

    CajaResponseDto actualizar(Integer id, CajaRequestDto request);

    void desactivar(Integer id);

    void activar(Integer id);
}