package com.upsin.demo.utils;

public class SwaggerConstants {

    // Centralizamos los JSON de ejemplo aquí para no ensuciar los controladores
        public static final String EJEMPLO_REGISTRO = "{\n" +
            "  \"nombre\": \"Juan Perez\",\n" +
            "  \"correo\": \"jp@email.com\",\n" +
            "  \"contraseña\": \"password123\",\n" +
            "  \"sexo\": \"Masculino\",\n" +
            "  \"fechaNacimiento\": \"2005-09-14\"\n" +
            "}";

    public static final String EJEMPLO_REGISTRO_PSICOLOGO = "{\n" +
            "  \"nombre\": \"Dr. Hiruluk\",\n" +
            "  \"correo\": \"hiruluk@email.com\",\n" +
            "  \"contraseña\": \"password123\",\n" +
            "  \"sexo\": \"Masculino\",\n" +
            "  \"fechaNacimiento\": \"1962-07-20\"\n" +
            "}";

    // 3. Directorio Médico
    public static final String EJEMPLO_ACTUALIZAR_PERFIL = "{\n" +
            "  \"añosExperiencia\": 5,\n" +
            "  \"resumen\": \"Especialista en terapia adolescente.\"\n" +
            "}";

    // 4. Gestión de Citas (Sirve para los 3 endpoints de citas ya que todos piden lo mismo)
    public static final String EJEMPLO_FECHA_CITA = "{\n" +
            "  \"fechaHora\": \"2026-08-24T10:00:00\"\n" +
            "}";

    // 5. Notas de Evolución
    public static final String EJEMPLO_NOTA_EVOLUCION = "{\n" +
            "  \"observaciones\": \"El paciente manifestó sentirse sobrecargado por responsabilidades laborales. Se exploraron factores desencadenantes del estrés y se establecieron prioridades para mejorar la organización de actividades diarias.\",\n" +
            "  \"diagnostico\": \"Estrés laboral con síntomas de ansiedad situacional.\",\n" +
            "  \"planTratamiento\": \"Implementar una agenda semanal de actividades, establecer pausas programadas durante la jornada laboral y practicar ejercicios de respiración consciente dos veces al día.\"\n" +
            "}";

    // 7. Expediente Clínico
    public static final String EJEMPLO_ACTUALIZAR_EXPEDIENTE = "{\n" +
            "  \"antecedentesMedicos\": \"El paciente reporta sindrome del impostor. No toma medicamentos actualmente.\",\n" +
            "  \"antecedentesFamiliares\": \"Padre con historial de adicciones.\"\n" +
            "}";
}