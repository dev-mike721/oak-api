package com.abarrotes.oakapi.service;

import com.abarrotes.oakapi.dto.request.LoginRequestDto;
import com.abarrotes.oakapi.dto.response.AuthResponseDto;

public interface AuthService {
    AuthResponseDto login(LoginRequestDto request);
}
