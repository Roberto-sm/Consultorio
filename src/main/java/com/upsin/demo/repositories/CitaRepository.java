package com.upsin.demo.repositories;

import com.upsin.demo.models.Cita;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface CitaRepository extends JpaRepository<Cita, Integer> {

    List<Cita> findByPsicologoId(Integer psicologoId);

    Optional<Cita> findFirstByPacienteIdAndEstado(Integer pacienteId, String estado);

    /** Regla de concurrencia: Verifica si un paciente ya tiene una cita activa en espera. */
    boolean existsByPacienteIdAndEstado(Integer pacienteId, String estado);

    /** Regla de empalme: Evita que el calendario del psicólogo sufra colisiones (Double Booking). */
    boolean existsByPsicologoIdAndFechaHoraAndEstado(Integer psicologoId, LocalDateTime fechaHora, String estado);
}