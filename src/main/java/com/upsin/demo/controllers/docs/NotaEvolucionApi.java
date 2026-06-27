package com.upsin.demo.controllers.docs;

import com.upsin.demo.models.NotaEvolucion;
import com.upsin.demo.utils.SwaggerConstants;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.parameters.RequestBody;

public interface NotaEvolucionApi {

    @Operation(summary = "Capturar hoja de evolución", description = "Redacta una nota clínica vinculándola permanentemente a la sesión (cita) que acaba de concluir. Bloquea duplicados automáticamente.")
    @RequestBody(
            description = "JSON con las observaciones, diagnóstico y plan de tratamiento",
            required = true,
            content = @Content(
                    mediaType = "application/json",
                    examples = @ExampleObject(
                            name = "Ejemplo Clínico Profesional", value = SwaggerConstants.EJEMPLO_NOTA_EVOLUCION
                    )
            )
    )
    NotaEvolucion crearNota(Integer idCita, NotaEvolucion nuevaNota);
}