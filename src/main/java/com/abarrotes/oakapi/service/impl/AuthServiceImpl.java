package com.abarrotes.oakapi.service.impl;

import com.abarrotes.oakapi.dto.request.LoginRequestDto;
import com.abarrotes.oakapi.dto.response.AuthResponseDto;
import com.abarrotes.oakapi.model.Usuario;
import com.abarrotes.oakapi.repository.UsuarioRepository;
import com.abarrotes.oakapi.security.JwtService;
import com.abarrotes.oakapi.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    @Override
    public AuthResponseDto login(LoginRequestDto request) {
        Usuario usuario = usuarioRepository.findByUsernameAndActivoTrue(request.getUsername())
                .orElseThrow(() -> new BadCredentialsException("Credenciales inválidas o usuario inactivo"));

        if (!passwordEncoder.matches(request.getPassword(), usuario.getPasswordHash())) {
            throw new BadCredentialsException("Credenciales inválidas");
        }

        String token = jwtService.generateToken(usuario);

        return AuthResponseDto.builder()
                .token(token)
                .id(usuario.getId())
                .nombre(usuario.getNombre())
                .username(usuario.getUsername())
                .rol(usuario.getRol())
                .build();
    }
}
