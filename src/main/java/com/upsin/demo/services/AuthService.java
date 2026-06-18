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

/**
 * Servicio de Autenticación y Registro.
 * Encapsula la lógica de negocio para la creación segura de cuentas, encriptación
 * de contraseñas mediante BCrypt, y generación de tokens JWT.
 */
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

    /**
     * Registra un nuevo paciente en el sistema.
     * Es una operación transaccional: crea el usuario, encripta su contraseña,
     * le asigna automáticamente un psicólogo de planta y le genera un expediente clínico vacío.
     * * @param nuevoUsuario Entidad Usuario con los datos extraídos del RequestBody.
     * @return El Usuario guardado en la base de datos.
     * @throws RuntimeException Si no existe un psicólogo de planta configurado en el sistema.
     */
    @Transactional
    public Usuario registrarPaciente(Usuario nuevoUsuario) {
        nuevoUsuario.setRol("paciente");
        nuevoUsuario.setContraseña(passwordEncoder.encode(nuevoUsuario.getContraseña()));

        Usuario usuarioGuardado = usuarioRepository.save(nuevoUsuario);

        Psicologo psicologoPlanta = psicologoRepository.findFirstByEsDePlantaTrue()
                .orElseThrow(() -> new RuntimeException("Error crítico: No hay un psicólogo de planta configurado en el sistema para recibir al paciente."));

        Paciente nuevoPaciente = new Paciente();
        nuevoPaciente.setUsuario(usuarioGuardado);
        nuevoPaciente.setPsicologo(psicologoPlanta);

        Paciente pacienteGuardado = pacienteRepository.save(nuevoPaciente);

        HistorialClinico historialVacio = new HistorialClinico();
        historialVacio.setPaciente(pacienteGuardado);

        historialClinicoRepository.save(historialVacio);

        return usuarioGuardado;
    }

    /**
     * Registra un nuevo profesional de la salud en el sistema.
     * Crea su perfil base de usuario y su entrada correspondiente en la tabla de psicólogos.
     * * @param nuevoUsuario Entidad Usuario con los datos de registro.
     * @return El Usuario guardado.
     */
    @Transactional
    public Usuario registrarPsicologo(Usuario nuevoUsuario) {
        nuevoUsuario.setRol("psicologo");
        nuevoUsuario.setContraseña(passwordEncoder.encode(nuevoUsuario.getContraseña()));

        Usuario usuarioGuardado = usuarioRepository.save(nuevoUsuario);

        Psicologo nuevoPsicologo = new Psicologo();
        nuevoPsicologo.setUsuario(usuarioGuardado);
        nuevoPsicologo.setEsDePlanta(false);

        psicologoRepository.save(nuevoPsicologo);

        return usuarioGuardado;
    }

    /**
     * Valida las credenciales de un usuario y genera un token de acceso.
     * * @param request Objeto DTO que contiene correo y contraseña en texto plano.
     * @return Objeto AuthResponse con el Token JWT generado y los datos de la sesión.
     * @throws RuntimeException Si el correo no existe o la contraseña no coincide.
     */
    public AuthResponse login(LoginRequest request) {
        Usuario usuario = usuarioRepository.findByCorreo(request.getCorreo())
                .orElseThrow(() -> new RuntimeException("Error: Correo no encontrado"));

        boolean esValida = passwordEncoder.matches(request.getContraseña(), usuario.getContraseña());

        if (!esValida) {
            throw new RuntimeException("Error: Contraseña incorrecta");
        }

        AuthResponse response = new AuthResponse();
        response.setMensaje("Login exitoso");
        response.setRol(usuario.getRol());
        response.setIdUsuario(usuario.getId());

        String tokenReal = jwtUtil.generarToken(usuario.getCorreo(), usuario.getRol());
        response.setToken(tokenReal);

        return response;
    }
}