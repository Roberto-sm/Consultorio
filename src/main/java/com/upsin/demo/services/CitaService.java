package com.upsin.demo.services;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import com.upsin.demo.models.Cita;
import com.upsin.demo.models.Paciente;
import com.upsin.demo.models.Psicologo;
import com.upsin.demo.models.Usuario;
import com.upsin.demo.repositories.CitaRepository;
import com.upsin.demo.repositories.PacienteRepository;
import com.upsin.demo.repositories.PsicologoRepository;
import com.upsin.demo.repositories.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;


@Service
public class CitaService {

    @Autowired
    private CitaRepository citaRepository;

    @Autowired
    private PsicologoRepository psicologoRepository;

    @Autowired
    private PacienteRepository pacienteRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    public Cita agendarPrimeraCita(Cita nuevaCita) {

        validarReglasDeHorario(nuevaCita.getFechaHora());
        Usuario usuarioLogueado = obtenerUsuarioAutenticado();

        //  Obtenemos el ID real seguro
        Integer idPacienteReal = usuarioLogueado.getId();
        Paciente pacienteReal = pacienteRepository.findById(idPacienteReal)
                .orElseThrow(() -> new RuntimeException("Error: Paciente no encontrado en la base de datos"));

        // Forzamos la identidad en la cita
        nuevaCita.setPaciente(pacienteReal);

        if (pacienteReal.getPenalizacionActiva() != null && pacienteReal.getPenalizacionActiva()) {
            throw new RuntimeException("Error: Tienes una penalización pendiente del 50% por cancelación tardía. Debes liquidar tu adeudo antes de agendar una nueva cita.");
        }

        // Un paciente solo puede tener UNA cita activa
        if (citaRepository.existsByPacienteIdAndEstado(idPacienteReal, "pendiente")) {
            throw new RuntimeException("Error: El paciente ya tiene una cita pendiente activa.");
        }

        // Buscamos al psicólogo de planta
        Psicologo psicologoDePlanta = psicologoRepository.findFirstByEsDePlantaTrue()
                .orElseThrow(() -> new RuntimeException("Error: No hay psicólogo de planta disponible"));

        // Evitar empalmes
        if (citaRepository.existsByPsicologoIdAndFechaHoraAndEstado(
                psicologoDePlanta.getId(), nuevaCita.getFechaHora(), "pendiente")) {
            throw new RuntimeException("Error: El horario seleccionado ya está ocupado. Por favor, elige otra hora.");
        }

        if (citaRepository.existsByPsicologoIdAndFechaHoraAndEstado(
                psicologoDePlanta.getId(), nuevaCita.getFechaHora(), "confirmada")) {
            throw new RuntimeException("Error: El horario seleccionado ya está ocupado. Por favor, elige otra hora.");
        }

        nuevaCita.setPsicologo(psicologoDePlanta);
        nuevaCita.setEstado("pendiente");
        nuevaCita.setEsPrimera(true);

        return citaRepository.save(nuevaCita);
    }

    public Cita agendarCitaSeguimiento(Cita nuevaCita) {

        validarReglasDeHorario(nuevaCita.getFechaHora());
        Usuario usuarioLogueado = obtenerUsuarioAutenticado();

        // Obtenemos el ID real seguro
        Integer idPacienteReal = usuarioLogueado.getId();
        Paciente pacienteReal = pacienteRepository.findById(idPacienteReal)
                .orElseThrow(() -> new RuntimeException("Error: Paciente no encontrado en la base de datos."));

        // Forzamos la identidad en la cita
        nuevaCita.setPaciente(pacienteReal);

        if (pacienteReal.getPenalizacionActiva() != null && pacienteReal.getPenalizacionActiva()) {
            throw new RuntimeException("Error: Tienes una penalización pendiente del 50% por cancelación tardía. Debes liquidar tu adeudo antes de agendar una nueva cita.");
        }

        // Validamos si el paciente ya fue derivado a un especialista
        if (pacienteReal.getPsicologo() == null) {
            throw new RuntimeException("Error: El paciente aún no tiene un psicólogo asignado. Debe agendar una primera cita de triaje.");
        }

        // Regla de Concurrencia usando idPacienteReal
        if (citaRepository.existsByPacienteIdAndEstado(idPacienteReal, "pendiente")) {
            throw new RuntimeException("Error: El paciente ya tiene una cita pendiente activa.");
        }

        // Extraemos a su especialista asignado directamente de nuestro pacienteReal
        Psicologo especialista = pacienteReal.getPsicologo();

        // Regla de Empalme
        if (citaRepository.existsByPsicologoIdAndFechaHoraAndEstado(
                especialista.getId(), nuevaCita.getFechaHora(), "pendiente")) {
            throw new RuntimeException("Error: El horario seleccionado ya está ocupado. Por favor, elige otra hora.");
        }


        if (citaRepository.existsByPsicologoIdAndFechaHoraAndEstado(
                especialista.getId(), nuevaCita.getFechaHora(), "confirmada")) {
            throw new RuntimeException("Error: El horario seleccionado ya está ocupado. Por favor, elige otra hora.");
        }

        nuevaCita.setPsicologo(especialista);
        nuevaCita.setEstado("pendiente");
        nuevaCita.setEsPrimera(false);

        return citaRepository.save(nuevaCita);
    }

    public Cita cancelarCita(Integer idCita) {
        Usuario usuarioLogueado = obtenerUsuarioAutenticado();
        Cita cita = citaRepository.findById(idCita)
                .orElseThrow(() -> new RuntimeException("Error: Cita no encontrada"));

        // Validamos que la cita sea cancelable
        String estadoActual = cita.getEstado();
        if (estadoActual.equals("cancelada") || estadoActual.equals("finalizada")
                || estadoActual.equals("rechazada") || estadoActual.equals("no_asistio")) {
            throw new RuntimeException("Error: La cita ya no puede ser cancelada (Estado actual: " + estadoActual + ").");        }

        // 4. LÓGICA DE PENALIZACIÓN (Solo aplica si es el paciente quien cancela)
        if (usuarioLogueado.getRol().equalsIgnoreCase("paciente")) {

            // Verificamos por seguridad que el paciente esté cancelando SU propia cita
            if (!cita.getPaciente().getId().equals(usuarioLogueado.getId())) {
                throw new RuntimeException("Error: cita no encontrada");
            }
            if (estadoActual.equals("confirmada")) {
                long horasFaltantes = ChronoUnit.HOURS.between(LocalDateTime.now(), cita.getFechaHora());

                if (horasFaltantes < 20) {
                // Multa aplicada
                Paciente paciente = cita.getPaciente();
                paciente.setPenalizacionActiva(true);
                pacienteRepository.save(paciente);
            }
        }
            cita.setEstado("cancelada");}


            else if (usuarioLogueado.getRol().equalsIgnoreCase("psicologo")) {

            // Verificamos que el psicólogo no cancele la cita de otro doctor
            if (!cita.getPsicologo().getId().equals(usuarioLogueado.getId())) {
                throw new RuntimeException("Error: cita no encontrada");
            }
            if (estadoActual.equals("pendiente")) {
                throw new RuntimeException("Error: Para solicitudes pendientes, utiliza el botón 'Rechazar'.");
            }
            cita.setEstado("cancelada");
        }

        return citaRepository.save(cita);
    }

    public Cita aprobarCita(Integer idCita) {

        Usuario usuarioLogueado = obtenerUsuarioAutenticado();

        // Buscamos la cita por su ID
        Cita cita = citaRepository.findById(idCita)
                .orElseThrow(() -> new RuntimeException("Error: Cita no encontrada"));


        // Solo se pueden aprobar citas que estén esperando aprobación
        if (!cita.getEstado().equals("pendiente")) {
            throw new RuntimeException("Error: Esta cita no está pendiente de aprobación (Estado actual: " + cita.getEstado() + ")");
        }

        if (!cita.getPsicologo().getId().equals(usuarioLogueado.getId())) {
            throw new RuntimeException("Error de seguridad: Esta cita pertenece a otro psicólogo.");
        }

        // Cambiamos el estado oficialmente
        cita.setEstado("confirmada");
        // Guardamos los cambios en MySQL
        return citaRepository.save(cita);
    }

    public Cita rechazarCita(Integer idCita) {
        Usuario usuarioLogueado = obtenerUsuarioAutenticado();
        Cita cita = citaRepository.findById(idCita)
                .orElseThrow(() -> new RuntimeException("Error: Cita no encontrada"));

        // Solo se puede rechazar si estaba esperando aprobación
        if (!cita.getEstado().equals("pendiente")) {
            throw new RuntimeException("Error: Solo puedes rechazar citas que estén pendientes de aprobación.");
        }

        if (!cita.getPsicologo().getId().equals(usuarioLogueado.getId())) {
            throw new RuntimeException("Error de seguridad: No puedes rechazar una cita asignada a otro psicólogo.");
        }

        cita.setEstado("rechazada");
        return citaRepository.save(cita);
    }

    public Cita finalizarCita(Integer idCita) {
        Usuario usuarioLogueado = obtenerUsuarioAutenticado();
        Cita cita = citaRepository.findById(idCita)
                .orElseThrow(() -> new RuntimeException("Error: Cita no encontrada"));

        if (!cita.getEstado().equals("confirmada")) {
            throw new RuntimeException("Error: Solo puedes finalizar citas que estén en estado 'confirmada'.");
        }

        if (!cita.getPsicologo().getId().equals(usuarioLogueado.getId())) {
            throw new RuntimeException("Error de seguridad: No puedes finalizar la cita de otro psicólogo.");
        }

        cita.setEstado("finalizada");
        return citaRepository.save(cita);
    }

    public Cita registrarNoShow(Integer idCita) {
        Usuario usuarioLogueado = obtenerUsuarioAutenticado();
        Cita cita = citaRepository.findById(idCita)
                .orElseThrow(() -> new RuntimeException("Error: Cita no encontrada"));

        if (!cita.getEstado().equals("confirmada")) {
            throw new RuntimeException("Error: Solo puedes marcar como No-Show citas que estén 'confirmada'.");
        }

        if (!cita.getPsicologo().getId().equals(usuarioLogueado.getId())) {
            throw new RuntimeException("Error de seguridad: No puedes modificar la cita de otro psicólogo.");
        }

        cita.setEstado("no_asistio");
        return citaRepository.save(cita);
    }

    private Usuario obtenerUsuarioAutenticado() {
        //  Extraemos el correo directamente del Token y buscamos al usuario dueño de ese token
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return usuarioRepository.findByCorreo(auth.getName())
                .orElseThrow(() -> new RuntimeException("Error de seguridad: Usuario no encontrado"));
    }

    private void validarReglasDeHorario(LocalDateTime fechaHora) {

        // Anticipación mínima (Al menos el día de mañana)
        if (!fechaHora.toLocalDate().isAfter(LocalDate.now())) {
            throw new RuntimeException("Error: Debes reservar con al menos 1 día de anticipación.");
        }

        // Duración fija y en punto (--:00:00)
        // Para asegurar que duren 50 mins
        if (fechaHora.getMinute() != 0) {
            throw new RuntimeException("Error: Las citas inician a la hora en punto (ej. 10:00, 11:00).");
        }

        DayOfWeek dia = fechaHora.getDayOfWeek();
        int hora = fechaHora.getHour();

        // Horario laboral
        if (dia == DayOfWeek.SUNDAY) {
            throw new RuntimeException("Error: No laboramos los domingos.");
        }

        if (dia == DayOfWeek.SATURDAY) {
            // Sábado: 09:00 a 14:00 (última cita a las 13:00)
            if (hora < 9 || hora > 13) {
                throw new RuntimeException("Error: Horario sabatino es de 09:00 a 14:00 (Última cita a las 13:00).");
            }
        } else {
            // Lunes a Viernes: 09:00 a 19:00 (última cita a las 18:00)
            if (hora < 9 || hora > 18) {
                throw new RuntimeException("Error: Horario L-V es de 09:00 a 19:00 (Última cita a las 18:00).");
            }
            // REGLA 1: Hora de comida
            if (hora == 14) {
                throw new RuntimeException("Error: El horario de 14:00 a 15:00 está inhabilitado por hora de comida.");
            }
        }
    }
}