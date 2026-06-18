package com.upsin.demo.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "Data Transfer Object (DTO) que encapsula la respuesta tras un login exitoso.")
public class AuthResponse {

    @Schema(description = "Mensaje de estado del sistema", example = "Login exitoso")
    private String mensaje;

    @Schema(description = "Token criptográfico para consumir endpoints privados", example = "eyJhbGciOiJIUzI1NiIsInR...")
    private String token;

    @Schema(description = "Rol de autorización detectado por el sistema", example = "psicologo")
    private String rol;

    @Schema(description = "Llave primaria del usuario autenticado", example = "1")
    private Integer idUsuario;
}