package com.upsin.demo.repositories;

import com.upsin.demo.models.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

/**
 * Repositorio de acceso a datos para la entidad Usuario.
 */
@Repository
public interface UsuarioRepository extends JpaRepository<Usuario, Integer> {

    /**
     * Busca a un usuario específico mediante su correo electrónico.
     * Utilizado principalmente durante el proceso de autenticación (Login).
     * * @param correo Correo electrónico exacto a buscar.
     * @return Un contenedor Optional con el Usuario si existe, o vacío si no se encuentra.
     */
    Optional<Usuario> findByCorreo(String correo);
}