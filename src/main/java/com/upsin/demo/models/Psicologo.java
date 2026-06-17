package com.upsin.demo.models;

import jakarta.persistence.*;
import lombok.Data;
import java.util.List;

@Data
@Entity
@Table(name = "psicologos")
public class Psicologo {

    @Id
    private Integer id;

    // 1. Relación 1 a 1 con la tabla Usuarios compartiendo el mismo ID
    @OneToOne
    @MapsId
    @JoinColumn(name = "id")
    private Usuario usuario;

    @Column(name = "años_experiencia")
    private Integer añosExperiencia;

    @Column(columnDefinition = "TEXT")
    private String resumen;

    @Column(name = "foto_url", columnDefinition = "TEXT")
    private String fotoUrl;

    private String cedula;

    @Column(name = "es_de_planta")
    private Boolean esDePlanta;

    // 2. Relación Muchos a Muchos con las Especialidades
    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
            name = "psicologo_especialidad", // La tabla intermedia que creaste
            joinColumns = @JoinColumn(name = "id_psicologo"),
            inverseJoinColumns = @JoinColumn(name = "id_especialidad")
    )
    private java.util.Set<Especialidad> especialidades;
}