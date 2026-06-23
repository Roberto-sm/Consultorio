package com.upsin.demo.config;

import com.upsin.demo.models.Paciente;
import com.upsin.demo.models.Psicologo;
import com.upsin.demo.models.Usuario;
import com.upsin.demo.repositories.PacienteRepository;
import com.upsin.demo.repositories.PsicologoRepository;
import com.upsin.demo.repositories.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

@Component
public class DatabaseSeeder implements CommandLineRunner {

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private PsicologoRepository psicologoRepository;

    @Autowired
    private PacienteRepository pacienteRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) throws Exception {
        sembrarPsicologoDePrueba();
        sembrarPacienteDePrueba();
    }

    private void sembrarPsicologoDePrueba() {
        // Solo lo creamos si no existe en la base de datos
        if (usuarioRepository.findByCorreo("tenma@email.com").isEmpty()) {

            // 1. Crear el registro base del Usuario
            Usuario usuarioTenma = new Usuario();
            usuarioTenma.setNombre("Dr. Tenma Kenzo");
            usuarioTenma.setCorreo("tenma@email.com");
            // Hasheamos la contraseña para que el login de Spring Security funcione
            usuarioTenma.setContraseña(passwordEncoder.encode("password123"));
            usuarioTenma.setRol("psicologo");
            usuarioTenma.setSexo("Masculino");
            usuarioTenma.setFechaNacimiento(LocalDate.of(1988, 4, 20));

            usuarioRepository.save(usuarioTenma);

            // 2. Crear el registro profesional vinculado
            Psicologo psicologo = new Psicologo();
            psicologo.setUsuario(usuarioTenma);
            psicologo.setAñosExperiencia(15);
            psicologo.setCedula("CED-112233");
            psicologo.setEsDePlanta(true);
            psicologo.setResumen("Especialista en terapia educativa enfocado al nivel infantil y adolescente.");

            psicologoRepository.save(psicologo);

            System.out.println("🌱 [SEEDER] Psicólogo de prueba (Dr. Tenma) sembrado con éxito.");
        }
    }

    private void sembrarPacienteDePrueba() {
        // Solo lo creamos si no existe en la base de datos
        if (usuarioRepository.findByCorreo("nata@email.com").isEmpty()) {

            // 1. Crear el registro base del Usuario
            Usuario usuarioNata = new Usuario();
            usuarioNata.setNombre("Natanael Cano");
            usuarioNata.setCorreo("nata@email.com");
            usuarioNata.setContraseña(passwordEncoder.encode("password123"));
            usuarioNata.setRol("paciente");
            usuarioNata.setSexo("Masculino");
            usuarioNata.setFechaNacimiento(LocalDate.of(2002, 6, 10));

            usuarioRepository.save(usuarioNata);

            // 2. Crear el registro de paciente vinculado (y asignárselo a Tenma para pruebas)
            Paciente paciente = new Paciente();
            paciente.setUsuario(usuarioNata);
            paciente.setPenalizacionActiva(false);

            // Buscamos a Tenma para asignarlo como su doctor de cabecera
            usuarioRepository.findByCorreo("tenma@email.com").ifPresent(usuarioPsicologo -> {
                psicologoRepository.findById(usuarioPsicologo.getId()).ifPresent(paciente::setPsicologo);
            });

            pacienteRepository.save(paciente);

            System.out.println("🌱 [SEEDER] Paciente de prueba (Natanael) sembrado con éxito.");
        }
    }
}