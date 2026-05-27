package com.upsin.demo.controllers;

import com.upsin.demo.models.Especialidad;
import com.upsin.demo.repositories.EspecialidadRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController // Le dice a Spring que esta clase devolverá respuestas en formato JSON
@RequestMapping("/api/especialidades") // Define la URL principal para este controlador
public class EspecialidadController {

    @Autowired // Inyecta automáticamente el repositorio que creamos en el Paso 1
    private EspecialidadRepository especialidadRepository;

    // Endpoint para obtener TODAS las especialidades
    @GetMapping
    public List<Especialidad> obtenerTodas() {
        // Esto equivale a hacer la consulta a MySQL, empaquetar los resultados
        // en una lista de objetos Java y convertirlos a JSON. Todo en una línea.
        return especialidadRepository.findAll();
    }
}
