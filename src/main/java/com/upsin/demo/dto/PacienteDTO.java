package com.upsin.demo.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "DTO ligero que representa la asignación actual de un paciente, evitando la exposición del expediente completo.")
public class PacienteDTO {

    private Integer idPaciente;
    private String nombrePaciente;
    private Boolean penalizacionActiva;

    @Schema(description = "ID del especialista actual responsable del caso")
    private Integer idPsicologoAsignado;

    @Schema(description = "Nombre del especialista actual")
    private String nombrePsicologoAsignado;
}