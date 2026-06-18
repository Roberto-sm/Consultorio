package com.upsin.demo.controllers;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

/**
 * Interceptor global de excepciones (Controller Advice).
 * Se encarga de atrapar errores en la capa de negocio o de validación antes de que
 * lleguen al cliente, formateándolos en un JSON estructurado y amigable.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * Captura las excepciones de regla de negocio lanzadas manualmente en los Servicios.
     */
    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<Map<String, String>> manejarErroresDeNegocio(RuntimeException ex) {
        Map<String, String> respuesta = new HashMap<>();
        respuesta.put("mensaje", ex.getMessage());

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(respuesta);
    }

    /**
     * Captura los errores arrojados por el blindaje @Valid en los controladores.
     * @return Diccionario JSON con los campos específicos que fallaron y el motivo.
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, String>> manejarValidaciones(MethodArgumentNotValidException ex) {
        Map<String, String> errores = new HashMap<>();

        for (FieldError error : ex.getBindingResult().getFieldErrors()) {
            errores.put(error.getField(), error.getDefaultMessage());
        }

        return new ResponseEntity<>(errores, HttpStatus.BAD_REQUEST);
    }
}