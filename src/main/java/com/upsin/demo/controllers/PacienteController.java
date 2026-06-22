package com.upsin.demo.controllers;

import com.upsin.demo.dto.PacienteDTO;
import com.upsin.demo.models.Paciente;
import com.upsin.demo.services.PacienteService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Operation;

@Tag(name = "6. Gestión de Pacientes", description = "Operaciones administrativas y reasignación de casos clínicos.")
@RestController
@RequestMapping("/api/pacientes")
public class PacienteController {

    @Autowired
    private PacienteService pacienteService;

    @Operation(summary = "Derivar paciente a especialista", description = "Transfiere el caso clínico a un nuevo psicólogo. Valida que el médico destino no sea de triaje y cierra automáticamente cualquier cita inicial pendiente. Requiere rol PSICOLOGO.")
    @PreAuthorize("hasRole('PSICOLOGO')")
    @PutMapping("/{idPaciente}/derivar/{idNuevoPsicologo}")
    public PacienteDTO derivarPaciente(@PathVariable Integer idPaciente, @PathVariable Integer idNuevoPsicologo) {
        return pacienteService.derivarPaciente(idPaciente, idNuevoPsicologo);
    }
}