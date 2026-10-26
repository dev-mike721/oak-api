package com.abarrotes.oakapi.service;

import com.abarrotes.oakapi.dto.request.SerieEstadoRequestDto;
import com.abarrotes.oakapi.dto.request.SerieRequestDto;
import com.abarrotes.oakapi.dto.response.SerieResponseDto;
import com.abarrotes.oakapi.dto.response.SeriesDisponiblesResponseDto;
import com.abarrotes.oakapi.model.enums.EstadoSerie;

import java.util.List;

public interface SerieService {

    List<SerieResponseDto> registrarSeriesEnCompra(Integer productoId, Integer loteId, List<String> codigos);

    List<SerieResponseDto> obtenerDisponiblesParaVenta(Integer productoId, Integer cantidad);

    List<SerieResponseDto> venderSeries(Integer productoId, List<String> codigos, String ventaId);

    List<SerieResponseDto> liberarSeriesPorCancelacion(String ventaId);

    SerieResponseDto cambiarEstado(Integer serieId, SerieEstadoRequestDto request);

    List<SeriesDisponiblesResponseDto> obtenerDisponibles(Integer productoId, Integer limite);
}