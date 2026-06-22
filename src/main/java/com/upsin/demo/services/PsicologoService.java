package com.upsin.demo.services;

import com.upsin.demo.models.Psicologo;
import com.upsin.demo.dto.PsicologoDTO;
import com.upsin.demo.models.Especialidad;
import com.upsin.demo.repositories.PsicologoRepository;
import com.upsin.demo.models.Usuario;
import com.upsin.demo.repositories.UsuarioRepository;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.stream.Collectors;

/**
 * Servicio enfocado en la capa de presentación segura y escalabilidad.
 * Transforma entidades pesadas de base de datos en DTOs ligeros para la capa HTTP.
 */
@Service
public class PsicologoService {

    @Autowired
    private PsicologoRepository psicologoRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    /**
     * Orquesta la búsqueda paginada de doctores por especialidad.
     */
    public Page<PsicologoDTO> buscarPsicologosPorEspecialidad(String nombreEspecialidad, Pageable pageable) {

        Page<Psicologo> paginaPsicologos;

        if (nombreEspecialidad == null || nombreEspecialidad.trim().isEmpty()) {
            paginaPsicologos = psicologoRepository.findAll(pageable);
        } else {
            paginaPsicologos = psicologoRepository.findByEspecialidadesNombreContainingIgnoreCase(nombreEspecialidad, pageable);
        }

        // Utiliza una referencia a método de Java para proyectar la transformación de los datos
        return paginaPsicologos.map(this::convertirADto);
    }

    public Page<PsicologoDTO> obtenerTodosLosPsicologosPaginados(Pageable pageable) {
        Page<Psicologo> psicologos = psicologoRepository.findAll(pageable);
        return psicologos.map(this::convertirADto); // Reutilizamos el mismo convertidor
    }

    /**
     * Helper Method: Transforma un objeto Psicologo en un PsicologoDTO.
     * Oculta datos sensibles y aplana las colecciones utilizando Streams funcionales.
     */
    public PsicologoDTO convertirADto(Psicologo psicologo) {
        PsicologoDTO dto = new PsicologoDTO();
        dto.setIdPsicologo(psicologo.getId());

        // Agregamos los campos administrativos
        dto.setAñosExperiencia(psicologo.getAñosExperiencia());
        dto.setCedula(psicologo.getCedula());
        dto.setResumen(psicologo.getResumen());
        dto.setFotoUrl(psicologo.getFotoUrl());
        dto.setEsDePlanta(psicologo.getEsDePlanta());

        if (psicologo.getUsuario() != null) {
            dto.setNombre(psicologo.getUsuario().getNombre());
            dto.setCorreo(psicologo.getUsuario().getCorreo());
            dto.setSexo(psicologo.getUsuario().getSexo()); // Extraemos el sexo
        }

        if (psicologo.getEspecialidades() != null) {
            dto.setEspecialidades(psicologo.getEspecialidades().stream()
                    .map(Especialidad::getNombre)
                    .collect(Collectors.toList()));
        }

        return dto;
    }

    public PsicologoDTO actualizarPerfil(Psicologo datosActualizados) {

        // 1. Obtenemos el correo del token
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String correo = auth.getName();

        // 2. Buscamos a qué psicólogo le pertenece este correo
        Usuario usuario = usuarioRepository.findByCorreo(correo)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        Psicologo psicologo = psicologoRepository.findById(usuario.getId())
                .orElseThrow(() -> new RuntimeException("Psicólogo no encontrado"));

        // 3. Actualizamos solo los datos profesionales
        psicologo.setAñosExperiencia(datosActualizados.getAñosExperiencia());
        psicologo.setResumen(datosActualizados.getResumen());
        psicologo.setCedula(datosActualizados.getCedula());

        if (datosActualizados.getEsDePlanta() != null) {
            psicologo.setEsDePlanta(datosActualizados.getEsDePlanta());
        }

        // 4. Guardamos en MySQL y convertimos a DTO
        Psicologo psicologoGuardado = psicologoRepository.save(psicologo);
        return convertirADto(psicologoGuardado);
    }
}