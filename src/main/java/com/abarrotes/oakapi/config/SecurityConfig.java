package com.abarrotes.oakapi.config;

import com.abarrotes.oakapi.security.JwtAuthenticationFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthFilter;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .cors(Customizer.withDefaults())
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/auth/**", "/error", "/swagger-ui/**", "/v3/api-docs/**", "/actuator/health", "/actuator/info").permitAll()
                        .requestMatchers(HttpMethod.GET, "/productos/*/foto/archivo").permitAll()
                        // Endpoints de gestión (requieren ADMIN/GERENTE)
                        .requestMatchers("/clientes/**").hasAnyRole("ADMINISTRADOR", "GERENTE")
                        .requestMatchers("/listas-precios/**").hasAnyRole("ADMINISTRADOR", "GERENTE")
                        .requestMatchers("/presupuestos/**").hasAnyRole("ADMINISTRADOR", "GERENTE")
                        .requestMatchers("/impuestos/**").hasAnyRole("ADMINISTRADOR", "GERENTE")
                        .requestMatchers("/series/**").hasAnyRole("ADMINISTRADOR", "GERENTE")
                        .requestMatchers("/cajas/**").hasAnyRole("ADMINISTRADOR", "GERENTE")
                        .requestMatchers("/auditoria/**").hasAnyRole("ADMINISTRADOR", "GERENTE")
                        .requestMatchers(HttpMethod.GET, "/categorias/**").hasAnyRole("ADMINISTRADOR", "GERENTE", "CAJERO")
                        .requestMatchers("/categorias/**").hasAnyRole("ADMINISTRADOR", "GERENTE")
                        .requestMatchers("/pagos-credito/**").hasAnyRole("ADMINISTRADOR", "GERENTE")
                        .requestMatchers("/turnos/{id}/autorizar", "/turnos/{id}/rechazar").hasAnyRole("ADMINISTRADOR", "GERENTE")
                        // Endpoints operativos (CAJERO, ADMINISTRADOR, GERENTE)
                        .requestMatchers("/turnos/solicitar", "/turnos/actual", "/turnos/caja/**").hasAnyRole("CAJERO", "ADMINISTRADOR", "GERENTE")
                        .requestMatchers("/ventas/**").hasAnyRole("CAJERO", "ADMINISTRADOR", "GERENTE")
                        .requestMatchers("/compras/**").hasAnyRole("CAJERO", "ADMINISTRADOR", "GERENTE")
                        .requestMatchers("/inventario/**").hasAnyRole("CAJERO", "ADMINISTRADOR", "GERENTE")
                        .requestMatchers("/productos/**").hasAnyRole("CAJERO", "ADMINISTRADOR", "GERENTE")
                        .requestMatchers("/proveedores/**").hasAnyRole("CAJERO", "ADMINISTRADOR", "GERENTE")
                        .anyRequest().authenticated()
                )
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}