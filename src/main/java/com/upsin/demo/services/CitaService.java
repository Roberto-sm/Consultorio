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
        // 1. Buscamos al psicólogo de planta
        Psicologo psicologoDePlanta = psicologoRepository.findFirstByEsDePlantaTrue()
                .orElseThrow(() -> new RuntimeException("Error: No hay psicólogo de planta disponible"));

        // 2. Le asignamos este psicólogo a la cita
        nuevaCita.setPsicologo(psicologoDePlanta);

        // 3. Establecemos las reglas iniciales
        nuevaCita.setEstado("pendiente");
        nuevaCita.setEsPrimera(true);

        // 4. Guardamos la cita en la base de datos
        return citaRepository.save(nuevaCita);
    }
}