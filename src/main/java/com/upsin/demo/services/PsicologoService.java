package com.upsin.demo.services;

import com.upsin.demo.models.Psicologo;
import com.upsin.demo.dto.PsicologoDTO;
import com.upsin.demo.models.Especialidad;
import com.upsin.demo.repositories.PsicologoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class PsicologoService {

    @Autowired
    private PsicologoRepository psicologoRepository;

    public Page<PsicologoDTO> buscarPsicologosPorEspecialidad(String nombreEspecialidad, Pageable pageable) {

        // 1. Declaramos la variable temporal estricta para recibir el MODELO de la BD
        Page<Psicologo> paginaPsicologos;

        // 2. Ejecutamos la búsqueda en MySQL
        if (nombreEspecialidad == null || nombreEspecialidad.trim().isEmpty()) {
            paginaPsicologos = psicologoRepository.findAll(pageable);
        } else {
            paginaPsicologos = psicologoRepository.findByEspecialidadesNombreContainingIgnoreCase(nombreEspecialidad, pageable);
        }

        // 3. TRANSFORMACIÓN: .map() toma el Page<Psicologo> y lo convierte a Page<PsicologoDTO>
        return paginaPsicologos.map(this::convertirADto);
    }

    private PsicologoDTO convertirADto(Psicologo psicologo) {
        PsicologoDTO dto = new PsicologoDTO();
        dto.setIdPsicologo(psicologo.getId());

        // Extraemos los datos del usuario vinculado
        if (psicologo.getUsuario() != null) {
            dto.setNombre(psicologo.getUsuario().getNombre());
            dto.setCorreo(psicologo.getUsuario().getCorreo());
        }

        dto.setEsDePlanta(psicologo.getEsDePlanta());

        // Extraemos solo los nombres de las especialidades usando programación funcional (Streams)
        if (psicologo.getEspecialidades() != null) {
            dto.setEspecialidades(psicologo.getEspecialidades().stream()
                    .map(Especialidad::getNombre)
                    .collect(Collectors.toList()));
        }

        return dto;
    }
}