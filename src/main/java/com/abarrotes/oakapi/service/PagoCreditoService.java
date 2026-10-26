package com.abarrotes.oakapi.service;

import com.abarrotes.oakapi.dto.request.PagoCreditoRequestDto;
import com.abarrotes.oakapi.dto.response.CuentaCorrienteResponseDto;
import com.abarrotes.oakapi.dto.response.PagoCreditoAplicacionResponseDto;
import com.abarrotes.oakapi.dto.response.PagoCreditoResponseDto;

import java.math.BigDecimal;
import java.util.List;

public interface PagoCreditoService {

    PagoCreditoResponseDto registrarPago(PagoCreditoRequestDto request);

    List<PagoCreditoResponseDto> obtenerPorCliente(Integer clienteId);

    PagoCreditoResponseDto obtenerPorId(Integer id);

    List<PagoCreditoAplicacionResponseDto> obtenerAplicaciones(Integer pagoId);

    void aplicarFIFO(Integer pagoId);

    CuentaCorrienteResponseDto obtenerCuentaCorriente(Integer clienteId);

    void anularPago(Integer pagoId);
}