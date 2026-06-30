# Guía de Contribución — API Consultorio Psicológico

Gracias por tu interés en este proyecto. Este documento cubre todo lo que necesitas saber para entender la arquitectura, correr el entorno local, y contribuir de forma ordenada.

---

## Tabla de Contenidos

1. [Contexto del Proyecto](#1-contexto-del-proyecto)
2. [Stack y Decisiones de Arquitectura](#2-stack-y-decisiones-de-arquitectura)
3. [Instalación Local](#3-instalación-local)
4. [Datos de Prueba (Sandbox)](#4-datos-de-prueba-sandbox)
5. [Reglas de Negocio Clave](#5-reglas-de-negocio-clave)
6. [Estructura del Proyecto](#6-estructura-del-proyecto)
7. [Convenciones de Código](#7-convenciones-de-código)
8. [Cómo Contribuir](#8-cómo-contribuir)

---

## 1. Contexto del Proyecto

Este sistema nació para digitalizar la gestión operativa de un departamento de psicología en Mazatlán que llevaba su control de citas, pacientes y expedientes en papel. El diseño visual original buscaba replicar el formato de los archivos físicos para facilitar la adopción del sector con menor afinidad tecnológica.

La API cubre el flujo completo: autenticación por roles, agendamiento con validación de concurrencia, expedientes clínicos con privacidad por especialista, penalizaciones automáticas y auditoría delegada a la base de datos.

---

## 2. Stack y Decisiones de Arquitectura

| Capa | Tecnología |
|---|---|
| Backend | Java 17, Spring Boot 3 |
| Seguridad | Spring Security + JWT |
| Base de Datos | MySQL 8.4 |
| Documentación | Springdoc OpenAPI (Swagger UI) |
| Despliegue | Railway |
| Build | Maven + Lombok |

### Decisiones relevantes

**¿Por qué los triggers están en la BD y no en Java?**
La auditoría de cambios en citas (`auditoria_citas`) y transferencias de pacientes (`auditoria_pacientes`) se delega a MySQL mediante triggers. Esto garantiza trazabilidad incluso si un cambio llega por fuera de la capa de aplicación, y mantiene los servicios de Java enfocados en lógica de negocio.

**¿Por qué el patrón Interface-Controller en Swagger?**
Las anotaciones de OpenAPI están separadas en interfaces (`controllers/docs/`). Los Controllers solo contienen lógica, sin ruido visual de documentación. Si modificas un endpoint, actualiza ambos archivos.

**Zona horaria del sistema**
Todas las validaciones temporales (concurrencia, penalizaciones, expiración de citas) operan bajo `America/Mazatlan`. Esto está configurado en `application.properties` y debe respetarse en cualquier lógica nueva que involucre fechas.

**Cron Job de limpieza**
Un proceso automatizado escanea periódicamente citas en estado `pendiente` cuya `fecha_hora` ya expiró y las muta a `rechazada`. Las citas `confirmadas` expiradas no son tocadas por este proceso; requieren acción manual del psicólogo (`finalizada` o `no-show`).

---

## 3. Instalación Local

### Prerrequisitos

- Java 17+
- Maven 3.8+
- MySQL 8.4 corriendo localmente

### Pasos

```bash
# 1. Clonar el repositorio
git clone https://github.com/TuUsuario/TuRepositorio.git
cd TuRepositorio
```

```bash
# 2. Crear la base de datos
# En tu cliente MySQL:
CREATE DATABASE gestor_psicologico;
```

```properties
# 3. Configurar credenciales en:
# src/main/resources/application.properties

spring.datasource.url=jdbc:mysql://localhost:3306/gestor_psicologico?serverTimezone=America/Mazatlan
spring.datasource.username=TU_USUARIO
spring.datasource.password=TU_CONTRASEÑA
```

```bash
# 4. Ejecutar los scripts SQL (obligatorio para auditoría completa)
# Corre database/schema.sql y luego database/Triggers.sql en tu cliente MySQL
```

```bash
# 5. Compilar y correr
mvn clean install
mvn spring-boot:run
```

El servidor inicia en `http://localhost:8080`. Si la base de datos está vacía, el `DatabaseSeeder` inyecta automáticamente los datos de demostración al arrancar.

> **Nota:** Si omites el paso 4, la API funciona pero las tablas `auditoria_citas` y `auditoria_pacientes` no registrarán cambios.

---

## 4. Datos de Prueba (Sandbox)

El seeder crea un entorno completo listo para pruebas. Contraseña universal: `password123`.

| Usuario | Correo | Rol | Estado / Propósito |
|---|---|---|---|
| Dr. Tenma Kenzo | `tenma@email.com` | Psicólogo | Tiene pacientes asignados y citas por finalizar |
| Dr. Senku Stone | `senku@email.com` | Psicólogo | Para verificar aislamiento de expedientes (no puede ver pacientes de Tenma) |
| Reigen Arataka | `reigen@email.com` | Paciente | Tiene cita pendiente con el psicólogo de planta |
| Kim Wexler | `kim@email.com` | Paciente | Penalización activa — prueba el bloqueo de agendamiento |
| Justo Bolsa | `justo@email.com` | Paciente | Fue derivado a otro psicólogo; útil para revisar `auditoria_pacientes` |

Todos los endpoints `POST`/`PUT` en Swagger incluyen ejemplos de Request Body listos para usar con **Try it out**.

---

## 5. Reglas de Negocio Clave

Antes de modificar cualquier lógica de citas o expedientes, familiarízate con estas restricciones. La especificación completa está en [`bussines_rules.md`](./BUSINESS_RULES.md).

### Agendamiento
- Duración fija de **50 minutos**, solo en punto (09:00, 10:00… no 10:13).
- Horario operativo: **lunes a viernes, 09:00–18:00**. Bloqueado de 14:00 a 15:00.
- Mínimo **24 horas de anticipación**. No se permiten citas el mismo día.
- Máximo **1 cita por hora por psicólogo** (sin solapamientos).

### Máquina de estados de citas

```
pendiente ──► confirmada ──► finalizada
    │               │
    ▼               ▼
rechazada       cancelada / no-show
```

Las transiciones tienen condiciones estrictas de tiempo y actor. Consulta la tabla completa en `BUSINESS_RULES.md §3`.

### Penalizaciones
Un `no-show` o una cancelación con menos de 20 horas de anticipación activa `penalizacionActiva = true` en el paciente, bloqueando nuevas citas hasta que se resuelva.

### Privacidad clínica
Un psicólogo solo puede acceder a los expedientes de sus propios pacientes. Cualquier intento de cruzar esa barrera retorna `403 Forbidden`.

---

## 6. Estructura del Proyecto

```
src/main/java/com.upsin.demo/
├── config/           # Seguridad global, filtros JWT, DatabaseSeeder
├── controllers/      # Controladores REST (lógica únicamente)
│   └── docs/         # Interfaces con anotaciones OpenAPI (contratos Swagger)
├── dtos/             # Objetos de transferencia de datos (request/response)
├── models/           # Entidades JPA
├── repositories/     # Interfaces Spring Data JPA
├── services/         # Lógica de negocio y validaciones
└── utils/            # Constantes y utilidades (JSONs de ejemplo para Swagger)

database/
├── schema.sql        # DDL de tablas
├── Triggers.sql      # Triggers de auditoría 
└── Script de limpieza.sql 
```

---

## 7. Convenciones de Código

- **Idioma:** lógica, variables, comentarios y mensajes de error en español (consistencia con el dominio).
- **DTOs:** un DTO por operación si los campos de entrada y salida difieren significativamente.
- **Validaciones:** las reglas de negocio viven en la capa `services/`.
- **Swagger:** si agregas o modificas un endpoint, actualiza la interfaz correspondiente en `controllers/docs/`.
- **Triggers:** cualquier nueva necesidad de auditoría a nivel BD debe agregarse en `Triggers.sql` y documentarse en `BUSINESS_RULES.md`.

---

## 8. Cómo Contribuir

1. Haz fork del repositorio y crea una rama descriptiva:
   ```bash
   git checkout -b fix/validacion-cancelacion-anticipacion
   ```

2. Asegúrate de que tu cambio no rompe ninguna regla de negocio existente en `BUSINESS_RULES.md`. Si la regla cambia, actualiza el documento.

3. Si modificas lógica de citas, verifica manualmente los edge cases de expiración (BR-EDG-01 al 03) y el comportamiento del Cron Job.

4. Abre un Pull Request con:
   - Descripción del problema que resuelve.
   - Referencia a la regla de negocio afectada (ej. `BR-CIT-04`).
   - Evidencia de prueba (captura de Swagger o log relevante).

---

*Para dudas sobre el dominio clínico o las reglas de negocio, abre un Issue con la etiqueta `question`.*