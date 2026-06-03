package com.upsin.demo.repositories;

import com.upsin.demo.models.Cita;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface CitaRepository extends JpaRepository<Cita, Integer> {

    // Spring Boot lee el nombre de este método y genera automáticamente
    // un "SELECT * FROM citas WHERE id_psicologo = ?"
    List<Cita> findByPsicologoId(Integer psicologoId);

    // NUEVA CONSULTA: Busca la cita activa de un paciente específico
    Optional<Cita> findFirstByPacienteIdAndEstado(Integer pacienteId, String estado);
}
