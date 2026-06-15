package com.upsin.demo.models;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "historial_clinico")
public class HistorialClinico {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    // Relación 1 a 1: Un paciente tiene un solo historial, y un historial pertenece a un solo paciente.
    @OneToOne
    @JoinColumn(name = "id_paciente", nullable = false, unique = true)
    private Paciente paciente;

    @Column(name = "antecedentes_familiares", columnDefinition = "TEXT")
    private String antecedentesFamiliares;

    @Column(name = "antecedentes_medicos", columnDefinition = "TEXT")
    private String antecedentesMedicos;

    @Column(name = "fecha_creacion", nullable = false, updatable = false)
    private LocalDateTime fechaCreacion;

    @PrePersist
    public void asignarFechaCreacion() {
        this.fechaCreacion = LocalDateTime.now();
    }
}