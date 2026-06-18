package com.upsin.demo.config;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Date;

/**
 * Utilería criptográfica para la gestión de JSON Web Tokens (JWT).
 * Se encarga de la generación, firmado matemático, validación de caducidad
 * y extracción de Claims (payload) de los tokens de autenticación.
 */
@Component
public class JwtUtil {

    // Llave maestra autogenerada de 256 bits para firmar el token (Firma digital HMAC-SHA256)
    private final Key SECRET_KEY = Keys.secretKeyFor(SignatureAlgorithm.HS256);

    // Tiempo de vida del token: 24 horas en milisegundos
    private final long EXPIRATION_TIME = 86400000;

    /**
     * Genera un token JWT firmado para un usuario recién autenticado.
     * * @param correo Identificador principal del usuario (Subject).
     * @param rol Rol de autorización (Claim personalizado).
     * @return Cadena JWT encriptada.
     */
    public String generarToken(String correo, String rol) {
        return Jwts.builder()
                .setSubject(correo)
                .claim("rol", rol)
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + EXPIRATION_TIME))
                .signWith(SECRET_KEY)
                .compact();
    }

    public String extraerCorreo(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(SECRET_KEY)
                .build()
                .parseClaimsJws(token)
                .getBody()
                .getSubject();
    }

    /**
     * Verifica que el token no haya sido manipulado por terceros y que aún no caduque.
     */
    public boolean validarToken(String token) {
        try {
            Jwts.parserBuilder().setSigningKey(SECRET_KEY).build().parseClaimsJws(token);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public String extraerRol(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(SECRET_KEY)
                .build()
                .parseClaimsJws(token)
                .getBody()
                .get("rol", String.class);
    }
}