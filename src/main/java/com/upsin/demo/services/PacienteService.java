package com.upsin.demo.services;

import com.upsin.demo.dto.PacienteDTO;
import com.upsin.demo.models.Cita;
import com.upsin.demo.models.Paciente;
import com.upsin.demo.models.Psicologo;
import com.upsin.demo.repositories.CitaRepository;
import com.upsin.demo.repositories.PacienteRepository;
import com.upsin.demo.repositories.PsicologoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

/**
 * Lógica de negocio para la gestión administrativa de los pacientes.
 */
@Service
public class PacienteService {

    @Autowired
    private PacienteRepository pacienteRepository;

    @Autowired
    private PsicologoRepository psicologoRepository;

    @Autowired
    private CitaRepository citaRepository;

    /**
     * Operación transaccional para la transferencia de casos clínicos (Derivación).
     * Asegura que el paciente pase de la etapa de triaje al especialista adecuado.
     * * @param idPaciente Identificador del paciente a transferir.
     * @param idNuevoPsicologo Identificador del nuevo especialista.
     * @return El registro del paciente actualizado en la base de datos.
     */
    @Transactional
    public PacienteDTO derivarPaciente(Integer idPaciente, Integer idNuevoPsicologo) {

        Paciente paciente = pacienteRepository.findById(idPaciente)
                .orElseThrow(() -> new RuntimeException("Error: Paciente no encontrado"));

        Psicologo nuevoPsicologo = psicologoRepository.findById(idNuevoPsicologo)
                .orElseThrow(() -> new RuntimeException("Error: Psicólogo no encontrado"));

        if (nuevoPsicologo.getEsDePlanta() != null && nuevoPsicologo.getEsDePlanta()) {
            throw new RuntimeException("Error: No puedes derivar a un paciente a otro psicólogo de triaje");
        }

        // Regla de negocio: Si existe una cita de triaje pendiente, la finaliza automáticamente
        Optional<Cita> citaPendiente = citaRepository.findFirstByPacienteIdAndEstado(idPaciente, "pendiente");

        if (citaPendiente.isPresent()) {
            Cita cita = citaPendiente.get();
            cita.setEstado("finalizada");
            citaRepository.save(cita);
        }

        paciente.setPsicologo(nuevoPsicologo);
        return convertirAPacienteDTO(pacienteRepository.save(paciente));
    }

    private PacienteDTO convertirAPacienteDTO(Paciente paciente) {
        PacienteDTO dto = new PacienteDTO();
        dto.setIdPaciente(paciente.getId());
        dto.setPenalizacionActiva(paciente.getPenalizacionActiva());

        if (paciente.getUsuario() != null) {
            dto.setNombrePaciente(paciente.getUsuario().getNombre());
        }

        if (paciente.getPsicologo() != null) {
            dto.setIdPsicologoAsignado(paciente.getPsicologo().getId());
            if (paciente.getPsicologo().getUsuario() != null) {
                dto.setNombrePsicologoAsignado(paciente.getPsicologo().getUsuario().getNombre());
            }
        }

        return dto;
    }
}