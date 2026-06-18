package com.upsin.demo.models;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Entidad principal para el control de la agenda clínica.
 * Funciona como una máquina de estados finita, permitiendo transiciones
 * de flujo estrictas (ej. Pendiente -> Confirmada -> Finalizada).
 */
@Data
@Entity
@Table(name = "citas")
public class Cita {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne
    @JoinColumn(name = "id_paciente", nullable = false)
    private Paciente paciente;

    @ManyToOne
    @JoinColumn(name = "id_psicologo", nullable = false)
    private Psicologo psicologo;

    @Column(name = "fecha_hora", nullable = false)
    private LocalDateTime fechaHora;

    /** Estado actual en la máquina de estados: pendiente, confirmada, cancelada, finalizada, rechazada o no_asistio. */
    @Column(nullable = false)
    private String estado;

    /** Bandera para el flujo clínico: Diferencia la cita inicial de triaje de las citas de seguimiento. */
    @Column(name = "es_primera", nullable = false)
    private Boolean esPrimera;

    @Column(name = "fecha_modificacion")
    private LocalDateTime fechaModificacion;

    /** Huella de auditoría administrativa para rastrear cancelaciones tardías. */
    @Column(name = "multa_aplicada", nullable = false)
    private Boolean multaAplicada = false;

    /**
     * Hook de ciclo de vida de JPA.
     * Actualiza automáticamente el timestamp cada vez que el registro sufre una mutación,
     * alimentando la validación del "Periodo de Gracia de 24 hrs".
     */
    @PrePersist
    @PreUpdate
    public void actualizarFechaModificacion() {
        this.fechaModificacion = LocalDateTime.now();
    }
}