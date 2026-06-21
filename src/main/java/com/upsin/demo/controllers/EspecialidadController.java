package com.upsin.demo.controllers;

import com.upsin.demo.models.Especialidad;
import com.upsin.demo.repositories.EspecialidadRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Operation;

import java.util.List;

@Tag(name = "1. Catálogo de Especialidades", description = "Endpoints de solo lectura para listar las áreas de atención de la clínica.")
@RestController
@RequestMapping("/api/especialidades")
public class EspecialidadController {

    @Autowired
    private EspecialidadRepository especialidadRepository;

    @Operation(summary = "Obtener todas las especialidades", description = "Devuelve el catálogo completo de especialidades dadas de alta en el sistema. Endpoint público de consulta.")
    @GetMapping
    public Page<Especialidad> obtenerTodas(
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "10") int size) {
            // Ejecutamos la consulta paginada directamente desde el repositorio
            return especialidadRepository.findAll(PageRequest.of(page, size));
        }
}