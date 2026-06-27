package com.upsin.demo.controllers;

import com.upsin.demo.controllers.docs.AuthApi;
import com.upsin.demo.models.Usuario;
import com.upsin.demo.services.AuthService;
import com.upsin.demo.dto.LoginRequest;
import com.upsin.demo.dto.AuthResponse;
import org.springframework.beans.factory.annotation.Autowired;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;
import io.swagger.v3.oas.annotations.tags.Tag;
import com.upsin.demo.utils.SwaggerConstants;


@Tag(name = "2. Autenticación y Seguridad", description = "Endpoints de acceso público para el registro de nuevos usuarios y la generación de tokens JWT.")
@RestController
@RequestMapping("/api/auth")
public class AuthController implements AuthApi {

    @Autowired
    private AuthService authService;

    @Override
    @PostMapping("/registro")
    public Usuario registrarPaciente(@Valid @RequestBody Usuario usuario) {
        return authService.registrarPaciente(usuario);
    }

    @Operation(summary = "Registrar un nuevo Psicólogo", description = "Crea un usuario con el rol 'psicologo' y genera su perfil en la base de datos de profesionales. Por defecto, no se le asigna el estatus 'de planta'.")
    @PostMapping("/registro/psicologo")
    public Usuario registrarPsicologo(@Valid @RequestBody Usuario usuario) {
        return authService.registrarPsicologo(usuario);
    }

    @Operation(summary = "Iniciar Sesión", description = "Valida el correo y la contraseña contra la base de datos. Si son correctos, devuelve un Token JWT firmado para consumir las rutas privadas de la API.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Token generado exitosamente"),
            @ApiResponse(responseCode = "400", description = "Credenciales incorrectas")
    })

    @PostMapping("/login")
    public AuthResponse login(@RequestBody LoginRequest request) {
        return authService.login(request);
    }
}