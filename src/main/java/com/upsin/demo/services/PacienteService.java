package com.upsin.demo.services;

import com.upsin.demo.models.Paciente;
import com.upsin.demo.models.Psicologo;
import com.upsin.demo.repositories.PacienteRepository;
import com.upsin.demo.repositories.PsicologoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class PacienteService {

    @Autowired
    private PacienteRepository pacienteRepository;

    @Autowired
    private PsicologoRepository psicologoRepository;

    public Paciente derivarPaciente(Integer idPaciente, Integer idNuevoPsicologo) {

        //  Buscamos al paciente
        Paciente paciente = pacienteRepository.findById(idPaciente)
                .orElseThrow(() -> new RuntimeException("Error: Paciente no encontrado"));

        // Buscamos al psicólogo especialista al que será transferido
        Psicologo nuevoPsicologo = psicologoRepository.findById(idNuevoPsicologo)
                .orElseThrow(() -> new RuntimeException("Error: Psicólogo no encontrado"));

        // Validamos que no lo estén derivando a otro psicólogo de planta
        if (nuevoPsicologo.getEsDePlanta() != null && nuevoPsicologo.getEsDePlanta()) {
            throw new RuntimeException("Error: No puedes derivar a un paciente a otro psicólogo de triaje");
        }

        //  Hacemos la transferencia oficial
        paciente.setPsicologo(nuevoPsicologo);

        //  Guardamos los cambios en MySQL
        return pacienteRepository.save(paciente);
    }
}