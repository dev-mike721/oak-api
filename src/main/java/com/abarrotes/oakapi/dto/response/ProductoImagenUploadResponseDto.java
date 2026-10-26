package com.abarrotes.oakapi.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductoImagenUploadResponseDto {

    private Integer productoId;
    private String imagenUrl;
    private String nombreOriginal;
    private Long tamañoBytes;
    private String contentType;
    private LocalDateTime fechaActualizacion;
}