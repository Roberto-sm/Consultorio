package com.upsin.demo.models;

import jakarta.persistence.*;
import lombok.Data;

/**
 * Entidad que representa al paciente dentro del modelo de datos clínico.
 * Utiliza @MapsId para compartir la llave primaria exacta con la tabla de Usuarios,
 * optimizando el rendimiento de las consultas relacionales.
 */
@Data
@Entity
@Table(name = "pacientes")
public class Paciente {

    @Id
    private Integer id;

    @OneToOne
    @MapsId
    @JoinColumn(name = "id")
    private Usuario usuario;

    /** Relación Muchos a 1: El especialista principal asignado para dar seguimiento al caso. */
    @ManyToOne
    @JoinColumn(name = "id_psicologo")
    private Psicologo psicologo;

    /** Bandera financiera que bloquea la creación de nuevas citas si existe un adeudo previo. */
    @Column(name = "penalizacion_activa")
    private Boolean penalizacionActiva = false;
}