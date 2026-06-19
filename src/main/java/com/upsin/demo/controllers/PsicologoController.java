package com.upsin.demo.controllers;

import com.upsin.demo.models.Psicologo;
import com.upsin.demo.dto.PsicologoDTO;
import com.upsin.demo.services.PsicologoService;
import com.upsin.demo.repositories.PsicologoRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
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

@Tag(name = "7. Directorio Médico", description = "Buscador de especialistas, paginación, y gestión de perfiles profesionales.")
@RestController
@RequestMapping("/api/psicologos")
public class PsicologoController {

    @Autowired
    private PsicologoRepository psicologoRepository;

    @Autowired
    private PsicologoService psicologoService;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Operation(summary = "Obtener todos los perfiles (Sin procesar)", description = "Devuelve los objetos completos directos de base de datos. Uso estrictamente administrativo.")
    @GetMapping
    public List<Psicologo> obtenerTodos() {
        return psicologoRepository.findAll();
    }

    @Operation(summary = "Actualizar mi perfil", description = "Endpoint protegido para que un psicólogo pueda editar su currículum, años de experiencia y cédula basándose en su Token de acceso.")
    @PreAuthorize("hasRole('PSICOLOGO')")
    @PutMapping("/perfil")
    public Psicologo actualizarPerfil(@RequestBody Psicologo datosActualizados) {

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String correo = auth.getName();

        Usuario usuario = usuarioRepository.findByCorreo(correo)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        Psicologo psicologo = psicologoRepository.findById(usuario.getId())
                .orElseThrow(() -> new RuntimeException("Psicólogo no encontrado"));

        psicologo.setAñosExperiencia(datosActualizados.getAñosExperiencia());
        psicologo.setResumen(datosActualizados.getResumen());
        psicologo.setCedula(datosActualizados.getCedula());

        if (datosActualizados.getEsDePlanta() != null) {
            psicologo.setEsDePlanta(datosActualizados.getEsDePlanta());
        }

        return psicologoRepository.save(psicologo);
    }

    @Operation(summary = "Buscador de Especialistas (Paginado / DTO)", description = "Búsqueda relacional ignorando mayúsculas. Expone los datos a través de un DTO (Data Transfer Object) para asegurar la información y utiliza paginación para optimizar la carga del servidor.")
    @GetMapping("/buscar")
    public Page<PsicologoDTO> buscarPorEspecialidad(
            @RequestParam(required = false, defaultValue = "") String especialidad,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        Pageable paginacion = PageRequest.of(page, size);
        return psicologoService.buscarPsicologosPorEspecialidad(especialidad, paginacion);
    }
}