package com.upsin.demo.repositories;

import com.upsin.demo.models.HistorialClinico;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface HistorialClinicoRepository extends JpaRepository<HistorialClinico, Integer> {

    // Obtiene todas las notas clínicas de un paciente en específico
    List<HistorialClinico> findByPacienteIdOrderByFechaHoraDesc(Integer pacienteId);
}