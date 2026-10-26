package com.abarrotes.oakapi.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.web.multipart.MultipartFile;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductoImagenUploadRequest {

    @NotNull(message = "El archivo de imagen es obligatorio")
    private MultipartFile archivo;
}