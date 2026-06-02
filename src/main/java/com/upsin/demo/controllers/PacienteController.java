package com.upsin.demo.controllers;

import com.upsin.demo.models.Paciente;
import com.upsin.demo.services.PacienteService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/pacientes")
public class PacienteController {

    @Autowired
    private PacienteService pacienteService;

    // Protegemos el endpoint: ¡Solo psicólogos!
    @PreAuthorize("hasRole('PSICOLOGO')")
    @PutMapping("/{idPaciente}/derivar/{idNuevoPsicologo}")
    public Paciente derivarPaciente(@PathVariable Integer idPaciente, @PathVariable Integer idNuevoPsicologo) {
        return pacienteService.derivarPaciente(idPaciente, idNuevoPsicologo);
    }
}