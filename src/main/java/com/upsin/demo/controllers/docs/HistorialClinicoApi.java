package com.upsin.demo.controllers.docs;

import com.upsin.demo.dto.HistorialClinicoDTO;
import com.upsin.demo.models.HistorialClinico;
import com.upsin.demo.utils.SwaggerConstants;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.parameters.RequestBody;

public interface HistorialClinicoApi {

    @Operation(summary = "Actualizar triaje y antecedentes", description = "Modifica los campos de antecedentes médicos y familiares. Protege la inmutabilidad de la fecha de creación original.")
    @RequestBody(
            description = "JSON con los nuevos antecedentes",
            required = true,
            content = @Content(
                    mediaType = "application/json",
                    examples = @ExampleObject(
                            name = "Ejemplo de Antecedentes", value = SwaggerConstants.EJEMPLO_ACTUALIZAR_EXPEDIENTE
                    )
            )
    )
    HistorialClinicoDTO actualizarAntecedentes(Integer pacienteId, HistorialClinico datosActualizados);
}