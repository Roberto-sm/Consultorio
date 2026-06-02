package com.upsin.demo.controllers;

import com.upsin.demo.models.HistorialClinico;
import com.upsin.demo.repositories.HistorialClinicoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/historial")
public class HistorialClinicoController {

    @Autowired
    private HistorialClinicoRepository historialRepository;

    // Solo los psicólogos pueden consultar historiales
    @PreAuthorize("hasRole('PSICOLOGO')")
    @GetMapping("/paciente/{pacienteId}")
    public List<HistorialClinico> verHistorialDePaciente(@PathVariable Integer pacienteId) {
        return historialRepository.findByPacienteIdOrderByFechaHoraDesc(pacienteId);
    }

    // Solo los psicólogos pueden escribir notas nuevas
    @PreAuthorize("hasRole('PSICOLOGO')")
    @PostMapping
    public HistorialClinico agregarNota(@RequestBody HistorialClinico nota) {
        nota.setFechaHora(LocalDateTime.now()); // Automatizamos la fecha y hora exacta
        return historialRepository.save(nota);
    }
}