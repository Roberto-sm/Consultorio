package com.upsin.demo.models;

import jakarta.persistence.*; // libreria para interactuar con BD´s en java JPA
import lombok.Data;

@Data //  genera getters, setters, toString y constructores automáticamente
@Entity // le indica a Spring Boot que esta clase representa una tabla en la base de datos
@Table(name = "especialidades") // nombre de la tabla en MySQL
public class Especialidad {

    @Id // Indica que esta variable es la Primary Key
    @GeneratedValue(strategy = GenerationType.IDENTITY) // Equivale a AUTO_INCREMENT en MySQL
    private Integer id;

    @Column(nullable = false, unique = true, length = 100)
    private String nombre;

    @Column(columnDefinition = "TEXT")
    private String descripcion;
}