# Especificación de Reglas de Negocio y Flujos Clínicos

Este documento define la lógica de dominio, restricciones temporales, control de concurrencia y la máquina de estados que gobierna la API del Consultorio Psicológico. Estas reglas garantizan la integridad de los datos clínicos, previenen conflictos de agenda y automatizan la gestión administrativa de la clínica. Para mas informacion acerca del proyecto vease tambien [contributing.md](./docs/contributing.md#3-instalación-local).

## Tabla de Contenidos

1. [Módulo de Autenticación y Seguridad](#1-módulo-de-autenticación-y-seguridad)
2. [Módulo de Directorio Médico](#2-módulo-de-directorio-médico)
3. [Módulo de Gestión de Citas](#3-módulo-de-gestión-de-citas)
4. [Módulo de Expedientes Clínicos y Seguimiento](#4-módulo-de-expedientes-clínicos-y-seguimiento)
5. [Permisos por Rol (Resumen)](#5-permisos-por-rol-resumen)

---

## Diagrama entidad-relacion

![Diagrama entidad-relacion](images/Entidad%20relacion.png)

Las tablas auditoria_citas y auditoria_pacientes no estan relacionadas para no comprometer los datos de las tablas que interfieren en ellas y no estar ligados directamente.


## 1. Módulo de Autenticación y Seguridad

### 1.1 Registro de Pacientes

| Código | Nombre | Descripción | HTTP |
|---|---|---|---|
| BR-RPA-01 | Formato de correo | El sistema requiere un correo con formato válido (dominio con `@` y extensión) y una contraseña. | `400` |
| BR-RPA-02 | Unicidad | El correo debe ser único en toda la base de datos. Si ya existe, se rechaza. | `400` |
| BR-RPA-03 | Asignación de rol | El sistema asigna forzosamente el rol `PACIENTE`, ignorando cualquier rol que el cliente envíe en la petición (prevención de escalado de privilegios). | — |
| BR-RPA-04 | Expediente automático | Al registrarse exitosamente, se dispara un evento interno que crea un Historial Clínico en blanco vinculado al nuevo paciente. | — |
| BR-RPA-05 | Asignación de especialista | El paciente queda asignado al "Psicólogo de Planta" disponible por defecto, o en estado `sin asignar` hasta su primera cita. Ver también [BR-CIT-06](#br-cit-06). | — |

### 1.2 Registro de Psicólogos

| Código | Nombre | Descripción | HTTP |
|---|---|---|---|
| BR-RPS-01 | Unicidad | El correo debe ser estrictamente único, igual que para pacientes. | `400` |
| BR-RPS-02 | Asignación de rol | El sistema asigna forzosamente el rol `PSICOLOGO`. | — |
| BR-RPS-03 | Especialidades | El psicólogo puede vincularse a una o más especialidades del catálogo al momento de su creación (relación Muchos a Muchos). | `400` |

### 1.3 Inicio de Sesión

| Código | Descripción | HTTP |
|---|---|---|
| BR-LOG-01 | Las contraseñas nunca se comparan en texto plano; el sistema verifica el hash usando el algoritmo configurado (BCrypt). | `401` |
| BR-LOG-02 | Tras autenticación exitosa, se emite un JWT firmado con los claims de identidad (ID, Rol) y expiración de 24 horas. | — |
| BR-LOG-03 | Cualquier acceso a endpoints protegidos sin un JWT válido, expirado o malformado se rechaza inmediatamente. | `401` |

---

## 2. Módulo de Directorio Médico

### 2.1 Obtener todos los psicólogos

| Código | Nombre | Descripción | HTTP |
|---|---|---|---|
| BR-DIR-01 | Exposición de datos sensibles | El endpoint nunca devuelve contraseñas (ni hasheadas) ni otros datos sensibles. En producción, estará limitado al rol administrador. | — |
| BR-DIR-02 | Paginación | Si el volumen de psicólogos es alto, la respuesta debe paginarse para no comprometer el rendimiento. | — |

### 2.2 Buscar psicólogos por especialidad

| Código | Nombre | Descripción | HTTP |
|---|---|---|---|
| BR-DIR-03 | Filtro relacional | El sistema filtra el catálogo devolviendo únicamente psicólogos que tengan la especialidad solicitada (por ID o por nombre). | — |
| BR-DIR-04 | Manejo de vacíos | Si ningún psicólogo tiene la especialidad buscada, se devuelve `[]` con código `200 OK`, no un error del servidor. | `200` |

### 2.3 Actualizar perfil de un psicólogo

| Código | Nombre | Descripción | HTTP |
|---|---|---|---|
| BR-PRF-01 | Autorización propietaria | Un psicólogo solo puede actualizar su propio perfil. El sistema compara el ID de la petición con el ID extraído del JWT. Si no coinciden, se rechaza. | `403` |

---

## 3. Módulo de Gestión de Citas

> **BR-CIT-00 — Huso horario base:** Todas las validaciones temporales, expiración de citas, mantenimientos automatizados y cruces de agenda se calculan bajo la zona horaria `America/Mazatlan`.

### 3.1 Concurrencia y Restricciones de Tiempo

| Código | Nombre | Descripción | HTTP |
|---|---|---|---|
| BR-CIT-01 | Duración estandarizada | Todas las citas duran exactamente 50 minutos. Solo pueden agendarse en punto (09:00, 10:00…). Se rechaza cualquier minuto arbitrario (ej. 10:13). | `400` |
| BR-CIT-02 | Horario operativo | Las citas se agendan de lunes a viernes, de 09:00 a 18:00 (última cita: 18:00–18:50). Existe un bloqueo de 14:00 a 15:00 (hora de comida). | `400` |
| BR-CIT-03 | Anticipación mínima | No se permiten reservas para el mismo día. Toda cita requiere mínimo 24 horas de anticipación. | `400` |
| BR-CIT-04 | Regla de oro de concurrencia | Máximo 1 cita por hora por psicólogo. Si existe una cita `pendiente` o `confirmada` en ese horario, la nueva solicitud se rechaza. | `409` |
| BR-CIT-05 | Fechas pasadas | No está permitido crear citas (estado inicial `pendiente`) con fecha y hora que ya transcurrieron en el servidor. | `400` |
| BR-CIT-06 | Asignación automática de médico | Si es la primera cita del paciente y su campo `psicólogo` es nulo, el sistema lo vincula al psicólogo de planta. Posteriormente, el psicólogo evalúa si es necesario derivarlo. Ver [BR-RPA-05](#br-rpa-05). | — |

### 3.2 Sistema de Penalizaciones

| Código | Nombre | Descripción | HTTP |
|---|---|---|---|
| BR-PEN-01 | Penalización por no-show | Si una cita se marca como `no-show`, el campo `multaAplicada` pasa a `true` y el paciente recibe `penalizacionActiva = true`. | — |
| BR-PEN-02 | Penalización por cancelación tardía | Cancelar una cita `confirmada` con menos de 20 horas de anticipación genera penalización y `multaAplicada = true` en la cita. | — |
| BR-PEN-03 | Bloqueo preventivo | Un paciente con `penalizacionActiva = true` no puede agendar nuevas citas hasta que su estado sea resuelto. | `403` |

## 3.3 Máquina de Estados de Citas

El sistema controla de forma estricta las transiciones entre estados. Ninguna transición no listada aquí es válida.

![Grafo de maquina de estados](images/Estados%20Citas.png)

#### Tabla de transiciones

| Estado origen | Estado destino | Actor | Condición |
|---|---|---|---|
| `pendiente` | `confirmada` | Psicólogo | Dentro de las 24 h posteriores a la creación |
| `pendiente` | `rechazada` | Psicólogo / Cron Job | Dentro de 24 h, o bien, hora de cita ya transcurrió sin respuesta |
| `confirmada` | `cancelada` | Psicólogo / Paciente | Antes de que llegue la hora de la cita (si la cita estaba confirmada y el paciente cancela se le hace una multa) |
| `confirmada` | `rechazada` | Psicólogo | durante las 24 hrs despues de confirmar. Este estado está para prevenir el "error de dedo" y poder revertir una decision |
| `confirmada` | `finalizada` | Psicólogo | Después de que concluyó la cita (`fechaHora < NOW()`) |
| `confirmada` | `no-show` | Psicólogo | Después de que concluyó la cita y el paciente no asistió |
| `rechazada` | `confirmada` | Psicólogo | Solo si el rechazo fue manual "error de dedo" (no por Cron Job) |
| `cancelada` | `confirmada` | Psicólogo | Sin restricciones adicionales, igual aplica el "error de dedo" |
| `finalizada` | `no-show` | Psicólogo | Corrección administrativa - "error de dedo"|
| `no-show` | `finalizada` | Psicólogo | Corrección administrativa - "error de dedo"|

#### Reglas de transición (detalle)

| Código | Nombre | Descripción |
|---|---|---|
| BR-EST-01 | Ciclo de vida ideal | Una cita transiciona normalmente: `pendiente → confirmada → finalizada`. |
| BR-EST-02 | Agendamiento interno | Una cita creada por el propio psicólogo toma el estado `confirmada` automáticamente. |
| BR-EST-03 | Ventana de transición | Los cambios de estado están permitidos dentro de las 24 horas desde la acción que los origina; pasado ese tiempo, el estado queda bloqueado, exceptuando los estados `pendiente` y `confirmada`. |
| BR-EST-04 | Resolución de pendientes | Una cita `pendiente` solo puede volverse `confirmada` o `rechazada`, una vez hecho el cambio dentro de las 24 h posteriores a su creación. Una cita rechazada por el Cron Job no puede cambiar de estado. |
| BR-EST-05 | Antes y después de la consulta | Una cita `confirmada` puede cancelarse si aún no ha llegado su hora. Una vez que la hora transcurrió, solo puede marcarse como `finalizada` o `no-show`. |
| BR-EST-06 | Recuperación de rechazos | Una cita `rechazada` puede revertirse a `confirmada` únicamente si el rechazo fue hecho manualmente (no por el Cron Job). |
| BR-EST-07 | Recuperación de cancelaciones | Una cita `cancelada` solo puede reactivarse a `confirmada`. |
| BR-EST-08 | Cierres administrativos | `finalizada` y `no-show` pueden intercambiarse entre sí para correcciones posteriores a la consulta, pero solo durante las 24 hrs despues de realizarse la primera accion. |
| BR-EST-09 | Trazabilidad de citas | Todo `UPDATE` en la tabla `citas` (cambio de fecha o de estado) debe capturarse de forma asíncrona mediante un Trigger hacia `auditoria_citas`, registrando: ID propio, ID de cita, valores anteriores y nuevos (fecha y estado). |

### 3.4 Edge Cases de Expiración

| Código | Nombre | Descripción |
|---|---|---|
| BR-EDG-01 | Finalización prematura | Está prohibido marcar una cita como `finalizada` si su `fechaHora` aún está en el futuro (`fechaHora > NOW()`). |
| BR-EDG-02 | Cita pendiente expirada | Si la hora de la cita ya transcurrió y su estado era `pendiente`, se cambiará automaticamente a `rechazada` (cron job). |
| BR-EDG-03 | Cita confirmada expirada | Si la hora de la cita ya transcurrió y estaba `confirmada`, los únicos estados de cierre son `finalizada` o `no-show`. |

### 3.5 Mantenimiento Automatizado (Cron Job)

| Código | Nombre | Descripción |
|---|---|---|
| BR-CRN-01 | Proceso independiente | El Cron Job se ejecuta periódicamente (cada hora) de forma independiente a las peticiones HTTP, bajo el huso horario `America/Mazatlan`. |
| BR-CRN-02 | Limpieza por omisión | El proceso busca citas que cumplan dos condiciones simultáneas: estado `pendiente` **y** `fecha_hora < NOW()`. |
| BR-CRN-03 | Transición de expiración | Las citas identificadas por BR-CRN-02 se mutan automáticamente a `rechazada`. Las citas en estado `confirmada` no son alteradas por este proceso; requieren intervención manual del psicólogo. |

---

## 4. Módulo de Expedientes Clínicos y Seguimiento

### 4.1 Privacidad del Historial Clínico

| Código | Nombre | Descripción | HTTP |
|---|---|---|---|
| BR-EXP-01 | Cerco de seguridad médico-paciente | Un psicólogo solo puede visualizar, consultar o actualizar los historiales de los pacientes **directamente asignados a su ID**. El acceso al expediente de un paciente de otro colega se rechaza. | `403` |

### 4.2 Notas de Evolución

| Código | Nombre | Descripción | HTTP |
|---|---|---|---|
| BR-NOT-01 | Condición de creación | Una Nota de Evolución (diagnóstico y plan de tratamiento) solo puede crearse si está ligada al ID de una cita cuyo estado sea `finalizada`. | `400` |
| BR-NOT-02 | Inmutabilidad del histórico | Las notas firmadas vinculadas a una cita pasada son documentos médicos de **solo lectura**. Se agregan progresivamente al expediente; no pueden editarse ni eliminarse. | `403` |

### 4.3 Derivar un Paciente

| Código | Nombre | Descripción |
|---|---|---|
| BR-DER-01 | Trazabilidad de transferencias | Toda modificación del campo `id_psicologo` en la tabla `pacientes` dispara un Trigger hacia `auditoria_pacientes`, registrando: ID propio, ID del paciente, psicólogo anterior, psicólogo nuevo y marca de tiempo. |

---

## 5. Permisos por Rol (Resumen)

| Acción | `PACIENTE` | `PSICOLOGO` |
|---|:---:|:---:|
| Registrarse | ✅ | ✅ |
| Iniciar sesión | ✅ | ✅ |
| Ver directorio de psicólogos | ✅ | ✅ |
| Actualizar perfil propio | ✅ | ✅ |
| Agendar cita | ✅ (si sin penalización) | ✅ (confirma automáticamente) |
| Confirmar / rechazar cita ajena | ❌ | ✅ |
| Cancelar cita propia | ✅ | ✅ |
| Marcar `finalizada` / `no-show` | ❌ | ✅ |
| Ver expediente clínico | ❌ | Solo pacientes asignados |
| Crear nota de evolución | ❌ | ✅ (cita finalizada) |
| Editar nota de evolución | ❌ | ❌ (inmutable) |
| Derivar paciente a otro psicólogo | ❌ | ✅ |