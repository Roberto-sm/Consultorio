package com.upsin.demo.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import java.util.List;

@Data
@Schema(description = "Data Transfer Object (DTO) diseñado para exponer perfiles clínicos de forma segura, ocultando credenciales y aplanando relaciones complejas.")
public class PsicologoDTO {

    @Schema(description = "Identificador único del especialista", example = "4")
    private Integer idPsicologo;

    @Schema(description = "Nombre completo extraído de la tabla de usuarios", example = "Dra. Ana López")
    private String nombre;

    @Schema(description = "Correo electrónico de contacto profesional", example = "ana.lopez@clinica.com")
    private String correo;

    @Schema(description = "Sexo del especialista", example = "Femenino")
    private String sexo;

    @Schema(description = "Años de experiencia profesional", example = "15")
    private Integer añosExperiencia;

    @Schema(description = "Cédula profesional validada", example = "CED-112233")
    private String cedula;

    @Schema(description = "Resumen profesional del especialista", example = "Especialista en terapia cognitivo-conductual...")
    private String resumen;

    @Schema(description = "URL de la fotografía de perfil", example = "https://bucket.com/foto.jpg")
    private String fotoUrl;

    @Schema(description = "Bandera de sistema para identificar médicos generales de la clínica", example = "true")
    private Boolean esDePlanta;

    @Schema(description = "Lista aplanada con los nombres de las especialidades médicas vinculadas", example = "[\"Terapia Cognitivo-Conductual\", \"Psicología Clínica\"]")
    private List<String> especialidades;
}