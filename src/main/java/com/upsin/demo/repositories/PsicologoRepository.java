package com.upsin.demo.repositories;

import com.upsin.demo.models.Psicologo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface PsicologoRepository extends JpaRepository<Psicologo, Integer> {
    Optional<Psicologo> findFirstByEsDePlantaTrue();
}