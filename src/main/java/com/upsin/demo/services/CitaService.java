package com.upsin.demo.services;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

import com.upsin.demo.dto.CitaDTO;
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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.util.List;
import java.util.Arrays;

/**
 * Servicio Orquestador de Citas Médicas.
 * Controla el ciclo de vida de las sesiones (agendamiento, transiciones de estado,
 * penalizaciones y validaciones de tiempo/empalme) asegurando la integridad del negocio.
 */
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

    /**
     * Agenda la cita inicial (Triaje).
     * Aplica reglas de horario de la clínica, validación de deudas, y asigna
     * automáticamente al psicólogo de planta evitando empalmes.
     */
    public CitaDTO agendarPrimeraCita(Cita nuevaCita) {

        validarReglasDeHorario(nuevaCita.getFechaHora());
        Usuario usuarioLogueado = obtenerUsuarioAutenticado();

        Integer idPacienteReal = usuarioLogueado.getId();
        Paciente pacienteReal = pacienteRepository.findById(idPacienteReal)
                .orElseThrow(() -> new RuntimeException("Error: Paciente no encontrado en la base de datos"));

        nuevaCita.setPaciente(pacienteReal);

        if (pacienteReal.getPenalizacionActiva() != null && pacienteReal.getPenalizacionActiva()) {
            throw new RuntimeException("Error: Tienes una penalización pendiente del 50% por cancelación tardía. Debes liquidar tu adeudo antes de agendar una nueva cita.");
        }

        if (citaRepository.existsByPacienteIdAndEstado(idPacienteReal, "pendiente")) {
            throw new RuntimeException("Error: El paciente ya tiene una cita pendiente activa.");
        }

        Psicologo psicologoDePlanta = psicologoRepository.findFirstByEsDePlantaTrue()
                .orElseThrow(() -> new RuntimeException("Error: No hay psicólogo de planta disponible"));

        if (citaRepository.existsByPsicologoIdAndFechaHoraAndEstado(psicologoDePlanta.getId(), nuevaCita.getFechaHora(), "pendiente") ||
                citaRepository.existsByPsicologoIdAndFechaHoraAndEstado(psicologoDePlanta.getId(), nuevaCita.getFechaHora(), "confirmada")) {
            throw new RuntimeException("Error: El horario seleccionado ya está ocupado. Por favor, elige otra hora.");
        }

        nuevaCita.setPsicologo(psicologoDePlanta);
        nuevaCita.setEstado("pendiente");
        nuevaCita.setEsPrimera(true);

        return convertirACitaDTO(citaRepository.save(nuevaCita));
    }

    /**
     * Agenda citas subsecuentes.
     * Enruta automáticamente al paciente con el especialista que tiene asignado.
     */
    public CitaDTO agendarCitaSeguimiento(Cita nuevaCita) {

        validarReglasDeHorario(nuevaCita.getFechaHora());
        Usuario usuarioLogueado = obtenerUsuarioAutenticado();

        Integer idPacienteReal = usuarioLogueado.getId();
        Paciente pacienteReal = pacienteRepository.findById(idPacienteReal)
                .orElseThrow(() -> new RuntimeException("Error: Paciente no encontrado en la base de datos."));

        nuevaCita.setPaciente(pacienteReal);

        if (pacienteReal.getPenalizacionActiva() != null && pacienteReal.getPenalizacionActiva()) {
            throw new RuntimeException("Error: Tienes una penalización pendiente del 50% por cancelación tardía. Debes liquidar tu adeudo antes de agendar una nueva cita.");
        }

        if (pacienteReal.getPsicologo() == null) {
            throw new RuntimeException("Error: El paciente aún no tiene un psicólogo asignado. Debe agendar una primera cita de triaje.");
        }

        if (citaRepository.existsByPacienteIdAndEstado(idPacienteReal, "pendiente")) {
            throw new RuntimeException("Error: El paciente ya tiene una cita pendiente activa.");
        }

        Psicologo especialista = pacienteReal.getPsicologo();

        if (citaRepository.existsByPsicologoIdAndFechaHoraAndEstado(especialista.getId(), nuevaCita.getFechaHora(), "pendiente") ||
                citaRepository.existsByPsicologoIdAndFechaHoraAndEstado(especialista.getId(), nuevaCita.getFechaHora(), "confirmada")) {
            throw new RuntimeException("Error: El horario seleccionado ya está ocupado. Por favor, elige otra hora.");
        }

        nuevaCita.setPsicologo(especialista);
        nuevaCita.setEstado("pendiente");
        nuevaCita.setEsPrimera(false);

        return convertirACitaDTO(citaRepository.save(nuevaCita));
    }

    /**
     * Transición a 'cancelada'.
     * Aplica la regla financiera: Si un paciente cancela con menos de 20 hrs de anticipación,
     * levanta un flag de penalización en el paciente y un rastro de auditoría en la cita.
     */
    public CitaDTO cancelarCita(Integer idCita) {
        Usuario usuarioLogueado = obtenerUsuarioAutenticado();
        Cita cita = citaRepository.findById(idCita)
                .orElseThrow(() -> new RuntimeException("Error: Cita no encontrada"));
        String estadoActual = cita.getEstado();

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

                    // Auditoría Financiera
                    cita.setMultaAplicada(true);
                }
            }
        } else if (usuarioLogueado.getRol().equalsIgnoreCase("psicologo")) {
            if (!cita.getPsicologo().getId().equals(usuarioLogueado.getId())) throw new RuntimeException("Error de seguridad.");
            if (estadoActual.equals("pendiente")) throw new RuntimeException("Utiliza el botón 'Rechazar' para solicitudes pendientes.");

            if (estadoActual.equals("confirmada")) {
                validarPeriodoDeGracia(cita);
            }
        }

        cita.setEstado("cancelada");
        return convertirACitaDTO(citaRepository.save(cita));
    }

    public CitaDTO aprobarCita(Integer idCita) {
        Usuario usuarioLogueado = obtenerUsuarioAutenticado();
        Cita cita = citaRepository.findById(idCita)
                .orElseThrow(() -> new RuntimeException("Error: Cita no encontrada"));

        String estadoActual = cita.getEstado();

        if (!cita.getPsicologo().getId().equals(usuarioLogueado.getId())) {
            throw new RuntimeException("Error de seguridad: Esta cita pertenece a otro psicólogo.");
        }

        if (!estadoActual.equals("pendiente") && !estadoActual.equals("rechazada") && !estadoActual.equals("cancelada")) {
            throw new RuntimeException("Error: No puedes confirmar una cita que está en estado '" + estadoActual + "'.");
        }

        if (LocalDateTime.now().isAfter(cita.getFechaHora())) {
            throw new RuntimeException("Error: La fecha de esta cita ya pasó. No puede ser confirmada.");
        }

        if (estadoActual.equals("rechazada") || estadoActual.equals("cancelada")) {
            validarPeriodoDeGracia(cita);
        }

        cita.setEstado("confirmada");
        return convertirACitaDTO(citaRepository.save(cita));
    }

    public CitaDTO rechazarCita(Integer idCita) {
        Usuario usuarioLogueado = obtenerUsuarioAutenticado();
        Cita cita = citaRepository.findById(idCita)
                .orElseThrow(() -> new RuntimeException("Error: Cita no encontrada"));

        if (!cita.getPsicologo().getId().equals(usuarioLogueado.getId())) {
            throw new RuntimeException("Error de seguridad: No puedes rechazar una cita asignada a otro psicólogo.");
        }

        String estadoActual = cita.getEstado();

        if (!estadoActual.equals("pendiente") && !estadoActual.equals("confirmada")) {
            throw new RuntimeException("Error: No puedes rechazar una cita en estado '" + estadoActual + "'.");
        }

        if (LocalDateTime.now().isAfter(cita.getFechaHora())) {
            throw new RuntimeException("Error: La hora de la cita ya pasó. Debe marcarse como finalizada o no-show.");
        }

        if (estadoActual.equals("confirmada")) {
            validarPeriodoDeGracia(cita);
        }

        cita.setEstado("rechazada");
        return convertirACitaDTO(citaRepository.save(cita));
    }

    public CitaDTO finalizarCita(Integer idCita) {
        Usuario usuarioLogueado = obtenerUsuarioAutenticado();
        Cita cita = citaRepository.findById(idCita)
                .orElseThrow(() -> new RuntimeException("Error: Cita no encontrada"));
        String estadoActual = cita.getEstado();

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
        return convertirACitaDTO(citaRepository.save(cita));
    }

    public CitaDTO registrarNoShow(Integer idCita) {
        Usuario usuarioLogueado = obtenerUsuarioAutenticado();
        Cita cita = citaRepository.findById(idCita)
                .orElseThrow(() -> new RuntimeException("Error: Cita no encontrada"));

        if (!cita.getPsicologo().getId().equals(usuarioLogueado.getId())) {
            throw new RuntimeException("Error de seguridad: No puedes modificar la cita de otro psicólogo.");
        }

        String estadoActual = cita.getEstado();

        if (!estadoActual.equals("confirmada") && !estadoActual.equals("finalizada")) {
            throw new RuntimeException("Error: No puedes marcar como No-Show una cita en estado '" + estadoActual + "'.");
        }

        validarSesionTerminada(cita);

        if (estadoActual.equals("finalizada")) {
            validarPeriodoDeGracia(cita);
        }

        cita.setEstado("no_asistio");
        return convertirACitaDTO(citaRepository.save(cita));
    }

    private Usuario obtenerUsuarioAutenticado() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return usuarioRepository.findByCorreo(auth.getName())
                .orElseThrow(() -> new RuntimeException("Error de seguridad: Usuario no encontrado"));
    }

    /**
     * Validador de límites temporales.
     * Restringe las reservaciones a días hábiles, previene agendamientos en la hora de comida
     * y fuerza el encuadre a bloques de 50 minutos iniciando en punto.
     */
    private void validarReglasDeHorario(LocalDateTime fechaHora) {

        if (!fechaHora.toLocalDate().isAfter(LocalDate.now())) {
            throw new RuntimeException("Error: Debes reservar con al menos 1 día de anticipación.");
        }

        if (fechaHora.getMinute() != 0) {
            throw new RuntimeException("Error: Las citas inician a la hora en punto (ej. 10:00, 11:00).");
        }

        DayOfWeek dia = fechaHora.getDayOfWeek();
        int hora = fechaHora.getHour();

        if (dia == DayOfWeek.SUNDAY) {
            throw new RuntimeException("Error: No laboramos los domingos.");
        }

        if (dia == DayOfWeek.SATURDAY) {
            if (hora < 9 || hora > 13) {
                throw new RuntimeException("Error: Horario sabatino es de 09:00 a 14:00 (Última cita a las 13:00).");
            }
        } else {
            if (hora < 9 || hora > 18) {
                throw new RuntimeException("Error: Horario L-V es de 09:00 a 19:00 (Última cita a las 18:00).");
            }
            if (hora == 14) {
                throw new RuntimeException("Error: El horario de 14:00 a 15:00 está inhabilitado por hora de comida.");
            }
        }
    }

    /**
     * Cron Job Diario.
     * Ejecuta una barrida nocturna automatizada para descartar solicitudes de cita
     * pendientes que ya caducaron cronológicamente.
     */
    @Scheduled(cron = "0 0 1 * * *")
    public void limpiarCitasExpiradas() {
        System.out.println("Iniciando tarea programada: Limpieza de citas expiradas...");

        List<Cita> todasLasCitas = citaRepository.findAll();

        for (Cita cita : todasLasCitas) {
            if (cita.getEstado().equals("pendiente") && LocalDateTime.now().isAfter(cita.getFechaHora())) {
                cita.setEstado("rechazada");
                citaRepository.save(cita);
                System.out.println("Cita ID " + cita.getId() + " marcada como rechazada por expiración de tiempo.");
            }
        }
    }

    /**
     * Barrera de protección para la máquina de estados.
     * Evita que un psicólogo modifique registros históricos de días pasados.
     */
    private void validarPeriodoDeGracia(Cita cita) {
        if (cita.getFechaModificacion() != null) {
            long horasTranscurridas = ChronoUnit.HOURS.between(cita.getFechaModificacion(), LocalDateTime.now());
            if (horasTranscurridas > 24) {
                throw new RuntimeException("Error: El periodo de gracia de 24 horas para deshacer este cambio de estado ha expirado.");
            }
        }
    }

    /**
     * Valida el encuadre clínico.
     * Impide que el psicólogo marque una cita como terminada antes de transcurridos los 50 minutos reglamentarios.
     */
    private void validarSesionTerminada(Cita cita) {
        LocalDateTime finCita = cita.getFechaHora().plusMinutes(50);
        if (LocalDateTime.now().isBefore(finCita)) {
            throw new RuntimeException("Error: La sesión aún no termina. Debes esperar a que pase su horario (" + finCita.toLocalTime() + ").");
        }
    }

    public Page<CitaDTO> obtenerMisCitasPaginadas(Pageable pageable) {
        // 1. Descubrimos quién es a través de su Token
        Usuario usuarioLogueado = obtenerUsuarioAutenticado();

        // 2. Por seguridad, verificamos su rol
        if (!usuarioLogueado.getRol().equalsIgnoreCase("psicologo")) {
            throw new RuntimeException("Error de seguridad: Solo los psicólogos pueden acceder a su agenda personalizada.");
        }

        Page<Cita> paginaCitasPesadas = citaRepository.findByPsicologoIdOrderByFechaHoraDesc(usuarioLogueado.getId(), pageable);

        // 3. Vamos a la base de datos solo por SUS citas
        return paginaCitasPesadas.map(this::convertirACitaDTO);
    }

    public Page<CitaDTO> obtenerMisCitasActivasPaginadas(Pageable pageable) {
        Usuario usuarioLogueado = obtenerUsuarioAutenticado();

        if (!usuarioLogueado.getRol().equalsIgnoreCase("psicologo")) {
            throw new RuntimeException("Error de seguridad: Solo los psicólogos pueden ver su agenda activa.");
        }

        // Definimos cuáles son los estados que el doctor necesita ver para trabajar
        List<String> estadosActivos = Arrays.asList("pendiente", "confirmada");

        Page<Cita> paginaCitasPesadas = citaRepository.findByPsicologoIdAndEstadoInOrderByFechaHoraAsc(
                usuarioLogueado.getId(), estadosActivos, pageable);

        // Ordenadas de la cita más próxima a la más lejana
        return paginaCitasPesadas.map(this::convertirACitaDTO);
    }

    // Helper Method
    private CitaDTO convertirACitaDTO(Cita cita) {
        CitaDTO dto = new CitaDTO();
        dto.setIdCita(cita.getId());
        dto.setFechaHora(cita.getFechaHora());
        dto.setEstado(cita.getEstado());
        dto.setEsPrimera(cita.getEsPrimera());

        // Extraemos los nombres de forma segura navegando por las relaciones
        if(cita.getPaciente() != null && cita.getPaciente().getUsuario() != null){
            dto.setNombrePaciente(cita.getPaciente().getUsuario().getNombre());
        }

        if(cita.getPsicologo() != null && cita.getPsicologo().getUsuario() != null){
            dto.setNombrePsicologo(cita.getPsicologo().getUsuario().getNombre());
        }

        return dto;
    }
}