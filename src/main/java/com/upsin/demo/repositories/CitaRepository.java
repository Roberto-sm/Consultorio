package com.upsin.demo.repositories;

import com.upsin.demo.models.Cita;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface CitaRepository extends JpaRepository<Cita, Integer> {

    Page<Cita> findByPsicologoIdOrderByFechaHoraDesc(Integer psicologoId, Pageable pageable);

    Page<Cita> findByPsicologoIdAndEstadoInOrderByFechaHoraAsc(Integer psicologoId, List<String> estados, Pageable pageable);

    Optional<Cita> findFirstByPacienteIdAndEstado(Integer pacienteId, String estado);

    /** Regla de concurrencia: Verifica si un paciente ya tiene una cita activa en espera. */
    boolean existsByPacienteIdAndEstado(Integer pacienteId, String estado);

    /** Regla de empalme: Evita que el calendario del psicólogo sufra colisiones (Double Booking). */
    boolean existsByPsicologoIdAndFechaHoraAndEstado(Integer psicologoId, LocalDateTime fechaHora, String estado);
}