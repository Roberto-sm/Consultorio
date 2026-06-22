package com.upsin.demo.services;

import com.upsin.demo.models.Psicologo;
import com.upsin.demo.dto.PsicologoDTO;
import com.upsin.demo.models.Especialidad;
import com.upsin.demo.repositories.PsicologoRepository;
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
    private PsicologoDTO convertirADto(Psicologo psicologo) {
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
}