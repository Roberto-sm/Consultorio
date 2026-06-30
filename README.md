# API REST — Sistema de Gestión de Citas Psicológicas

Una API RESTful construida con Java 17 y Spring Boot 3 para gestionar el flujo operativo completo de un departamento psicológico: roles de usuario, agendamiento con validación de concurrencia, historiales clínicos con privacidad por especialista, y un sistema automatizado de penalizaciones y auditoría.

Desarrollada con enfoque en Arquitectura Limpia, Seguridad (JWT) y Delegación de Responsabilidades a la Base de Datos (Triggers).
🔗 [Documentación Swagger](https://consultorio-production-2605.up.railway.app/swagger-ui/index.html#/)

## ¿Por qué este proyecto?

Este sistema nació para digitalizar la gestión operativa de un departamento de psicología en Mazatlán que llevaba su control de citas, pacientes y expedientes en papel. El diseño visual original buscaba replicar el formato de los archivos físicos para facilitar la adopción del sector con menor afinidad tecnológica.

La API cubre el flujo completo: autenticación por roles, agendamiento con validación de concurrencia, expedientes clínicos con privacidad por especialista, penalizaciones automáticas y auditoría delegada a la base de datos.

## Características Principales

```
- 🔐 **Autenticación y Seguridad** — JWT con rutas protegidas por rol (`PACIENTE` / `PSICOLOGO`).
- ⚙️ **Máquina de Estados de Citas** — Transiciones estrictas (`pendiente → confirmada → finalizada / cancelada / no-show / rechazada`) con validaciones temporales bajo zona horaria `America/Mazatlan`.
- ⚖️ **Sistema de Penalizaciones** — Las inasistencias (`no-show`) y cancelaciones tardías restringen automáticamente el agendamiento del paciente.
- 🕒 **Cron Job de Mantenimiento** — Proceso automatizado que rechaza citas pendientes expiradas sin intervención manual.
- 🗄️ **Auditoría Nativa en MySQL** — Triggers que rastrean cambios de estado en citas y transferencias de pacientes entre psicólogos.
- 🧪 **Entorno Sandbox Automático** — `DatabaseSeeder` que inyecta un kit de demostración completo si la base de datos está vacía.
- 📄 **Documentación OpenAPI Desacoplada** — Contratos de Swagger separados de los controllers (patrón Interface-Controller), manteniendo la lógica de negocio libre de ruido visual.
```
## Diagrama de Estados

![Grafo de maquina de estados](docs/images/Estados%20Citas.png)

## Pruebas y Endpoints (Live Demo)

🔗 [Documentación Swagger](https://consultorio-production-2605.up.railway.app/swagger-ui/index.html#/)

Incluye usuarios de prueba listos para explorar los distintos flujos (penalización activa, derivación entre psicólogos, citas por finalizar, etc.). Ver detalles de uso en  [business_rules.md](./docs/business_rules.md).

Usuarios: 

| Correo | Contraseña | Rol | Condicion |
|---|---|---|---|
| `tenma@email.com` | `password123` | Psicólogo | Es el psicologo de planta, tiene citas para finalizar |
| `senku@email.com` | `password123` | Psicólogo | Psicologo especialista, es posible derivar pacientes hacia él |
| `reigen@email.com` | `password123` | Paciente | Pertenece al psicologo de planta, tiene una cita pendiente asignada |
| `kim@email.com` | `password123` | Paciente | Tiene una penalización activa en su perfil para probar el bloqueo de sistema |
| `justo@email.com` | `password123` | Paciente | Pertenece al psicologo especialista, se puede ver su transferencia en auditoria_pacientes |

## Stack Tecnológico

Java 17 · Spring Boot 3 (Web, Data JPA, Security, Validation) · MySQL 8.4 · Springdoc OpenAPI · Railway · Maven · Lombok

## Estructura del Proyecto

| Estructura de Directorios y Archivos | Contenido y Responsabilidad |
| :--- | :--- |
| 📁 **src/main/java/com.upsin.demo/** | |
| ├── 🛡️ `config/` | Seguridad global, filtros JWT, DatabaseSeeder |
| ├── 🌐 `controllers/` | Controladores REST (lógica únicamente) |
| │   └── 📄 `docs/` | Interfaces con anotaciones OpenAPI (contratos Swagger) |
| ├── 📦 `dtos/` | Objetos de transferencia de datos (request/response) |
| ├── 🗃️ `models/` | Entidades JPA |
| ├── 💾 `repositories/` | Interfaces Spring Data JPA |
| ├── 🧠 `services/` | Lógica de negocio y validaciones |
| └── 🛠️ `utils/` | Constantes y utilidades (JSONs de ejemplo para Swagger) |
| | |
| 🗄️ **database/** | |
| ├── 📜 `schema.sql` | DDL de tablas |
| ├── ⚡ `Triggers.sql` | Triggers de auditoría |
| └── 🧹 `Script de limpieza.sql` | |


## Instalación Rápida

```bash
git clone https://github.com/TuUsuario/TuRepositorio.git
cd TuRepositorio
mvn clean install
mvn spring-boot:run
```

Requiere MySQL 8.4 corriendo localmente y la base `gestor_psicologico` creada. Guía completa de configuración, ejecución de triggers y variables de entorno en [contributing.md](./docs/contributing.md#3-instalación-local).

## Documentación Adicional

- 📋 [Reglas de Negocio](./docs/business_rules.md) — especificación completa de validaciones, máquina de estados y casos límite.
- 🤝 [Guía de Contribución](./docs/contributing.md) — instalación detallada, convenciones de código y flujo para Pull Requests.

## Autor

Elaborado por:
```
Jesús Roberto Sandoval Martínez - Estudiante de Ing. TI
```

Desarrollado como proyecto de portafolio para demostrar capacidades en desarrollo Backend, diseño de APIs seguras y manejo avanzado de bases de datos relacionales.
