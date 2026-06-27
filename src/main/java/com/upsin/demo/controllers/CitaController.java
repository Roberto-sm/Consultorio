package com.upsin.demo.controllers;

import com.upsin.demo.dto.CitaDTO;
import com.upsin.demo.models.Cita;
import com.upsin.demo.repositories.CitaRepository;
import com.upsin.demo.services.CitaService;
import com.upsin.demo.controllers.docs.CitaApi;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Operation;

import java.util.List;

@Tag(name = "4. Gestión de Citas", description = "Máquina de estados para la agenda de la clínica. Controla validaciones de horario, empalmes, confirmaciones y aplicación de multas financieras.")
@RestController
@RequestMapping("/api/citas")
public class CitaController implements CitaApi {

    @Autowired
    private CitaRepository citaRepository;

    @Autowired
    private CitaService citaService;

    @Operation(summary = "Consultar todas las citas Nota: Esta peticion es exclusiva del rol Admin que actualmente no existe", description = "Genera un volcado completo de la agenda histórica del sistema. Ideal para tableros de administración.")
    @PreAuthorize("hasRole('ADMIN')") // esta peticion es unicamente para el rol ADMIN que actualmente no existe
    @GetMapping()
    public List<Cita> obtenerTodas() {
        return citaRepository.findAll();
    }


    @Override
    @PostMapping("/primera-cita")
    public CitaDTO agendarPrimeraCita(@RequestBody Cita cita) {
        return citaService.agendarPrimeraCita(cita);
    }

    @Override
    @PostMapping("/seguimiento")
    public CitaDTO agendarCitaSeguimiento(@RequestBody Cita cita) {
        return citaService.agendarCitaSeguimiento(cita);
    }

    @Operation(summary = "Aprobar solicitud de cita", description = "Transición en la máquina de estados a 'confirmada'. Bloqueado por seguridad si la fecha de la cita ya caducó.")
    @PreAuthorize("hasRole('PSICOLOGO')")
    @PutMapping("/{id}/aprobar")
    public CitaDTO aprobarCita(@PathVariable Integer id) {
        return citaService.aprobarCita(id);
    }

    @Operation(summary = "Rechazar solicitud de cita", description = "Transición a 'rechazada'. Liberando el bloque horario para otros pacientes.")
    @PreAuthorize("hasRole('PSICOLOGO')")
    @PutMapping("/{id}/rechazar")
    public CitaDTO rechazarCita(@PathVariable Integer id) {
        return citaService.rechazarCita(id);
    }

    @Operation(summary = "Cancelar Cita Agendada", description = "Transición a 'cancelada'. Si el rol 'paciente' ejecuta este endpoint con menos de 20 horas de antelación al inicio de la sesión, el sistema aplica una penalización financiera.")
    @PreAuthorize("hasAnyRole('PACIENTE', 'PSICOLOGO')")
    @PutMapping("/{id}/cancelar")
    public CitaDTO cancelarCita(@PathVariable Integer id) {
        return citaService.cancelarCita(id);
    }

    @Operation(summary = "Marcar Sesión como Finalizada", description = "Cierra el ciclo de la cita habilitando la captura de Notas de Evolución. Por regla de negocio clínica, no puede ejecutarse antes de que hayan transcurrido 50 min de sesión.")
    @PreAuthorize("hasRole('PSICOLOGO')")
    @PutMapping("/{id}/finalizar")
    public CitaDTO finalizarCita(@PathVariable Integer id) {
        return citaService.finalizarCita(id);
    }

    @Operation(summary = "Registrar inasistencia (No-Show)", description = "Transición para penalizar clínicamente el expediente del paciente si no se presentó al consultorio sin previo aviso.")
    @PreAuthorize("hasRole('PSICOLOGO')")
    @PutMapping("/{id}/no-show")
    public CitaDTO registrarNoShow(@PathVariable Integer id) {
        return citaService.registrarNoShow(id);
    }

    @Operation(summary = "Mi Agenda (Paginada)", description = "Lee el Token del psicólogo y devuelve exclusivamente sus citas ordenadas de la más reciente a la más antigua, utilizando paginación para no saturar la red.")
    @PreAuthorize("hasRole('PSICOLOGO')") // <-- SOLO PSICÓLOGOS
    @GetMapping("/mis-citas")
    public Page<CitaDTO> obtenerMisCitas(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        Pageable paginacion = PageRequest.of(page, size);
        return citaService.obtenerMisCitasPaginadas(paginacion);
    }

    @Operation(summary = "Mi Agenda Activa", description = "Devuelve exclusivamente las citas en estado 'pendiente' o 'confirmada' del psicólogo logueado. Paginado y ordenado por fecha de proximidad.")
    @PreAuthorize("hasRole('PSICOLOGO')")
    @GetMapping("/mis-citas/activas")
    public Page<CitaDTO> obtenerMisCitasActivas(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        Pageable paginacion = PageRequest.of(page, size);
        return citaService.obtenerMisCitasActivasPaginadas(paginacion);
    }

    @Override
    @PreAuthorize("hasRole('PSICOLOGO')")
    @PostMapping("/paciente/{idPaciente}/agendar")
    public CitaDTO agendarCitaPorPsicologo(@PathVariable Integer idPaciente, @RequestBody Cita nuevaCita) {
        return citaService.agendarCitaPorPsicologo(idPaciente, nuevaCita);
    }
}