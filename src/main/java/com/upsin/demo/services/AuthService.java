package com.upsin.demo.services;

import com.upsin.demo.models.Paciente;
import com.upsin.demo.models.Psicologo;
import com.upsin.demo.models.Usuario;
import com.upsin.demo.repositories.PacienteRepository;
import com.upsin.demo.repositories.PsicologoRepository;
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

    @Autowired
    private PsicologoRepository psicologoRepository;

    @Transactional
    public Usuario registrarPaciente(Usuario nuevoUsuario) {

        //  Forzamos el rol para evitar inyecciones de datos incorrectos
        nuevoUsuario.setRol("paciente");

        //  Guardamos en la tabla 'usuarios, se le asigna el id del usuario a la variable usuarioGuardado
        Usuario usuarioGuardado = usuarioRepository.save(nuevoUsuario);

        //  Preparamos el registro para la tabla 'pacientes' usando ese mismo ID
        Paciente nuevoPaciente = new Paciente();
        nuevoPaciente.setUsuario(usuarioGuardado);

        //  Guardamos en la tabla 'pacientes'
        pacienteRepository.save(nuevoPaciente);

        return usuarioGuardado;
    }

    @Transactional
    public Usuario registrarPsicologo(Usuario nuevoUsuario) {

        // Forzamos el rol
        nuevoUsuario.setRol("psicologo");

        // Guardamos en la tabla 'usuarios'
        Usuario usuarioGuardado = usuarioRepository.save(nuevoUsuario);

        //  Creamos el perfil vacío en la tabla 'psicologos'
        Psicologo nuevoPsicologo = new Psicologo();
        nuevoPsicologo.setUsuario(usuarioGuardado);

        // Por defecto, un psicólogo nuevo no es de planta.
        // (Esto solo lo debería cambiar un administrador después)
        nuevoPsicologo.setEsDePlanta(false);

        //  Guardamos en la tabla 'psicologos'
        psicologoRepository.save(nuevoPsicologo);

        return usuarioGuardado;
    }
}

