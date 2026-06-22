package com.upsin.demo.controllers;

import com.upsin.demo.dto.HistorialClinicoDTO;
import com.upsin.demo.models.HistorialClinico;
import com.upsin.demo.services.HistorialClinicoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Operation;

@Tag(name = "7. Expediente Clínico", description = "Manejo de antecedentes generales y estructura base del paciente.")
@RestController
@RequestMapping("/api/historial")
public class HistorialClinicoController {

    @Autowired
    private HistorialClinicoService historialClinicoService;

    @Operation(summary = "Consultar el historial base de un paciente", description = "Devuelve la entidad principal del expediente (antecedentes fijos). Requiere rol PSICOLOGO.")
    @PreAuthorize("hasRole('PSICOLOGO')")
    @GetMapping("/paciente/{pacienteId}")
    public HistorialClinicoDTO verHistorialBase(@PathVariable Integer pacienteId) { // <-- CAMBIAR TIPO DE RETORNO
        return historialClinicoService.obtenerPorPaciente(pacienteId);
    }

    @Operation(summary = "Actualizar triaje y antecedentes", description = "Modifica los campos de antecedentes médicos y familiares. Protege la inmutabilidad de la fecha de creación original.")
    @PreAuthorize("hasRole('PSICOLOGO')")
    @PutMapping("/paciente/{pacienteId}")
    public HistorialClinicoDTO actualizarAntecedentes(
            @PathVariable Integer pacienteId,
            @RequestBody HistorialClinico datosActualizados) {

        return historialClinicoService.actualizarAntecedentes(pacienteId, datosActualizados);
    }
}