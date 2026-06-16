package com.upsin.demo.controllers;

import com.upsin.demo.models.NotaEvolucion;
import com.upsin.demo.services.NotaEvolucionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/notas")
public class NotaEvolucionController {

    @Autowired
    private NotaEvolucionService notaEvolucionService;

    // Crear una nueva nota al terminar una cita
    @PreAuthorize("hasRole('PSICOLOGO')")
    @PostMapping("/cita/{idCita}")
    public NotaEvolucion crearNota(@PathVariable Integer idCita, @RequestBody NotaEvolucion nuevaNota) {
        return notaEvolucionService.crearNota(idCita, nuevaNota);
    }

    // Ver todas las hojas del expediente de un paciente
    @PreAuthorize("hasRole('PSICOLOGO')")
    @GetMapping("/paciente/{idPaciente}")
    public List<NotaEvolucion> verNotasDePaciente(@PathVariable Integer idPaciente) {
        return notaEvolucionService.consultarNotasDePaciente(idPaciente);
    }
}