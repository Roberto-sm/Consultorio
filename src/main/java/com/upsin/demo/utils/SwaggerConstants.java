package com.upsin.demo.utils;

public class SwaggerConstants {

    // Centralizamos los JSON de ejemplo aquí para no ensuciar los controladores
    public static final String EJEMPLO_REGISTRO_PACIENTE = "{\n" +
            "  \"nombre\": \"Juan Perez\",\n" +
            "  \"correo\": \"jp@email.com\",\n" +
            "  \"contraseña\": \"password123\",\n" +
            "  \"sexo\": \"Masculino\",\n" +
            "  \"fechaNacimiento\": \"2005-09-14\"\n" +
            "}";

    public static final String EJEMPLO_LOGIN = "{\n" +
            "  \"correo\": \"tenma@email.com\",\n" +
            "  \"contraseña\": \"password123\"\n" +
            "}";

    // Puedes seguir agregando más ejemplos aquí en el futuro...
}