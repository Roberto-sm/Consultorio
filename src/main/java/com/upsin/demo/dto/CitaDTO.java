package com.upsin.demo.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Schema(description = "Objeto de transferencia de datos para presentar citas de forma segura y ligera.")
public class CitaDTO {

    private Integer idCita;
    private LocalDateTime fechaHora;
    private String estado;
    private Boolean esPrimera;

    @Schema(description = "Nombre completo del paciente extraído de su relación con Usuario")
    private String nombrePaciente;

    @Schema(description = "Nombre completo del especialista")
    private String nombrePsicologo;
}