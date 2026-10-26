package com.abarrotes.oakapi.service;

import com.abarrotes.oakapi.dto.request.ConvertirPresupuestoRequestDto;
import com.abarrotes.oakapi.dto.request.PresupuestoDetalleRequestDto;
import com.abarrotes.oakapi.dto.request.PresupuestoRequestDto;
import com.abarrotes.oakapi.dto.response.PresupuestoDetalleResponseDto;
import com.abarrotes.oakapi.dto.response.PresupuestoResponseDto;
import com.abarrotes.oakapi.model.enums.EstadoPresupuesto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.util.List;

public interface PresupuestoService {

    Page<PresupuestoResponseDto> obtenerTodos(EstadoPresupuesto estado, Integer clienteId, Integer usuarioId,
                                              LocalDate fechaDesde, LocalDate fechaHasta, Pageable pageable);

    PresupuestoResponseDto obtenerPorId(String id);

    PresupuestoResponseDto crear(PresupuestoRequestDto request);

    PresupuestoResponseDto enviar(String id);

    PresupuestoResponseDto aprobar(String id);

    PresupuestoResponseDto convertirAVenta(String id, ConvertirPresupuestoRequestDto request);

    PresupuestoResponseDto cancelar(String id);

    void vencerPresupuestos();
}