package com.upsin.demo.config;

import com.upsin.demo.models.*;
import com.upsin.demo.repositories.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;
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

    @Override
    public void run(String... args) throws Exception {
        if (especialidadRepository.count() == 0 && usuarioRepository.count() == 0) {
            System.out.println("[SEEDER] Iniciando inyección del Kit de Demostración Base...");

            sembrarEspecialidades();
            sembrarPsicologos();
            sembrarPacientesEHistoriales();
            sembrarCitasYNotas();

            System.out.println("[SEEDER] Base de datos inyectada exitosamente (Entidades Base).");
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
        // 1. Dr. Tenma (De Planta) -> ID 1
        Usuario u1 = crearUsuarioBase("Dr. Tenma Kenzo", "tenma@email.com", "psicologo", "Masculino");
        Psicologo p1 = crearPsicologoBase(u1, 15, "CED-112233", true, "Especialista clínico.", 1);

        // 2. Dr. Stone -> ID 2
        Usuario u2 = crearUsuarioBase("Dr. Stone", "senku@email.com", "psicologo", "Masculino");
        crearPsicologoBase(u2, 7, "CED-1000000", false, "Especialista educativo.", 2);
    }

    private void sembrarPacientesEHistoriales() {
        List<Psicologo> ps = psicologoRepository.findAll();
        Psicologo tenma = ps.get(0);
        Psicologo stone = ps.get(1);

        // 1. Reigen -> ID 1
        Usuario uReigen = crearUsuarioBase("Reigen Arataka", "reigen@email.com", "paciente", "Masculino");
        Paciente pac1 = crearPacienteBase(uReigen, tenma, false);
        crearHistorialBase(pac1, "Padre con adicciones.", "Ansiedad social.");

        // 2. Kim -> ID 2
        Usuario ukim = crearUsuarioBase("Kim Wexler", "kim@email.com", "paciente", "Femenino");
        Paciente pac2 = crearPacienteBase(ukim, tenma, true);
        crearHistorialBase(pac2, "Ninguno.", "Estrés crónico.");

        // 3. Justo -> ID 3
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

        LocalDateTime pasadoLunes = LocalDateTime.of(2026, 8, 17, 11, 0);
        LocalDateTime pasadoMiercoles = LocalDateTime.of(2026, 8, 19, 12, 0);

        LocalDateTime lunesFuturo9AM = LocalDateTime.of(2026, 8, 24, 9, 0);
        LocalDateTime lunesFuturo10AM = LocalDateTime.of(2026, 8, 24, 10, 0);
        LocalDateTime martesFuturo4PM = LocalDateTime.of(2026, 8, 31, 16, 0);
        LocalDateTime martesFuturo5PM = LocalDateTime.of(2026, 8, 31, 17, 0);

        // Cita 1: Finalizada
        Cita c1 = crearCitaEspecifica(kim, tenma, "finalizada", true, false, pasadoLunes);
        // Cita 2: Rechazada
        crearCitaEspecifica(kim, tenma, "rechazada", true, false, lunesFuturo10AM);
        // Cita 3: Cancelada
        crearCitaEspecifica(justo, stone, "cancelada", false, false, martesFuturo4PM);
        // Cita 4: confirmada
        crearCitaEspecifica(justo, stone, "confirmada", false, false, martesFuturo5PM);
        // Cita 5: No-Show
        crearCitaEspecifica(kim, tenma, "no-show", false, true, pasadoMiercoles);
        // Cita 6: Pendiente
        crearCitaEspecifica(reigen, tenma, "pendiente", false, false, lunesFuturo9AM);

        HistorialClinico historialKim = historialClinicoRepository.findByPacienteId(kim.getId()).orElse(null);
        if (historialKim != null) {
            NotaEvolucion nota = new NotaEvolucion();
            nota.setHistorialClinico(historialKim);
            nota.setCita(c1); // Perfectamente asociada a la Cita 1 histórica y finalizada
            nota.setObservaciones("El paciente muestra mejoría con ejercicios de respiración.");
            nota.setDiagnostico("Ansiedad situacional controlada.");
            nota.setPlanTratamiento("Continuar con bitácora de sueño y pausas activas.");
            nota.setFechaRegistro(pasadoLunes.plusMinutes(55)); // Registrada inmediatamente después de la sesión

            notaEvolucionRepository.save(nota);
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