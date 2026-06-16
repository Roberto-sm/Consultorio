package com.upsin.demo.repositories;

import com.upsin.demo.models.NotaEvolucion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface NotaEvolucionRepository extends JpaRepository<NotaEvolucion, Integer> {

    // Trae todas las hojas de un expediente, ordenadas de la más nueva a la más vieja
    List<NotaEvolucion> findByHistorialClinicoIdOrderByFechaRegistroDesc(Integer idHistorial);

    // Para evitar que un psicólogo escriba dos notas para la misma sesión
    boolean existsByCitaId(Integer idCita);
}