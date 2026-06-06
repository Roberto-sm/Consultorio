package com.upsin.demo.controllers;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    // Atrapa todos los "throw new RuntimeException" del proyecto
    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<Map<String, String>> manejarErroresDeNegocio(RuntimeException ex) {

        // Creamos un JSON con la llave "mensaje"
        Map<String, String> respuesta = new HashMap<>();
        respuesta.put("mensaje", ex.getMessage());

        // Devolvemos un código 400 (Bad Request) en lugar de un 500
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(respuesta);
    }
}