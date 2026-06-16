package com.upsin.demo.services;

import com.upsin.demo.models.Cita;
import com.upsin.demo.models.HistorialClinico;
import com.upsin.demo.models.NotaEvolucion;
import com.upsin.demo.models.Usuario;
import com.upsin.demo.repositories.CitaRepository;
import com.upsin.demo.repositories.HistorialClinicoRepository;
import com.upsin.demo.repositories.NotaEvolucionRepository;
import com.upsin.demo.repositories.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class NotaEvolucionService {

    @Autowired
    private NotaEvolucionRepository notaEvolucionRepository;

    @Autowired
    private CitaRepository citaRepository;

    @Autowired
    private HistorialClinicoRepository historialClinicoRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    public NotaEvolucion crearNota(Integer idCita, NotaEvolucion nuevaNota) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        Usuario usuarioLogueado = usuarioRepository.findByCorreo(auth.getName())
                .orElseThrow(() -> new RuntimeException("Error: Usuario no encontrado"));

        if (!usuarioLogueado.getRol().equalsIgnoreCase("psicologo")) {
            throw new RuntimeException("Error de seguridad: Solo los psicólogos pueden redactar notas.");
        }

        //  Validamos la Cita
        Cita cita = citaRepository.findById(idCita)
                .orElseThrow(() -> new RuntimeException("Error: Cita no encontrada."));

        if (!cita.getPsicologo().getId().equals(usuarioLogueado.getId())) {
            throw new RuntimeException("Error de seguridad: No puedes escribir notas en citas de otros psicólogos.");
        }

        if (!cita.getEstado().equals("finalizada")) {
            throw new RuntimeException("Error: Solo puedes agregar notas de evolución a citas que hayan sido marcadas como 'finalizada'.");
        }

        if (notaEvolucionRepository.existsByCitaId(idCita)) {
            throw new RuntimeException("Error: Ya existe una nota de evolución redactada para esta cita.");
        }

        //  Vinculamos la nota con la "Carpeta" del paciente
        HistorialClinico historial = historialClinicoRepository.findByPacienteId(cita.getPaciente().getId())
                .orElseThrow(() -> new RuntimeException("Error crítico: El paciente no tiene un historial base creado."));

        nuevaNota.setHistorialClinico(historial);
        nuevaNota.setCita(cita);

        return notaEvolucionRepository.save(nuevaNota);
    }

    // Método para consultar todas las notas de un paciente
    public List<NotaEvolucion> consultarNotasDePaciente(Integer idPaciente) {
        HistorialClinico historial = historialClinicoRepository.findByPacienteId(idPaciente)
                .orElseThrow(() -> new RuntimeException("Error: El paciente no tiene un historial clínico."));

        return notaEvolucionRepository.findByHistorialClinicoIdOrderByFechaRegistroDesc(historial.getId());
    }
}