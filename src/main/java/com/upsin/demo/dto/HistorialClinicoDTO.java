package com.upsin.demo.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Schema(description = "DTO ligero para visualizar el expediente clínico sin exponer relaciones anidadas o credenciales.")
public class HistorialClinicoDTO {

    private Integer idHistorial;
    private String antecedentesFamiliares;
    private String antecedentesMedicos;
    private LocalDateTime fechaCreacion;

    // Datos planos del paciente
    private Integer idPaciente;
    private String nombrePaciente;
}