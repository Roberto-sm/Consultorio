package com.upsin.demo.services;

import com.upsin.demo.models.HistorialClinico;
import com.upsin.demo.models.Usuario;
import com.upsin.demo.repositories.HistorialClinicoRepository;
import com.upsin.demo.repositories.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Service
public class HistorialClinicoService {

    @Autowired
    private HistorialClinicoRepository historialClinicoRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    public HistorialClinico actualizarAntecedentes(Integer idPaciente, HistorialClinico datosNuevos) {

        // 1. Validamos quién está intentando hacer el cambio
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        Usuario usuarioLogueado = usuarioRepository.findByCorreo(auth.getName())
                .orElseThrow(() -> new RuntimeException("Error: Usuario no encontrado"));

        if (!usuarioLogueado.getRol().equalsIgnoreCase("psicologo")) {
            throw new RuntimeException("Error de seguridad: Solo el personal de psicología puede actualizar el historial clínico.");
        }

        // 2. Buscamos la "carpeta" del paciente usando el método que creamos en el repositorio
        HistorialClinico historial = historialClinicoRepository.findByPacienteId(idPaciente)
                .orElseThrow(() -> new RuntimeException("Error: El paciente no tiene un historial clínico registrado."));

        // 3. Actualizamos únicamente los campos médicos (protegiendo el id y la fecha de creación)
        historial.setAntecedentesMedicos(datosNuevos.getAntecedentesMedicos());
        historial.setAntecedentesFamiliares(datosNuevos.getAntecedentesFamiliares());

        // 4. Guardamos los cambios
        return historialClinicoRepository.save(historial);
    }

    // Método para ver la "carpeta" base del paciente
    public HistorialClinico obtenerPorPaciente(Integer idPaciente) {
        return historialClinicoRepository.findByPacienteId(idPaciente)
                .orElseThrow(() -> new RuntimeException("Error: El paciente no tiene un historial clínico registrado."));
    }
}