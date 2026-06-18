package com.upsin.demo.controllers;

import com.upsin.demo.models.Cita;
import com.upsin.demo.repositories.CitaRepository;
import com.upsin.demo.services.CitaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Operation;

import java.util.List;

@Tag(name = "2. Gestión de Citas", description = "Máquina de estados para agendar, confirmar, cancelar (con penalizaciones) y finalizar sesiones.")
@RestController
@RequestMapping("/api/citas")
public class CitaController {

    @Autowired
    private CitaRepository citaRepository;

    @Autowired
    private CitaService citaService;

    // Obtener todas las citas del sistema (Ideal para el perfil Administrador)
    @GetMapping
    public List<Cita> obtenerTodas() {
        return citaRepository.findAll();
    }

    // Obtener solo las citas de un psicólogo en específico (Para pintar su calendario)
    @GetMapping("/psicologo/{id}")
    public List<Cita> obtenerPorPsicologo(@PathVariable Integer id) {
        return citaRepository.findByPsicologoId(id);
    }

    // NUEVO ENDPOINT: Recibe una petición POST para crear una cita
    @PostMapping("/primera-cita")
    public Cita agendarPrimeraCita(@RequestBody Cita cita) {
        return citaService.agendarPrimeraCita(cita);
    }

    @PostMapping("/seguimiento")
    public Cita agendarCitaSeguimiento(@RequestBody Cita cita) { return citaService.agendarCitaSeguimiento(cita); }

    @PreAuthorize("hasRole('PSICOLOGO')")
    @PutMapping("/{id}/aprobar")
    public Cita aprobarCita(@PathVariable Integer id) {
        return citaService.aprobarCita(id);
    }

    @PreAuthorize("hasRole('PSICOLOGO')")
    @PutMapping("/{id}/rechazar")
    public Cita rechazarCita(@PathVariable Integer id) {
        return citaService.rechazarCita(id);
    }

    @Operation(
            summary = "Cancelar una cita agendada",
            description = "Permite a un paciente o psicólogo cancelar una cita. Si el paciente cancela con menos de 24 horas de anticipación, el sistema aplica una penalización automática y guarda una huella de auditoría."
    )
    @PreAuthorize("hasAnyRole('PACIENTE', 'PSICOLOGO')")
    @PutMapping("/{id}/cancelar")
    public Cita cancelarCita(@PathVariable Integer id) {
        return citaService.cancelarCita(id);
    }

    @PreAuthorize("hasRole('PSICOLOGO')")
    @PutMapping("/{id}/finalizar")
    public Cita finalizarCita(@PathVariable Integer id) {
        return citaService.finalizarCita(id);
    }

    @PreAuthorize("hasRole('PSICOLOGO')")
    @PutMapping("/{id}/no-show")
    public Cita registrarNoShow(@PathVariable Integer id) {
        return citaService.registrarNoShow(id);
    }
}