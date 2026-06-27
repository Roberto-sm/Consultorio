package com.upsin.demo.config;

import com.upsin.demo.models.*;
import com.upsin.demo.repositories.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashSet;
import java.util.List;

@Component
public class DatabaseSeeder implements CommandLineRunner {

    @Autowired private UsuarioRepository usuarioRepository;
    @Autowired private PsicologoRepository psicologoRepository;
    @Autowired private PacienteRepository pacienteRepository;
    @Autowired private EspecialidadRepository especialidadRepository;
    @Autowired private HistorialClinicoRepository historialClinicoRepository;
    @Autowired private CitaRepository citaRepository;
    @Autowired private NotaEvolucionRepository notaEvolucionRepository;
    @Autowired private PasswordEncoder passwordEncoder;

    @Autowired private JdbcTemplate jdbcTemplate;

    @Override
    public void run(String... args) throws Exception {
        if (especialidadRepository.count() == 0 && usuarioRepository.count() == 0) {
            System.out.println("[SEEDER] Iniciando inyección del Kit de Demostración Completo...");

            sembrarEspecialidades();
            sembrarPsicologos();
            sembrarPacientesEHistoriales();
            sembrarCitasYNotas();
            sembrarAuditorias();

            System.out.println("[SEEDER] Base de datos inyectada con todos los casos de prueba (CRUD, Auditorías y Estados).");
        }
    }

    private void sembrarEspecialidades() {
        String[][] datos = {
                {"Psicología Clínica", "Evaluación de trastornos emocionales."},
                {"Psicología Educativa", "Orientación académica y desarrollo cognitivo."}
        };
        for (String[] esp : datos) {
            Especialidad e = new Especialidad();
            e.setNombre(esp[0]);
            e.setDescripcion(esp[1]);
            especialidadRepository.save(e);
        }
    }

    private void sembrarPsicologos() {
        // 1. Dr. Tenma (De Planta)
        Usuario u1 = crearUsuarioBase("Dr. Tenma Kenzo", "tenma@email.com", "psicologo", "Masculino");
        Psicologo p1 = crearPsicologoBase(u1, 15, "CED-112233", true, "Especialista clínico.", 1);

        // 2. Dr. Stone
        Usuario u2 = crearUsuarioBase("Dr. Stone", "senku@email.com", "psicologo", "Masculino");
        crearPsicologoBase(u2, 7, "CED-1000000", false, "Especialista educativo.", 2);
    }

    private void sembrarPacientesEHistoriales() {
        List<Psicologo> ps = psicologoRepository.findAll();
        Psicologo tenma = ps.get(0);
        Psicologo stone = ps.get(1);

        // 1. Reigen (Asignado a Tenma)
        Usuario uReigen = crearUsuarioBase("Reigen Arataka", "reigen@email.com", "paciente", "Masculino");
        Paciente pac1 = crearPacienteBase(uReigen, tenma, false);
        crearHistorialBase(pac1, "Padre con adicciones.", "Ansiedad social.");

        // 2. Kim (Asignada a Tenma, con penalización activa)
        Usuario ukim = crearUsuarioBase("Kim Wexler", "kim@email.com", "paciente", "Femenino");
        Paciente pac2 = crearPacienteBase(ukim, tenma, true);
        crearHistorialBase(pac2, "Ninguno.", "Estrés crónico.");

        // 3. Justo (Asignado a Stone)
        Usuario uJusto = crearUsuarioBase("Justo Bolsa", "justo@email.com", "paciente", "Masculino");
        Paciente pac3 = crearPacienteBase(uJusto, stone, false);
        crearHistorialBase(pac3, "Madre hipertensa.", "Insomnio severo.");
    }

    private void sembrarCitasYNotas() {
        List<Paciente> pacs = pacienteRepository.findAll();
        List<Psicologo> ps = psicologoRepository.findAll();

        Paciente reigen = pacs.get(0);
        Paciente kim = pacs.get(1);
        Paciente justo = pacs.get(2);

        Psicologo tenma = ps.get(0);
        Psicologo stone = ps.get(1);

        LocalDate mañana = LocalDate.now().plusDays(1);
        LocalDate enTresDias = LocalDate.now().plusDays(3);
        LocalDate laSemanaPasada = LocalDate.now().minusDays(7);

        // Cita 1:
        crearCitaEspecifica(kim, tenma, "pendiente", true, false, mañana.atTime(9, 0));

        // Cita 2:
        crearCitaEspecifica(kim, tenma, "rechazada", true, false, mañana.atTime(10, 0));

        // Cita 3:
        crearCitaEspecifica(justo, stone, "confirmada", false, false, enTresDias.atTime(16, 0));

        // Cita 4:
        crearCitaEspecifica(justo, stone, "cancelada", false, false, enTresDias.atTime(17, 0));

        // Cita 5:
        crearCitaEspecifica(kim, tenma, "no-show", false, true, laSemanaPasada.atTime(12, 0));

        // Cita 6
        Cita cFinalizada = crearCitaEspecifica(reigen, tenma, "finalizada", false, false, laSemanaPasada.atTime(11, 0));

        // Nota de evolución para la Cita 6
        NotaEvolucion nota = new NotaEvolucion();
        nota.setCita(cFinalizada);
        nota.setObservaciones("El paciente muestra mejoría con ejercicios de respiración.");
        nota.setDiagnostico("Ansiedad Leve.");
        nota.setPlanTratamiento("Continuar con ejercicios diarios.");
        nota.setFechaRegistro(LocalDateTime.now());
        notaEvolucionRepository.save(nota);
    }

    private void sembrarAuditorias() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

        List<Paciente> pacientes = pacienteRepository.findAll();
        List<Psicologo> psicologos = psicologoRepository.findAll();
        List<Cita> citas = citaRepository.findAll();

        if(pacientes.isEmpty() || psicologos.size() < 2 || citas.isEmpty()) return;

        Paciente reigen = pacientes.get(0);
        Psicologo tenma = psicologos.get(0);
        Psicologo stone = psicologos.get(1);

        Cita citaNoShow = citas.get(4);
        String fechaCitaStr = citaNoShow.getFechaHora().format(formatter);

        String haceSieteDias = LocalDateTime.now().minusDays(7).format(formatter);
        String haceDiezDias = LocalDateTime.now().minusDays(10).format(formatter);
        String haceOchoDias = LocalDateTime.now().minusDays(8).format(formatter);

        try {
            // 1. Inyectar Auditoria de Paciente (Reigen transferido de Stone a Tenma)
            String sqlPaciente = "INSERT INTO auditoria_pacientes (id_paciente, id_psicologo_anterior, id_psicologo_nuevo, fecha_modificacion) VALUES (?, ?, ?, ?)";
            jdbcTemplate.update(sqlPaciente, reigen.getId(), stone.getId(), tenma.getId(), haceSieteDias);

            // 2. Inyectar Auditoria de Citas (El historial del No-Show de Kim)
            String sqlCita = "INSERT INTO auditoria_citas (id_cita, fecha_anterior, fecha_nueva, estado_anterior, estado_nuevo, es_primera_anterior, es_primera_nuevo, fecha_modificacion) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";

            // Primer cambio: De pendiente a confirmada
            jdbcTemplate.update(sqlCita, citaNoShow.getId(), fechaCitaStr, fechaCitaStr, "pendiente", "confirmada", 0, 0, haceDiezDias);

            // Segundo cambio: De confirmada a no-show
            jdbcTemplate.update(sqlCita, citaNoShow.getId(), fechaCitaStr, fechaCitaStr, "confirmada", "no-show", 0, 0, haceOchoDias);

            System.out.println("[AUDITORÍAS] Historiales de simulación insertados exitosamente en MySQL.");
        } catch (Exception e) {
            System.err.println("[ERROR CRÍTICO AL SEMBRAR AUDITORÍAS]:");
            e.printStackTrace(); // Esto obligara a Railway a imprimir el error exacto si algo falla
        }
    }

    // --- Métodos Constructores Auxiliares ---
    private Usuario crearUsuarioBase(String nombre, String correo, String rol, String sexo) {
        Usuario u = new Usuario();
        u.setNombre(nombre);
        u.setCorreo(correo);
        u.setContraseña(passwordEncoder.encode("password123"));
        u.setRol(rol);
        u.setSexo(sexo);
        u.setFechaNacimiento(LocalDate.of(1995, 1, 1));
        return usuarioRepository.save(u);
    }

    private Psicologo crearPsicologoBase(Usuario u, int exp, String cedula, boolean planta, String res, Integer espId) {
        Psicologo p = new Psicologo();
        p.setUsuario(u);
        p.setAñosExperiencia(exp);
        p.setCedula(cedula);
        p.setEsDePlanta(planta);
        p.setResumen(res);
        HashSet<Especialidad> esp = new HashSet<>();
        especialidadRepository.findById(espId).ifPresent(esp::add);
        p.setEspecialidades(esp);
        return psicologoRepository.save(p);
    }

    private Paciente crearPacienteBase(Usuario u, Psicologo doc, boolean penalizado) {
        Paciente p = new Paciente();
        p.setUsuario(u);
        p.setPsicologo(doc);
        p.setPenalizacionActiva(penalizado);
        return pacienteRepository.save(p);
    }

    private HistorialClinico crearHistorialBase(Paciente p, String fam, String med) {
        HistorialClinico h = new HistorialClinico();
        h.setPaciente(p);
        h.setAntecedentesFamiliares(fam);
        h.setAntecedentesMedicos(med);
        h.setFechaCreacion(LocalDateTime.now());
        return historialClinicoRepository.save(h);
    }

    private Cita crearCitaEspecifica(Paciente pac, Psicologo psi, String estado, boolean primera, boolean multa, LocalDateTime fechaHora) {
        Cita c = new Cita();
        c.setPaciente(pac);
        c.setPsicologo(psi);
        c.setEstado(estado);
        c.setEsPrimera(primera);
        c.setMultaAplicada(multa);
        c.setFechaHora(fechaHora);
        c.setFechaModificacion(LocalDateTime.now());
        return citaRepository.save(c);
    }
}