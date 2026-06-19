package com.upsin.demo.models;

import jakarta.persistence.*;
import lombok.Data;
import java.util.List;

/**
 * Entidad que modela el perfil profesional del personal médico.
 * Implementa relaciones complejas en JPA, incluyendo un JoinTable para mapear
 * las especialidades de forma eficiente.
 */
@Data
@Entity
@Table(name = "psicologos")
public class Psicologo {

    @Id
    private Integer id;

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

    /** Relación Muchos a Muchos gestionada a través de la tabla pivote 'psicologo_especialidad'. */
    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
            name = "psicologo_especialidad",
            joinColumns = @JoinColumn(name = "id_psicologo"),
            inverseJoinColumns = @JoinColumn(name = "id_especialidad")
    )
    private java.util.Set<Especialidad> especialidades;
}