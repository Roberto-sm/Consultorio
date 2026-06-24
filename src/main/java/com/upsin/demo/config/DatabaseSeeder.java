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
            System.out.println("🌱 [SEEDER] Iniciando inyección del Kit de Demostración Completo...");

            sembrarEspecialidades();
            sembrarPsicologos();
            sembrarPacientesEHistoriales();
            sembrarCitasYNotas();
            sembrarAuditorias();

            System.out.println("🚀 [SEEDER] Base de datos inyectada con todos los casos de prueba (CRUD, Auditorías y Estados).");
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
        Usuario u2 = crearUsuarioBase("Dr. Senku Stone", "stone@email.com", "psicologo", "Masculino");
        crearPsicologoBase(u2, 7, "CED-445566", false, "Especialista educativo.", 2);
    }

    private void sembrarPacientesEHistoriales() {
        List<Psicologo> ps = psicologoRepository.findAll();

        // 1. Natanael (Asignado a Tenma, sin penalización)
        Usuario uNata = crearUsuarioBase("Natanael Cano", "nata@email.com", "paciente", "Masculino");
        Paciente pac1 = crearPacienteBase(uNata, ps.get(0), false);
        crearHistorialBase(pac1, "Padre con adicciones.", "Ansiedad social.");

        // 2. Luis (Asignado a Stone, CON penalización activa) - Cumple condición
        Usuario uLuis = crearUsuarioBase("Luis Conriquez", "luis@email.com", "paciente", "Masculino");
        Paciente pac2 = crearPacienteBase(uLuis, ps.get(1), true);
        crearHistorialBase(pac2, "Ninguno.", "Estrés crónico.");

        // 3. Hassan (Nuevo, sin asignar) - Cumple condición: todos con historial
        Usuario uPeso = crearUsuarioBase("Hassan Peso", "peso@email.com", "paciente", "Masculino");
        Paciente pac3 = crearPacienteBase(uPeso, null, false);
        crearHistorialBase(pac3, "Madre hipertensa.", "Insomnio severo.");
    }

    private void sembrarCitasYNotas() {
        List<Paciente> pacs = pacienteRepository.findAll();
        List<Psicologo> ps = psicologoRepository.findAll();

        // Citas para cumplir todos los estados y multas
        Cita cPendiente = crearCitaBase(pacs.get(2), ps.get(0), "pendiente", true, false, 2);
        Cita cConfirmada = crearCitaBase(pacs.get(0), ps.get(0), "confirmada", false, false, 5);
        Cita cCancelada = crearCitaBase(pacs.get(0), ps.get(0), "cancelada", false, false, 7);
        Cita cRechazada = crearCitaBase(pacs.get(2), ps.get(0), "rechazada", true, false, -1);

        // Cumple condición: No-show con multa aplicada = true (Justifica la penalización de Luis)
        Cita cNoShow = crearCitaBase(pacs.get(1), ps.get(1), "no-show", false, true, -3);

        // Cumple condición: Finalizada
        Cita cFinalizada = crearCitaBase(pacs.get(0), ps.get(0), "finalizada", false, false, -10);

        // Cumple condición: Al menos una nota de evolución post-consulta
        NotaEvolucion nota = new NotaEvolucion();
        nota.setCita(cFinalizada);
        nota.setObservaciones("El paciente muestra mejoría con ejercicios de respiración.");
        nota.setFechaRegistro(LocalDateTime.now());
        notaEvolucionRepository.save(nota);
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