package com.upsin.demo.services;

import com.upsin.demo.models.Cita;
import com.upsin.demo.models.Psicologo;
import com.upsin.demo.repositories.CitaRepository;
import com.upsin.demo.repositories.PsicologoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class CitaService {

    @Autowired
    private CitaRepository citaRepository;

    @Autowired
    private PsicologoRepository psicologoRepository;

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
}