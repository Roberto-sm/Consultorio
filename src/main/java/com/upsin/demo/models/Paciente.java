package com.upsin.demo.models;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "pacientes")
public class Paciente {

    @Id
    private Integer id;

    // Relación 1 a 1 con Usuario (El paciente ES un usuario)
    @OneToOne
    @MapsId
    @JoinColumn(name = "id")
    private Usuario usuario;

    // Relación Muchos a 1 con Psicologo (Muchos pacientes pueden tener el mismo psicólogo)
    @ManyToOne
    @JoinColumn(name = "id_psicologo")
    private Psicologo psicologo;

    @Column(name = "penalizacion_activa")
    private Boolean penalizacionActiva = false;
}