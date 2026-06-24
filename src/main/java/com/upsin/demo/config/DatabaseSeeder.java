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
        // 1. Dr. Tenma (De Planta) - Cumple perfil actualizado y de_planta = true
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

        // 1. Natanael (Asignado a Tenma, sin penalización)
        Usuario uReigen = crearUsuarioBase("Reigen Arataka", "reigen@email.com", "paciente", "Masculino");
        Paciente pac1 = crearPacienteBase(uReigen, tenma, false);
        crearHistorialBase(pac1, "Padre con adicciones.", "Ansiedad social.");

        // 2. Luis (Asignado a Stone, CON penalización activa) - Cumple condición
        Usuario ukim = crearUsuarioBase("Kim Wexler", "kim@email.com", "paciente", "Femenino");
        Paciente pac2 = crearPacienteBase(ukim, tenma, true);
        crearHistorialBase(pac2, "Ninguno.", "Estrés crónico.");

        // 3. Hassan (Nuevo, sin asignar) - Cumple condición: todos con historial
        Usuario uJusto = crearUsuarioBase("Justo Bolsa", "justo@email.com", "paciente", "Masculino");
        Paciente pac3 = crearPacienteBase(uJusto, stone, false);
        crearHistorialBase(pac3, "Madre hipertensa.", "Insomnio severo.");
    }

    private void sembrarCitasYNotas() {
        List<Paciente> pacs = pacienteRepository.findAll();
        List<Psicologo> ps = psicologoRepository.findAll();

        Paciente reigen = pacs.get(0);   // Paciente 1
        Paciente kim = pacs.get(1);  // Paciente 2
        Paciente justo = pacs.get(2); // Paciente 3

        Psicologo tenma = ps.get(0);   // Psicólogo 1
        Psicologo stone = ps.get(1);   // Psicólogo 2

        LocalDate mañana = LocalDate.now().plusDays(1);
        LocalDate enTresDias = LocalDate.now().plusDays(3);
        LocalDate laSemanaPasada = LocalDate.now().minusDays(7);

        Cita c1 = crearCitaEspecifica(kim, tenma, "pendiente", true, false, mañana.atTime(9, 0));

        crearCitaEspecifica(kim, tenma, "rechazada", true, false, mañana.atTime(10, 0));

        crearCitaEspecifica(justo, stone, "confirmada", false, false, enTresDias.atTime(16, 0));

        crearCitaEspecifica(justo, stone, "cancelada", false, false, enTresDias.atTime(17, 0));

        crearCitaEspecifica(kim, tenma, "no-show", false, true, laSemanaPasada.atTime(12, 0));

        Cita cFinalizada = crearCitaEspecifica(reigen, tenma, "finalizada", false, false, laSemanaPasada.atTime(11, 0));

        // Cumple condición: Al menos una nota de evolución post-consulta
        NotaEvolucion nota = new NotaEvolucion();
        nota.setCita(cFinalizada);
        nota.setObservaciones("El paciente muestra mejoría con ejercicios de respiración.");
        nota.setFechaRegistro(LocalDateTime.now());
        notaEvolucionRepository.save(nota);
    }

    // Método auxiliar con soporte de fecha y hora exacta
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

    // Reemplaza el método anterior por este:
    private void sembrarAuditorias() {
        List<Cita> citas = citaRepository.findAll();
        List<Paciente> pacientes = pacienteRepository.findAll();
        List<Psicologo> psicologos = psicologoRepository.findAll();

        // 1. Disparar Trigger de Citas (Hacemos un UPDATE)
        // Tomamos una cita pendiente y le cambiamos el estado a confirmada, y el booleano a false
        Cita citaTrigger = citas.get(0);
        citaTrigger.setEstado("confirmada");
        citaTrigger.setEsPrimera(false);
        // Al hacer save(), MySQL detectará el UPDATE y disparará 'tr_auditar_cambio_cita'
        citaRepository.save(citaTrigger);

        // 2. Disparar Trigger de Pacientes (Hacemos un UPDATE)
        // Tomamos al paciente de Stone y se lo pasamos a Tenma
        Paciente pacienteTrigger = pacientes.get(1);
        pacienteTrigger.setPsicologo(psicologos.get(0));
        // Al hacer save(), MySQL detectará el UPDATE y disparará 'tr_auditar_cambio_psicologo'
        pacienteRepository.save(pacienteTrigger);

        System.out.println("⚙️ [TRIGGERS] Updates realizados. Auditorías generadas automáticamente por MySQL.");
    }

    // --- Métodos Constructores Auxiliares (Para ahorrar líneas) ---

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

    private Cita crearCitaBase(Paciente pac, Psicologo psi, String estado, boolean primera, boolean multa, int dias) {
        Cita c = new Cita();
        c.setPaciente(pac);
        c.setPsicologo(psi);
        c.setEstado(estado);
        c.setEsPrimera(primera);
        c.setMultaAplicada(multa);
        c.setFechaHora(LocalDateTime.now().plusDays(dias));
        c.setFechaModificacion(LocalDateTime.now());
        return citaRepository.save(c);
    }
}