package com.abarrotes.oakapi.dto.response;

import com.abarrotes.oakapi.model.enums.Rol;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@AllArgsConstructor
public class AuthResponseDto {
    private String token;
    private Integer id;
    private String nombre;
    private String username;
    private Rol rol;
}
