package com.upsin.demo.repositories;

import com.upsin.demo.models.HistorialClinico;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface HistorialClinicoRepository extends JpaRepository<HistorialClinico, Integer> {

    /**
     * Localiza la carpeta principal del expediente utilizando la llave foránea del paciente.
     */
    Optional<HistorialClinico> findByPacienteId(Integer pacienteId);
}