package com.upsin.demo.repositories;

import com.upsin.demo.models.Cita;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface CitaRepository extends JpaRepository<Cita, Integer> {

    // Spring Boot lee el nombre de este método y genera automáticamente
    // un "SELECT * FROM citas WHERE id_psicologo = ?"
    List<Cita> findByPsicologoId(Integer psicologoId);
}
