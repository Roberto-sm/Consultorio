package com.upsin.demo.repositories;

import com.upsin.demo.models.Especialidad;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface EspecialidadRepository extends JpaRepository<Especialidad, Integer> {
    // Por si necesitas buscar una especialidad exacta por su nombre en el futuro
    Optional<Especialidad> findByNombre(String nombre);
}