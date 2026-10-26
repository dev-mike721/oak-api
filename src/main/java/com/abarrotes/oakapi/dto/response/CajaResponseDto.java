package com.abarrotes.oakapi.dto.response;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class CajaResponseDto {

    private Integer id;
    private String nombre;
    private String ubicacion;
    private Boolean activa;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}