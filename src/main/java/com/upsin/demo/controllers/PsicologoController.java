package com.upsin.demo.controllers;

import com.upsin.demo.models.Psicologo;
import com.upsin.demo.dto.PsicologoDTO;
import com.upsin.demo.services.PsicologoService;
import com.upsin.demo.repositories.PsicologoRepository;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;
import com.upsin.demo.models.Usuario;
import com.upsin.demo.repositories.UsuarioRepository;
import java.util.List;

@RestController
@RequestMapping("/api/psicologos")
public class PsicologoController {

    @Autowired
    private PsicologoRepository psicologoRepository;

    @Autowired
    private PsicologoService psicologoService;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @GetMapping
    public List<Psicologo> obtenerTodos() {
        return psicologoRepository.findAll();
    }

    @PreAuthorize("hasRole('PSICOLOGO')")
    @PutMapping("/perfil")
    public Psicologo actualizarPerfil(@RequestBody Psicologo datosActualizados) {

        // 1. Obtenemos el correo del token (El gafete actual)
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

        // Solo un administrador debería poder hacer a alguien "De Planta", pero lo dejaremos aquí por ahora
        if (datosActualizados.getEsDePlanta() != null) {
            psicologo.setEsDePlanta(datosActualizados.getEsDePlanta());
        }

        // 4. Guardamos
        return psicologoRepository.save(psicologo);
    }

    @Operation(
            summary = "Buscador de Especialistas (Paginado)",
            description = "Búsqueda relacional ignorando mayúsculas. Devuelve DTOs paginados para no saturar la memoria del cliente."
    )
    @GetMapping("/buscar")
    public Page<PsicologoDTO> buscarPorEspecialidad(
            @RequestParam(required = false, defaultValue = "") String especialidad,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        Pageable paginacion = PageRequest.of(page, size);
        return psicologoService.buscarPsicologosPorEspecialidad(especialidad, paginacion);
    }
}
