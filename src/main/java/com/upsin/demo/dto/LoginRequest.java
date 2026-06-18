package com.upsin.demo.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "Data Transfer Object (DTO) para recibir credenciales de acceso.")
public class LoginRequest {

    @Schema(description = "Correo electrónico registrado en base de datos", example = "paciente@upsin.edu.mx")
    private String correo;

    @Schema(description = "Contraseña en texto plano para ser validada contra el hash de BCrypt", example = "password123")
    private String contraseña;
}