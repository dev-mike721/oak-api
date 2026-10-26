package com.abarrotes.oakapi.service;

import com.abarrotes.oakapi.dto.request.ListaPrecioDetalleRequestDto;
import com.abarrotes.oakapi.dto.request.ListaPrecioRequestDto;
import com.abarrotes.oakapi.dto.response.ListaPrecioDetalleResponseDto;
import com.abarrotes.oakapi.dto.response.ListaPrecioResponseDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.util.List;

public interface ListaPrecioService {

    Page<ListaPrecioResponseDto> obtenerTodas(Pageable pageable);

    Page<ListaPrecioResponseDto> obtenerVigentes(LocalDate fecha, Pageable pageable);

    ListaPrecioResponseDto obtenerPorId(Integer id);

    ListaPrecioResponseDto crear(ListaPrecioRequestDto request);

    ListaPrecioResponseDto actualizar(Integer id, ListaPrecioRequestDto request);

    void eliminar(Integer id);

    // Detalles
    ListaPrecioDetalleResponseDto agregarDetalle(Integer listaPrecioId, ListaPrecioDetalleRequestDto request);

    List<ListaPrecioDetalleResponseDto> obtenerDetalles(Integer listaPrecioId);

    void eliminarDetalle(Integer listaPrecioId, Integer productoId);
}