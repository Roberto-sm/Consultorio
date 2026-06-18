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

/**
 * Capa de negocio para las Notas de Evolución.
 * Conecta las observaciones del psicólogo con el historial del paciente y asegura
 * que se respeten las jerarquías clínicas y la máquina de estados de las citas.
 */
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

    /**
     * Procesa y guarda una nueva hoja de evolución.
     * Aplica la regla de negocio crítica: Una nota solo puede redactarse por el psicólogo
     * tratante, y únicamente si la cita origen se encuentra en estado 'finalizada'.
     */
    public NotaEvolucion crearNota(Integer idCita, NotaEvolucion nuevaNota) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        Usuario usuarioLogueado = usuarioRepository.findByCorreo(auth.getName())
                .orElseThrow(() -> new RuntimeException("Error: Usuario no encontrado"));

        if (!usuarioLogueado.getRol().equalsIgnoreCase("psicologo")) {
            throw new RuntimeException("Error de seguridad: Solo los psicólogos pueden redactar notas.");
        }

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

        HistorialClinico historial = historialClinicoRepository.findByPacienteId(cita.getPaciente().getId())
                .orElseThrow(() -> new RuntimeException("Error crítico: El paciente no tiene un historial base creado."));

        nuevaNota.setHistorialClinico(historial);
        nuevaNota.setCita(cita);

        return notaEvolucionRepository.save(nuevaNota);
    }

    public List<NotaEvolucion> consultarNotasDePaciente(Integer idPaciente) {
        HistorialClinico historial = historialClinicoRepository.findByPacienteId(idPaciente)
                .orElseThrow(() -> new RuntimeException("Error: El paciente no tiene un historial clínico."));

        return notaEvolucionRepository.findByHistorialClinicoIdOrderByFechaRegistroDesc(historial.getId());
    }
}