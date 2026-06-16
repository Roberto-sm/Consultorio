package com.upsin.demo.models;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;
import com.fasterxml.jackson.annotation.JsonIgnore;

@Data
@Entity
@Table(name = "notas_evolucion")
public class NotaEvolucion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    // Vinculamos esta nota a la "carpeta" principal del paciente
    @JsonIgnore
    @ManyToOne
    @JoinColumn(name = "id_historial", nullable = false)
    private HistorialClinico historialClinico;

    // Vinculamos esta nota a la cita específica donde ocurrió la sesión
    @JsonIgnore
    @OneToOne
    @JoinColumn(name = "id_cita", nullable = false, unique = true)
    private Cita cita;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String observaciones;

    @Column(columnDefinition = "TEXT")
    private String diagnostico;

    @Column(name = "plan_tratamiento", columnDefinition = "TEXT")
    private String planTratamiento;

    @Column(name = "fecha_registro", nullable = false, updatable = false)
    private LocalDateTime fechaRegistro;

    @PrePersist
    public void asignarFechaRegistro() {
        this.fechaRegistro = LocalDateTime.now();
    }
}