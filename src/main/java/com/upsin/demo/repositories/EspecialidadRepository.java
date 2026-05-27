package com.upsin.demo.repositories;

import com.upsin.demo.models.Especialidad;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface EspecialidadRepository extends JpaRepository<Especialidad, Integer> {
    // Al heredar de JpaRepository, Spring Boot ya genera automáticamente
    // métodos como findAll(), findById(), save() y deleteById() para ti.
}