package com.upsin.demo.controllers;

import com.upsin.demo.models.Usuario;
import com.upsin.demo.services.AuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private AuthService authService;

    @PostMapping("/registro")
    public Usuario registrarPaciente(@RequestBody Usuario usuario) {
        return authService.registrarPaciente(usuario);
    }
}