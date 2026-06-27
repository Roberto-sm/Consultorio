package com.upsin.demo.controllers.docs;

import com.upsin.demo.dto.PsicologoDTO;
import com.upsin.demo.models.Psicologo;
import com.upsin.demo.utils.SwaggerConstants;
import com.upsin.demo.controllers.docs.NotaEvolucionApi;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.parameters.RequestBody;

public interface PsicologoApi {

    @Operation(summary = "Actualizar el perfil del psicologo logueado", description = "Endpoint protegido para que un psicólogo pueda editar su currículum, años de experiencia y cédula basándose en su Token de acceso. Devuelve el perfil actualizado en formato DTO plano.")
    @RequestBody(
            description = "JSON con los datos profesionales a actualizar",
            required = true,
            content = @Content(
                    mediaType = "application/json",
                    examples = @ExampleObject(
                            name = "Ejemplo de Perfil", value = SwaggerConstants.EJEMPLO_ACTUALIZAR_PERFIL
                    )
            )
    )
    PsicologoDTO actualizarPerfil(Psicologo datosActualizados);
}