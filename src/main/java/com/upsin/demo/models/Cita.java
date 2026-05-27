package com.upsin.demo.models;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDate;
import java.time.LocalTime;

@Data
@Entity
@Table(name = "citas")
public class Cita {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    // Una cita pertenece a un paciente
    @ManyToOne
    @JoinColumn(name = "id_paciente", nullable = false)
    private Paciente paciente;

    // Una cita pertenece a un psicólogo
    @ManyToOne
    @JoinColumn(name = "id_psicologo", nullable = false)
    private Psicologo psicologo;

    @Column(nullable = false)
    private LocalDate fecha;

    // El estado (pendiente, completada, cancelada, etc.)
    private String estado;

    // Para saber si es la primera cita (triaje)
    @Column(name = "es_primera")
    private Boolean esPrimera;
}