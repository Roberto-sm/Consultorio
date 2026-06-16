package com.upsin.demo.repositories;

import com.upsin.demo.models.HistorialClinico;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface HistorialClinicoRepository extends JpaRepository<HistorialClinico, Integer> {

    // Este método nos servirá más adelante para que el psicólogo busque el historial
    Optional<HistorialClinico> findByPacienteId(Integer pacienteId);
}