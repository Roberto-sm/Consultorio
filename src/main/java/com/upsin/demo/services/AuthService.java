package com.upsin.demo.services;

import com.upsin.demo.models.HistorialClinico;
import com.upsin.demo.models.Paciente;
import com.upsin.demo.models.Psicologo;
import com.upsin.demo.models.Usuario;
import com.upsin.demo.repositories.HistorialClinicoRepository;
import com.upsin.demo.repositories.PacienteRepository;
import com.upsin.demo.repositories.PsicologoRepository;
import com.upsin.demo.repositories.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.upsin.demo.dto.LoginRequest;
import com.upsin.demo.dto.AuthResponse;
import com.upsin.demo.config.JwtUtil;

@Service
public class AuthService {

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private PacienteRepository pacienteRepository;

    @Autowired
    private PsicologoRepository psicologoRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private HistorialClinicoRepository historialClinicoRepository;

    @Autowired
    private JwtUtil jwtUtil;

    @Transactional
    public Usuario registrarPaciente(Usuario nuevoUsuario) {

        //  Forzamos el rol para evitar inyecciones de datos incorrectos
        nuevoUsuario.setRol("paciente");

        // Encriptacion de contraseñas
        String hash = passwordEncoder.encode(nuevoUsuario.getContraseña());
        nuevoUsuario.setContraseña(hash);

        //  Guardamos en la tabla 'usuarios, se le asigna el id del usuario a la variable usuarioGuardado
        Usuario usuarioGuardado = usuarioRepository.save(nuevoUsuario);

        //  Preparamos el registro para la tabla 'pacientes' usando ese mismo ID
        Paciente nuevoPaciente = new Paciente();
        nuevoPaciente.setUsuario(usuarioGuardado);

        Paciente pacienteGuardado = pacienteRepository.save(nuevoPaciente);

        HistorialClinico historialVacio = new HistorialClinico();
        historialVacio.setPaciente(pacienteGuardado);

        historialClinicoRepository.save(historialVacio);

        return usuarioGuardado;
    }

    @Transactional
    public Usuario registrarPsicologo(Usuario nuevoUsuario) {

        // Forzamos el rol
        nuevoUsuario.setRol("psicologo");

        // Encriptacion de contraseñas
        String hash = passwordEncoder.encode(nuevoUsuario.getContraseña());
        nuevoUsuario.setContraseña(hash);

        // Guardamos en la tabla 'usuarios'
        Usuario usuarioGuardado = usuarioRepository.save(nuevoUsuario);

        //  Creamos el perfil vacío en la tabla 'psicologos'
        Psicologo nuevoPsicologo = new Psicologo();
        nuevoPsicologo.setUsuario(usuarioGuardado);

        // Por defecto, un psicólogo nuevo no es de planta. Esto solo lo debería cambiar un administrador después
        nuevoPsicologo.setEsDePlanta(false);

        //  Guardamos en la tabla 'psicologos'
        psicologoRepository.save(nuevoPsicologo);

        return usuarioGuardado;
    }

    public AuthResponse login(LoginRequest request) {

        // Busca al usuario por correo
        Usuario usuario = usuarioRepository.findByCorreo(request.getCorreo())
                .orElseThrow(() -> new RuntimeException("Error: Correo no encontrado"));

        // matches(contraseña_plana_de_postman, contraseña_hasheada_de_mysql)
        boolean esValida = passwordEncoder.matches(request.getContraseña(), usuario.getContraseña());

        if (!esValida) {
            throw new RuntimeException("Error: Contraseña incorrecta");
        }

        // mensaje de exito
        AuthResponse response = new AuthResponse();
        response.setMensaje("Login exitoso");
        response.setRol(usuario.getRol());
        response.setIdUsuario(usuario.getId());

        // Por ahora simularemos un token. El siguiente paso será generar un JWT real.
        String tokenReal = jwtUtil.generarToken(usuario.getCorreo(), usuario.getRol());
        response.setToken(tokenReal);

        return response;
    }

}

