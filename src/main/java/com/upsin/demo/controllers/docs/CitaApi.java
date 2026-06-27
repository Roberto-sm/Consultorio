package com.upsin.demo.controllers.docs;

import com.upsin.demo.dto.CitaDTO;
import com.upsin.demo.models.Cita;
import com.upsin.demo.utils.SwaggerConstants;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.parameters.RequestBody;

public interface CitaApi {

    @Operation(summary = "Agendar cita (Desde el perfil del Psicólogo)", description = "Permite al especialista agendar una cita directa a uno de sus pacientes. La cita nace automáticamente con estado 'confirmada' y devuelve un DTO ligero.")
    @RequestBody(
            description = "JSON con la fecha y hora de la cita", required = true,
            content = @Content(mediaType = "application/json", examples = @ExampleObject(name = "Ejemplo de Fecha", value = SwaggerConstants.EJEMPLO_FECHA_CITA))
    )
    CitaDTO agendarCitaPorPsicologo(Integer idPaciente, Cita nuevaCita);

    @Operation(summary = "Agendar Primera Sesión (Triaje)", description = "Petición del paciente para su primera evaluación. El sistema le asigna automáticamente al psicólogo de planta y valida que el horario esté libre de empalmes.")
    @RequestBody(
            description = "JSON con la fecha y hora de la cita", required = true,
            content = @Content(mediaType = "application/json", examples = @ExampleObject(name = "Ejemplo de Fecha", value = SwaggerConstants.EJEMPLO_FECHA_CITA))
    )
    CitaDTO agendarPrimeraCita(Cita cita);

    @Operation(summary = "Agendar Sesión de Seguimiento", description = "Petición del paciente regular. El sistema enruta automáticamente la solicitud al especialista que el paciente tiene asignado en su perfil.")
    @RequestBody(
            description = "JSON con la fecha y hora de la cita", required = true,
            content = @Content(mediaType = "application/json", examples = @ExampleObject(name = "Ejemplo de Fecha", value = SwaggerConstants.EJEMPLO_FECHA_CITA))
    )
    CitaDTO agendarCitaSeguimiento(Cita cita);
}