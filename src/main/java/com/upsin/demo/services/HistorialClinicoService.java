package com.upsin.demo.services;

import com.upsin.demo.models.HistorialClinico;
import com.upsin.demo.models.Paciente;
import com.upsin.demo.models.Usuario;
import com.upsin.demo.repositories.HistorialClinicoRepository;
import com.upsin.demo.repositories.UsuarioRepository;
import com.upsin.demo.dto.HistorialClinicoDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

/**
 * Servicio encargado de la manipulación del expediente médico base.
 * Controla que únicamente el personal clínico autorizado pueda sobreescribir antecedentes.
 */
@Service
public class HistorialClinicoService {

    @Autowired
    private HistorialClinicoRepository historialClinicoRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    /**
     * Actualiza los antecedentes médicos y familiares del paciente.
     * Valida mediante el contexto de Spring Security que la petición provenga de un psicólogo.
     */
    public HistorialClinico actualizarAntecedentes(Integer idPaciente, HistorialClinico datosNuevos) {

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        Usuario usuarioLogueado = usuarioRepository.findByCorreo(auth.getName())
                .orElseThrow(() -> new RuntimeException("Error: Usuario no encontrado"));

        if (!usuarioLogueado.getRol().equalsIgnoreCase("psicologo")) {
            throw new RuntimeException("Error de seguridad: Solo el personal de psicología puede actualizar el historial clínico.");
        }

        HistorialClinico historial = historialClinicoRepository.findByPacienteId(idPaciente)
                .orElseThrow(() -> new RuntimeException("Error: El paciente no tiene un historial clínico registrado."));

        historial.setAntecedentesMedicos(datosNuevos.getAntecedentesMedicos());
        historial.setAntecedentesFamiliares(datosNuevos.getAntecedentesFamiliares());

        return historialClinicoRepository.save(historial);
    }

    /**
     * Recupera el contenedor base del expediente.
     */
    public HistorialClinicoDTO obtenerPorPaciente(Integer idPaciente) {
        //  Identificamos quién hace la petición
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        Usuario usuarioLogueado = usuarioRepository.findByCorreo(auth.getName())
                .orElseThrow(() -> new RuntimeException("Error: Usuario no encontrado"));

        //  Buscamos el historial
        HistorialClinico historial = historialClinicoRepository.findByPacienteId(idPaciente)
                .orElseThrow(() -> new RuntimeException("Error: El paciente no tiene un historial clínico registrado."));

        // Si es psicólogo, verificar que el paciente le pertenezca
        if (usuarioLogueado.getRol().equalsIgnoreCase("psicologo")) {
            Paciente paciente = historial.getPaciente();

            if (paciente.getPsicologo() == null || !paciente.getPsicologo().getId().equals(usuarioLogueado.getId())) {
                throw new RuntimeException("Error de Privacidad (HIPAA): No tienes permisos para visualizar el expediente clínico de un paciente que no te ha sido asignado.");
            }
        }

        return convertirADto(historial);
    }

    private HistorialClinicoDTO convertirADto(HistorialClinico historial) {
        HistorialClinicoDTO dto = new HistorialClinicoDTO();
        dto.setIdHistorial(historial.getId());
        dto.setAntecedentesFamiliares(historial.getAntecedentesFamiliares());
        dto.setAntecedentesMedicos(historial.getAntecedentesMedicos());
        dto.setFechaCreacion(historial.getFechaCreacion());

        if (historial.getPaciente() != null && historial.getPaciente().getUsuario() != null) {
            dto.setIdPaciente(historial.getPaciente().getId());
            dto.setNombrePaciente(historial.getPaciente().getUsuario().getNombre());
        }

        return dto;
    }
}