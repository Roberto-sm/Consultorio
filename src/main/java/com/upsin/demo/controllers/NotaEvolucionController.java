package com.upsin.demo.controllers;

import com.upsin.demo.models.NotaEvolucion;
import com.upsin.demo.services.NotaEvolucionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Operation;

import java.util.List;

@Tag(name = "5. Notas de Evolución", description = "Endpoints para la captura y lectura cronológica de las observaciones de cada sesión terapéutica.")
@RestController
@RequestMapping("/api/notas")
public class NotaEvolucionController {

    @Autowired
    private NotaEvolucionService notaEvolucionService;

    @Operation(summary = "Capturar hoja de evolución", description = "Redacta una nota clínica vinculándola permanentemente a la sesión (cita) que acaba de concluir. Bloquea duplicados automáticamente.")
    @PreAuthorize("hasRole('PSICOLOGO')")
    @PostMapping("/cita/{idCita}")
    public NotaEvolucion crearNota(@PathVariable Integer idCita, @RequestBody NotaEvolucion nuevaNota) {
        return notaEvolucionService.crearNota(idCita, nuevaNota);
    }

    @Operation(summary = "Consultar el progreso del paciente", description = "Devuelve todas las notas de evolución de un paciente, ordenadas de la más reciente a la más antigua para revisión del especialista.")
    @PreAuthorize("hasRole('PSICOLOGO')")
    @GetMapping("/paciente/{idPaciente}")
    public List<NotaEvolucion> verNotasDePaciente(@PathVariable Integer idPaciente) {
        return notaEvolucionService.consultarNotasDePaciente(idPaciente);
    }
}