package com.upsin.demo.controllers;

import com.upsin.demo.models.Usuario;
import com.upsin.demo.services.AuthService;
import com.upsin.demo.dto.LoginRequest;
import com.upsin.demo.dto.AuthResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private AuthService authService;

    // endpoint para pacientes
    @PostMapping("/registro")
    public Usuario registrarPaciente(@RequestBody Usuario usuario) {
        return authService.registrarPaciente(usuario);
    }

    // endpoint para psicologos
    @PostMapping("/registro/psicologo")
    public Usuario registrarPsicologo(@RequestBody Usuario usuario) {
        return authService.registrarPsicologo(usuario);
    }

    @PostMapping("/login")
    public AuthResponse login(@RequestBody LoginRequest request) {
        return authService.login(request);
    }
}