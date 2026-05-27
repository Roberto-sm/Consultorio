package com.upsin.demo.services;

import com.upsin.demo.models.Paciente;
import com.upsin.demo.models.Usuario;
import com.upsin.demo.repositories.PacienteRepository;
import com.upsin.demo.repositories.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private PacienteRepository pacienteRepository;

    // Esta etiqueta garantiza que ambas tablas se actualicen o ninguna lo haga
    @Transactional
    public Usuario registrarPaciente(Usuario nuevoUsuario) {

        // 1. Forzamos el rol para evitar inyecciones de datos incorrectos
        nuevoUsuario.setRol("paciente");

        // 2. Guardamos en la tabla 'usuarios'
        // Al guardar, MySQL genera el ID y Spring Boot se lo inyecta a la variable usuarioGuardado
        Usuario usuarioGuardado = usuarioRepository.save(nuevoUsuario);

        // 3. Preparamos el registro para la tabla 'pacientes' usando ese mismo ID
        Paciente nuevoPaciente = new Paciente();
        nuevoPaciente.setUsuario(usuarioGuardado);
        // Nota: Aún no tiene id_psicologo porque es un paciente nuevo

        // 4. Guardamos en la tabla 'pacientes'
        pacienteRepository.save(nuevoPaciente);

        return usuarioGuardado;
    }
}