package com.upsin.demo.services;

import com.upsin.demo.models.Psicologo;
import com.upsin.demo.repositories.PsicologoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class PsicologoService {

    @Autowired
    private PsicologoRepository psicologoRepository;

    public List<Psicologo> buscarPsicologosPorEspecialidad(String nombreEspecialidad) {
        // Validación básica: si el texto viene vacío, podríamos retornar todos o lanzar un error
        if (nombreEspecialidad == null || nombreEspecialidad.trim().isEmpty()) {
            return psicologoRepository.findAll();
        }

        return psicologoRepository.findByEspecialidadesNombreContainingIgnoreCase(nombreEspecialidad);
    }
}