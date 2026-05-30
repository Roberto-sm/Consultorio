package com.upsin.demo.repositories;

import com.upsin.demo.models.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface UsuarioRepository extends JpaRepository<Usuario, Integer> {
    // Spring Boot construye automáticamente el "SELECT * FROM usuarios WHERE correo = ?"
    Optional<Usuario> findByCorreo(String correo);
}
