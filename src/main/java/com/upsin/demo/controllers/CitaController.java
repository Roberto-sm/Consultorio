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

@Tag(name = "2. Gestión de Citas", description = "Máquina de estados para la agenda de la clínica. Controla validaciones de horario, empalmes, confirmaciones y aplicación de multas financieras.")
@RestController
@RequestMapping("/api/citas")
public class CitaController {

    @Autowired
    private CitaRepository citaRepository;

    @Autowired
    private CitaService citaService;

    @Operation(summary = "Consultar todas las citas", description = "Genera un volcado completo de la agenda histórica del sistema. Ideal para tableros de administración.")
    @GetMapping
    public List<Cita> obtenerTodas() {
        return citaRepository.findAll();
    }

    @Operation(summary = "Consultar agenda por especialista", description = "Obtiene las sesiones filtradas por el identificador de un psicólogo. Utilizado para pintar la vista de calendario del médico.")
    @GetMapping("/psicologo/{id}")
    public List<Cita> obtenerPorPsicologo(@PathVariable Integer id) {
        return citaRepository.findByPsicologoId(id);
    }

    @Operation(summary = "Agendar Primera Sesión (Triaje)", description = "Petición del paciente para su primera evaluación. El sistema le asigna automáticamente al psicólogo de planta y valida que el horario esté libre de empalmes.")
    @PostMapping("/primera-cita")
    public Cita agendarPrimeraCita(@RequestBody Cita cita) {
        return citaService.agendarPrimeraCita(cita);
    }

    @Operation(summary = "Agendar Sesión de Seguimiento", description = "Petición del paciente regular. El sistema enruta automáticamente la solicitud al especialista que el paciente tiene asignado en su perfil.")
    @PostMapping("/seguimiento")
    public Cita agendarCitaSeguimiento(@RequestBody Cita cita) {
        return citaService.agendarCitaSeguimiento(cita);
    }

    @Operation(summary = "Aprobar solicitud de cita", description = "Transición en la máquina de estados a 'confirmada'. Bloqueado por seguridad si la fecha de la cita ya caducó.")
    @PreAuthorize("hasRole('PSICOLOGO')")
    @PutMapping("/{id}/aprobar")
    public Cita aprobarCita(@PathVariable Integer id) {
        return citaService.aprobarCita(id);
    }

    @Operation(summary = "Rechazar solicitud de cita", description = "Transición a 'rechazada'. Liberando el bloque horario para otros pacientes.")
    @PreAuthorize("hasRole('PSICOLOGO')")
    @PutMapping("/{id}/rechazar")
    public Cita rechazarCita(@PathVariable Integer id) {
        return citaService.rechazarCita(id);
    }

    @Operation(summary = "Cancelar Cita Agendada", description = "Transición a 'cancelada'. Si el rol 'paciente' ejecuta este endpoint con menos de 20 horas de antelación al inicio de la sesión, el sistema aplica una penalización financiera.")
    @PreAuthorize("hasAnyRole('PACIENTE', 'PSICOLOGO')")
    @PutMapping("/{id}/cancelar")
    public Cita cancelarCita(@PathVariable Integer id) {
        return citaService.cancelarCita(id);
    }

    @Operation(summary = "Marcar Sesión como Finalizada", description = "Cierra el ciclo de la cita habilitando la captura de Notas de Evolución. Por regla de negocio clínica, no puede ejecutarse antes de que hayan transcurrido 50 min de sesión.")
    @PreAuthorize("hasRole('PSICOLOGO')")
    @PutMapping("/{id}/finalizar")
    public Cita finalizarCita(@PathVariable Integer id) {
        return citaService.finalizarCita(id);
    }

    @Operation(summary = "Registrar inasistencia (No-Show)", description = "Transición para penalizar clínicamente el expediente del paciente si no se presentó al consultorio sin previo aviso.")
    @PreAuthorize("hasRole('PSICOLOGO')")
    @PutMapping("/{id}/no-show")
    public Cita registrarNoShow(@PathVariable Integer id) {
        return citaService.registrarNoShow(id);
    }
}