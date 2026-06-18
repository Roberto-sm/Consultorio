package com.upsin.demo.repositories;

import com.upsin.demo.models.NotaEvolucion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface NotaEvolucionRepository extends JpaRepository<NotaEvolucion, Integer> {

    /** * Extrae todas las hojas de evolución de un paciente ordenadas cronológicamente
     * de la más reciente a la más antigua (Técnica LIFO de lectura de expedientes).
     */
    List<NotaEvolucion> findByHistorialClinicoIdOrderByFechaRegistroDesc(Integer idHistorial);

    /** Restricción de negocio: Asegura que no se redacte más de una nota por sesión. */
    boolean existsByCitaId(Integer idCita);
}