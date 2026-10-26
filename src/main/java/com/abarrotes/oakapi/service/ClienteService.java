package com.abarrotes.oakapi.service;

import com.abarrotes.oakapi.dto.request.ClientePrecioEspecialRequestDto;
import com.abarrotes.oakapi.dto.request.ClienteRequestDto;
import com.abarrotes.oakapi.dto.response.AgingResponseDto;
import com.abarrotes.oakapi.dto.response.ClientePrecioEspecialResponseDto;
import com.abarrotes.oakapi.dto.response.ClienteResponseDto;
import com.abarrotes.oakapi.dto.response.CuentaCorrienteResponseDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.util.List;

public interface ClienteService {

    Page<ClienteResponseDto> obtenerTodos(Pageable pageable);

    ClienteResponseDto obtenerPorId(Integer id);

    ClienteResponseDto obtenerPorRfc(String rfc);

    ClienteResponseDto crear(ClienteRequestDto request);

    ClienteResponseDto actualizar(Integer id, ClienteRequestDto request);

    void desactivar(Integer id);

    void reactivar(Integer id);

    CuentaCorrienteResponseDto obtenerCuentaCorriente(Integer clienteId);

    AgingResponseDto obtenerAging(LocalDate fechaCorte);

    // Precios especiales
    ClientePrecioEspecialResponseDto crearPrecioEspecial(Integer clienteId, com.abarrotes.oakapi.dto.request.ClientePrecioEspecialRequestDto request);

    List<ClientePrecioEspecialResponseDto> obtenerPreciosEspeciales(Integer clienteId);

    void eliminarPrecioEspecial(Integer clienteId, Integer productoId);
}