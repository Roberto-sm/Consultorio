package com.upsin.demo.models;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;
import java.time.LocalDate;

/**
 * Entidad base que representa a todos los usuarios del sistema.
 * Utiliza validaciones de Spring Boot Starter Validation para blindar la base de datos
 * contra registros nulos o mal formados.
 */
@Data
@Entity
@Table(name = "usuarios")
public class Usuario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @NotBlank(message = "El nombre es obligatorio")
    @Column(nullable = false)
    private String nombre;

    @NotBlank(message = "El correo es obligatorio")
    @Email(message = "Debe tener un formato válido de correo electrónico")
    @Column(nullable = false, unique = true)
    private String correo;

    @NotBlank(message = "La contraseña es obligatoria")
    @Size(min = 6, message = "La contraseña debe tener al menos 6 caracteres por seguridad")
    @Column(nullable = false)
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private String contraseña;

    /** Rol de autorización en el sistema (ej. paciente, psicologo, admin). Asignado internamente por el sistema. */
    private String rol;

    private String sexo;

    @Column(name = "fecha_nacimiento")
    private LocalDate fechaNacimiento;
}