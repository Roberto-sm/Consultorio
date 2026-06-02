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

    // A qué paciente pertenece este registro
    @ManyToOne
    @JoinColumn(name = "id_paciente", nullable = false)
    private Paciente paciente;

    // Qué psicólogo escribió esta nota
    @ManyToOne
    @JoinColumn(name = "id_psicologo", nullable = false)
    private Psicologo psicologo;

    @Column(name = "fecha_hora", nullable = false)
    private LocalDateTime fechaHora;

    @Column(name = "notas_sesion", columnDefinition = "TEXT", nullable = false)
    private String notasSesion;
}