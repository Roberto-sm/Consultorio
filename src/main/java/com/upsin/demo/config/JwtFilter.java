package com.upsin.demo.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.Collections;
import java.util.List;
import java.io.IOException;

/**
 * Interceptor de seguridad HTTP (Stateless).
 * Se ejecuta una vez por cada petición HTTP entrante. Su responsabilidad es buscar,
 * extraer y validar el token JWT del encabezado 'Authorization' para inyectar el
 * contexto de seguridad de Spring Security antes de llegar a los controladores.
 */
@Component
public class JwtFilter extends OncePerRequestFilter {

    @Autowired
    private JwtUtil jwtUtil;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        // Extraemos el header "Authorization" de la petición
        String authHeader = request.getHeader("Authorization");

        // Verificamos que exista y que cumpla con el estándar Bearer
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);

            // Si la validación criptográfica es exitosa, autorizamos la petición
            if (jwtUtil.validarToken(token)) {
                String correo = jwtUtil.extraerCorreo(token);
                String rol = jwtUtil.extraerRol(token);

                List<GrantedAuthority> authorities = Collections.singletonList(
                        new SimpleGrantedAuthority("ROLE_" + rol.toUpperCase())
                );

                UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(correo, null, authorities);
                SecurityContextHolder.getContext().setAuthentication(auth);
            }
        }

        // Continua la cadena de filtros de Spring Security
        filterChain.doFilter(request, response);
    }
}