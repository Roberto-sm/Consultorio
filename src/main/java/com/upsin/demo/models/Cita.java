package com.upsin.demo.models;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDate;
import java.time.LocalDateTime;

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

    @Column(name = "fecha_hora", nullable = false)
    private LocalDateTime fechaHora;

    @Column(nullable = false)
    private String estado;     // El estado (pendiente, completada, cancelada, etc.)

    // Para saber si es la primera cita (triaje)
    @Column(name = "es_primera", nullable = false)
    private Boolean esPrimera;

    @Column(name = "fecha_modificacion")
    private LocalDateTime fechaModificacion;

    // actualizan la fecha antes de guardar en MySQL
    @PrePersist
    @PreUpdate
    public void actualizarFechaModificacion() {
        this.fechaModificacion = LocalDateTime.now();
    }
}