package com.upsin.demo.models;

import jakarta.persistence.*;
import lombok.Data;

/**
 * Entidad catálogo que representa las áreas de especialización de la clínica.
 * Diseñada para relacionarse de forma Many-To-Many con los Psicólogos del sistema.
 */
@Data
@Entity
@Table(name = "especialidades")
public class Especialidad {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false, unique = true)
    private String nombre;

    @Column(columnDefinition = "TEXT")
    private String descripcion;
}