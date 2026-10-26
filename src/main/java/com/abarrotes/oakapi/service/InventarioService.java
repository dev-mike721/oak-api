package com.abarrotes.oakapi.service;

import com.abarrotes.oakapi.dto.request.AjusteInventarioRequestDto;
import com.abarrotes.oakapi.dto.response.AlertasCaducidadResponseDto;
import com.abarrotes.oakapi.dto.response.LoteResponseDto;
import com.abarrotes.oakapi.dto.response.MovimientoInventarioResponseDto;
import com.abarrotes.oakapi.dto.response.ValorizacionResponseDto;
import com.abarrotes.oakapi.model.enums.TipoMovimientoInventario;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.util.List;

public interface InventarioService {

    List<LoteResponseDto> lotesPorProducto(Integer productoId);

    MovimientoInventarioResponseDto registrarAjuste(AjusteInventarioRequestDto request);

    AlertasCaducidadResponseDto alertasCaducidad(Integer dias);

    ValorizacionResponseDto valorizacion();

    Page<MovimientoInventarioResponseDto> historial(Integer loteId, Integer productoId,
                                                    TipoMovimientoInventario tipo, Pageable pageable);

    List<MovimientoInventarioResponseDto> consumirStockFEFO(Integer productoId, BigDecimal cantidad,
                                                            Integer usuarioId, String documentoVentaId);
}
