package com.upsin.demo.repositories;

import com.upsin.demo.models.Psicologo;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface PsicologoRepository extends JpaRepository<Psicologo, Integer> {

    /** Localiza dinámicamente al psicólogo asignado a las evaluaciones iniciales (Triaje). */
    Optional<Psicologo> findFirstByEsDePlantaTrue();

    /** * Motor de búsqueda avanzado. Ejecuta un LIKE '%palabra%' ignorando mayúsculas,
     * haciendo un JOIN automático con la tabla de especialidades y devolviendo resultados paginados.
     */
    Page<Psicologo> findByEspecialidadesNombreContainingIgnoreCase(String nombreEspecialidad, Pageable pageable);
}