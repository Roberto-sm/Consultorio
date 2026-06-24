package com.upsin.demo.controllers.docs;

import com.upsin.demo.models.Usuario;
import com.upsin.demo.utils.SwaggerConstants;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "Autenticación", description = "Endpoints para registro y acceso a la plataforma")
public interface AuthApi {

    @Operation(summary = "Registrar un nuevo Paciente", description = "Crea un usuario paciente, encripta su contraseña y le asigna psicólogo.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Paciente registrado exitosamente"),
            @ApiResponse(responseCode = "400", description = "Datos inválidos")
    })
    @io.swagger.v3.oas.annotations.parameters.RequestBody(
            description = "JSON con los datos del paciente",
            required = true,
            content = @Content(
                    mediaType = "application/json",
                    examples = @ExampleObject(
                            name = "Ejemplo", value = SwaggerConstants.EJEMPLO_REGISTRO_PACIENTE
                    )
            )
    )
        // Solo declaramos el método, no le ponemos llaves {}
    Usuario registrarPaciente(Usuario usuario);
}