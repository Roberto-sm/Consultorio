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
import org.springframework.scheduling.annotation.Scheduled;
import java.util.List;


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
        String estadoActual = cita.getEstado();

        if (!cita.getPsicologo().getId().equals(usuarioLogueado.getId())) {
            throw new RuntimeException("Error: cita no encontrada");
        }

        // REGLA 2: Bloquear cancelaciones si la hora ya pasó
        if (LocalDateTime.now().isAfter(cita.getFechaHora())) {
            throw new RuntimeException("Error: La cita ya pasó, no puede ser cancelada.");
        }

        if (estadoActual.equals("cancelada") || estadoActual.equals("finalizada")
                || estadoActual.equals("rechazada") || estadoActual.equals("no_asistio")) {
            throw new RuntimeException("Error: La cita ya no puede ser cancelada.");
        }

        if (usuarioLogueado.getRol().equalsIgnoreCase("paciente")) {
            if (!cita.getPaciente().getId().equals(usuarioLogueado.getId())) throw new RuntimeException("Error de seguridad.");

            if (estadoActual.equals("confirmada")) {
                long horasFaltantes = ChronoUnit.HOURS.between(LocalDateTime.now(), cita.getFechaHora());
                if (horasFaltantes < 20) {
                    Paciente paciente = cita.getPaciente();
                    paciente.setPenalizacionActiva(true);
                    pacienteRepository.save(paciente);

                    // DEJAMOS LA HUELLA DE AUDITORÍA EN LA CITA
                    cita.setMultaAplicada(true);
                }
            }
        } else if (usuarioLogueado.getRol().equalsIgnoreCase("psicologo")) {
            if (!cita.getPsicologo().getId().equals(usuarioLogueado.getId())) throw new RuntimeException("Error de seguridad.");
            if (estadoActual.equals("pendiente")) throw new RuntimeException("Utiliza el botón 'Rechazar' para solicitudes pendientes.");

            // BARRERA DE DESHACER (24 hrs) para el psicólogo
            if (estadoActual.equals("confirmada")) {
                validarPeriodoDeGracia(cita);
            }
        }

        cita.setEstado("cancelada");
        return citaRepository.save(cita);
    }

    public Cita aprobarCita(Integer idCita) {

        Usuario usuarioLogueado = obtenerUsuarioAutenticado();
        // Buscamos la cita por su ID
        Cita cita = citaRepository.findById(idCita)
                .orElseThrow(() -> new RuntimeException("Error: Cita no encontrada"));

        String estadoActual = cita.getEstado();

        if (!cita.getPsicologo().getId().equals(usuarioLogueado.getId())) {
            throw new RuntimeException("Error de seguridad: Esta cita pertenece a otro psicólogo.");
        }

        // REGLA 1, 3 y 4: Solo se aprueba desde pendiente, o deshaciendo rechazada/cancelada
        if (!estadoActual.equals("pendiente") && !estadoActual.equals("rechazada") && !estadoActual.equals("cancelada")) {
            throw new RuntimeException("Error: No puedes confirmar una cita que está en estado '" + estadoActual + "'.");
        }

        // REGLA 1 y 3 (Cron Job): Si la hora de la cita ya pasó, es imposible confirmarla (Bloquea las rechazadas por el sistema)
        if (LocalDateTime.now().isAfter(cita.getFechaHora())) {
            throw new RuntimeException("Error: La fecha de esta cita ya pasó. No puede ser confirmada.");
        }

        // BARRERA DE DESHACER (24 hrs)
        if (estadoActual.equals("rechazada") || estadoActual.equals("cancelada")) {
            validarPeriodoDeGracia(cita);
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

        if (!cita.getPsicologo().getId().equals(usuarioLogueado.getId())) {
            throw new RuntimeException("Error de seguridad: No puedes rechazar una cita asignada a otro psicólogo.");
        }

        String estadoActual = cita.getEstado();

        // REGLA 1 y 2: Solo desde pendiente, o desde confirmada (si fue un error de dedo)
        if (!estadoActual.equals("pendiente") && !estadoActual.equals("confirmada")) {
            throw new RuntimeException("Error: No puedes rechazar una cita en estado '" + estadoActual + "'.");
        }

        // REGLA 2: No se puede rechazar si ya pasó la hora
        if (LocalDateTime.now().isAfter(cita.getFechaHora())) {
            throw new RuntimeException("Error: La hora de la cita ya pasó. Debe marcarse como finalizada o no-show.");
        }

        // BARRERA DE DESHACER (24 hrs)
        if (estadoActual.equals("confirmada")) {
            validarPeriodoDeGracia(cita);
        }

        cita.setEstado("rechazada");
        return citaRepository.save(cita);
    }

    public Cita finalizarCita(Integer idCita) {
        Usuario usuarioLogueado = obtenerUsuarioAutenticado();
        Cita cita = citaRepository.findById(idCita)
                .orElseThrow(() -> new RuntimeException("Error: Cita no encontrada"));
        String estadoActual = cita.getEstado();

        // REGLA 2 y 5: Solo desde confirmada o deshaciendo no-show
        if (!estadoActual.equals("confirmada") && !estadoActual.equals("no_asistio")) {
            throw new RuntimeException("Error: No puedes finalizar una cita en estado '" + estadoActual + "'.");
        }

        validarSesionTerminada(cita);

        if (estadoActual.equals("no_asistio")) {
            validarPeriodoDeGracia(cita);
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

        if (!cita.getPsicologo().getId().equals(usuarioLogueado.getId())) {
            throw new RuntimeException("Error de seguridad: No puedes modificar la cita de otro psicólogo.");
        }

        String estadoActual = cita.getEstado();

        // REGLA 2 y 5: Solo desde confirmada o deshaciendo finalizada
        if (!estadoActual.equals("confirmada") && !estadoActual.equals("finalizada")) {
            throw new RuntimeException("Error: No puedes marcar como No-Show una cita en estado '" + estadoActual + "'.");
        }

        validarSesionTerminada(cita);

        // BARRERA DE DESHACER (24 hrs)
        if (estadoActual.equals("finalizada")) {
            validarPeriodoDeGracia(cita);
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

    // Se ejecuta todos los días a la 1 de la mañana
    @Scheduled(cron = "0 0 1 * * *")
    public void limpiarCitasExpiradas() {
        System.out.println("Iniciando tarea programada: Limpieza de citas expiradas...");

        // Buscamos todas las citas en la base de datos
        List<Cita> todasLasCitas = citaRepository.findAll();

        for (Cita cita : todasLasCitas) {
            // Si la cita está pendiente y ya pasó su fecha/hora
            if (cita.getEstado().equals("pendiente") && LocalDateTime.now().isAfter(cita.getFechaHora())) {
                cita.setEstado("rechazada"); // O puedes crear un estado "expirada"
                citaRepository.save(cita);
                System.out.println("Cita ID " + cita.getId() + " marcada como rechazada por expiración de tiempo.");
            }
        }
    }

    private void validarPeriodoDeGracia(Cita cita) {
        if (cita.getFechaModificacion() != null) {
            long horasTranscurridas = ChronoUnit.HOURS.between(cita.getFechaModificacion(), LocalDateTime.now());
            if (horasTranscurridas > 24) {
                throw new RuntimeException("Error: El periodo de gracia de 24 horas para deshacer este cambio de estado ha expirado.");
            }
        }
    }

    private void validarSesionTerminada(Cita cita) {
        LocalDateTime finCita = cita.getFechaHora().plusMinutes(50);
        if (LocalDateTime.now().isBefore(finCita)) {
            throw new RuntimeException("Error: La sesión aún no termina. Debes esperar a que pase su horario (" + finCita.toLocalTime() + ").");
        }
    }
}