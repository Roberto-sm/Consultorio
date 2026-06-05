package com.upsin.demo.services;

import com.upsin.demo.models.Cita;
import com.upsin.demo.models.Paciente;
import com.upsin.demo.models.Psicologo;
import com.upsin.demo.repositories.CitaRepository;
import com.upsin.demo.repositories.PacienteRepository;
import com.upsin.demo.repositories.PsicologoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class CitaService {

    @Autowired
    private CitaRepository citaRepository;

    @Autowired
    private PsicologoRepository psicologoRepository;

    @Autowired
    private PacienteRepository pacienteRepository;

    public Cita agendarPrimeraCita(Cita nuevaCita) {
        Integer idPaciente = nuevaCita.getPaciente().getId();

        // --- Un paciente solo puede tener UNA cita activa ---
        if (citaRepository.existsByPacienteIdAndEstado(idPaciente, "pendiente")) {
            throw new RuntimeException("Error: El paciente ya tiene una cita pendiente activa.");
        }

        // Buscamos al psicólogo de planta
        Psicologo psicologoDePlanta = psicologoRepository.findFirstByEsDePlantaTrue()
                .orElseThrow(() -> new RuntimeException("Error: No hay psicólogo de planta disponible"));

        if (citaRepository.existsByPsicologoIdAndFechaHoraAndEstado(
                psicologoDePlanta.getId(), nuevaCita.getFechaHora(), "pendiente")) {
            throw new RuntimeException("Error: El horario seleccionado ya está ocupado. Por favor, elige otra hora.");
        }

        //  Le asignamos este psicólogo a la cita
        nuevaCita.setPsicologo(psicologoDePlanta);

        //  Establecemos las reglas iniciales
        nuevaCita.setEstado("pendiente");
        nuevaCita.setEsPrimera(true);

        //  Guardamos la cita en la base de datos
        return citaRepository.save(nuevaCita);
    }

    public Cita agendarCitaSeguimiento(Cita nuevaCita) {
        Integer idPaciente = nuevaCita.getPaciente().getId();

        // Buscamos al paciente en la base de datos para ver a quién pertenece
        Paciente paciente = pacienteRepository.findById(idPaciente)
                .orElseThrow(() -> new RuntimeException("Error: Paciente no encontrado."));

        // Validamos si el paciente ya fue derivado a un especialista
        if (paciente.getPsicologo() == null) {
            throw new RuntimeException("Error: El paciente aún no tiene un psicólogo asignado. Debe agendar una primera cita de triaje.");
        }

        // Regla de Concurrencia: ¿Ya tiene una cita activa?
        if (citaRepository.existsByPacienteIdAndEstado(idPaciente, "pendiente")) {
            throw new RuntimeException("Error: El paciente ya tiene una cita pendiente activa.");
        }

        // Extraemos a su especialista asignado
        Psicologo especialista = paciente.getPsicologo();

        // Regla de Empalme: ¿El especialista está libre a esa hora?
        if (citaRepository.existsByPsicologoIdAndFechaHoraAndEstado(
                especialista.getId(), nuevaCita.getFechaHora(), "pendiente")) {
            throw new RuntimeException("Error: El horario del especialista ya está ocupado. Por favor, elige otra hora.");
        }

        // Agendamos la cita con SU especialista
        nuevaCita.setPsicologo(especialista);
        nuevaCita.setEstado("pendiente");
        nuevaCita.setEsPrimera(false); // Ya no es su primera vez

        return citaRepository.save(nuevaCita);
    }
}