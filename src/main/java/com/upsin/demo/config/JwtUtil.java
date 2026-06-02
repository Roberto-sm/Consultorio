package com.upsin.demo.config;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Date;

@Component
public class JwtUtil {

    // En un entorno de producción esto iría en application.properties
    // Generamos una llave aleatoria de 256 bits para firmar el token
    private final Key SECRET_KEY = Keys.secretKeyFor(SignatureAlgorithm.HS256);

    // El token durará 24 horas (en milisegundos)
    private final long EXPIRATION_TIME = 86400000;

    public String generarToken(String correo, String rol) {
        return Jwts.builder()
                .setSubject(correo) // Dueño del token
                .claim("rol", rol)  // Guardamos el rol dentro del token para usarlo después
                .setIssuedAt(new Date(System.currentTimeMillis())) // Fecha de creación
                .setExpiration(new Date(System.currentTimeMillis() + EXPIRATION_TIME)) // Fecha de caducidad
                .signWith(SECRET_KEY) // Firmamos matemáticamente el token
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

    public boolean validarToken(String token) {
        try {
            Jwts.parserBuilder().setSigningKey(SECRET_KEY).build().parseClaimsJws(token);
            return true;
        } catch (Exception e) {
            return false; // Si el token caducó o fue modificado
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