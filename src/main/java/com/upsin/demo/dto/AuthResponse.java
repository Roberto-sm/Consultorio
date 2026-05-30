package com.upsin.demo.dto;

import lombok.Data;

@Data
public class AuthResponse {
    private String mensaje;
    private String token;
    private String rol;
    private Integer idUsuario;
}
