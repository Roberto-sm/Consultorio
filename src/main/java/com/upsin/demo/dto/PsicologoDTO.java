package com.upsin.demo.dto;

import lombok.Data;
import java.util.List;

@Data
public class PsicologoDTO {
    private Integer idPsicologo;
    private String nombre;
    private String correo;
    private Boolean esDePlanta;
    private List<String> especialidades;
}