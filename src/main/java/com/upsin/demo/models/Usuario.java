package com.upsin.demo.models;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDate;

@Data
@Entity
@Table(name = "usuarios")
public class Usuario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    private String nombre;

    @Column(unique = true)
    private String correo;

    private String contraseña;

    private String rol; // paciente, psicologo, admin

    private String sexo;

    @Column(name = "fecha_nacimiento")
    private LocalDate fechaNacimiento;
}