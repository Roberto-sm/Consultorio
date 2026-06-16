package com.upsin.demo.controllers;

import com.upsin.demo.models.HistorialClinico;
import com.upsin.demo.services.HistorialClinicoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/historial")
public class HistorialClinicoController {

    @Autowired
    private HistorialClinicoService historialClinicoService;

    // EL EQUIVALENTE A TU ANTIGUO GET (Pero ahora devuelve un solo objeto, no una lista)
    @PreAuthorize("hasRole('PSICOLOGO')")
    @GetMapping("/paciente/{pacienteId}")
    public HistorialClinico verHistorialBase(@PathVariable Integer pacienteId) {
        return historialClinicoService.obtenerPorPaciente(pacienteId);
    }

    // EL NUEVO ENDPOINT PARA LLENAR LOS DATOS (El Triaje)
    @PreAuthorize("hasRole('PSICOLOGO')")
    @PutMapping("/paciente/{pacienteId}")
    public HistorialClinico actualizarAntecedentes(
            @PathVariable Integer pacienteId,
            @RequestBody HistorialClinico datosNuevos) {

        return historialClinicoService.actualizarAntecedentes(pacienteId, datosNuevos);
    }
}