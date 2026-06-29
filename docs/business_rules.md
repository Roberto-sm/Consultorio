# Especificación Detallada de Reglas de Negocio y Flujo Clínicos

Este documento detalla la lógica de dominio, restricciones de tiempo, control de concurrencia y la máquina de estados que gobierna la API del Consultorio Psicológico. Estas reglas aseguran la integridad de los datos clínicos, previenen conflictos de agenda y automatizan la gestión administrativa de la clínica.

## 1. Modulo de Autenticación y Seguridad

1.1 Registro de Pacientes
BR-RPA-1: El sistema debe requerir un formato de correo electrónico válido (debe llevar "@" seguido de un texto y despues ".com") y una contraseña para procesar la solicitud.

BR-RPA-2: El correo electrónico ingresado debe ser único en toda la base de datos. Si el correo ya existe, se debe rechazar con un error 400 Bad Request.

BR-RPA-3 (Asignación de Rol): El sistema debe asignar forzosamente y de manera interna el rol de paciente, ignorando cualquier rol que el usuario intente enviarse a sí mismo en la petición (prevención de escalado de privilegios).

BR-RPA-4 (Expediente Automático): Al registrarse exitosamente, el sistema debe disparar un evento interno que cree un registro de Historial Clínico (Expediente) totalmente en blanco y vinculado a este nuevo paciente.

BR-RPA-5 (Asignación de Especialista): El paciente recién creado debe ser asignado internamente a un "Psicólogo de Planta" disponible por defecto, o en su defecto, quedar en estado "sin asignar" hasta su primera cita.


1.2. Registro de Psicologos
BR-RPS-1 (Unicidad): Al igual que los pacientes, el correo debe ser estrictamente único.

BR-RPS-2 (Asignación de Rol): El sistema debe asignar forzosamente el rol de psicologo.

BR-RPS-3 (Especialidades): El psicólogo debe poder ser vinculado a una o más especialidades (relación Muchos a Muchos) existentes en el catálogo del sistema al momento de su creación.


1.3. Inicio de Sesión (Login)
BR-LOG-1: Las contraseñas nunca deben ser comparadas en texto plano; el sistema debe verificar el hash encriptado utilizando el algoritmo de seguridad configurado (ej. BCrypt).

BR-LOG-2: Tras una autenticación exitosa, el sistema debe emitir un Token JWT (JSON Web Token) firmado, el cual contendrá los claims de identidad (ID, Rol) y tendrá un tiempo de expiración definido (24 hrs).

BR-LOG-3: Cualquier intento de acceso a endpoints protegidos sin un Token JWT válido, expirado o malformado debe ser rechazado inmediatamente con un código de estado 401 Unauthorized.

## 2. Módulo de Directorio Médico

2.1. Obtener todos los psicólogos
BR-DIR-1 (Exposición de Datos Sensibles): El endpoint público/protegido que devuelve la lista de psicólogos NUNCA debe devolver las contraseñas (ni siquiera hasheadas) u otros datos sensibles del modelo de Usuario, en produccion este endpoint estaria limitado al rol de administrador.

BR-DIR-2 (Paginación): Si el volumen de psicólogos es alto, la respuesta debe estar paginada para no comprometer el rendimiento del servidor.

2.2. Buscar psicólogos por especialidad
BR-DIR-3 (Filtro Relacional): El sistema debe ser capaz de filtrar el catálogo de psicólogos devolviendo únicamente aquellos que tengan en su arreglo de especialidades la especialidad solicitada (por ID o por Nombre de especialidad).

BR-DIR-4 (Manejo de Vacíos): Si se busca una especialidad que no tiene ningún psicólogo asignado en ese momento, el sistema debe devolver una lista vacía [] con código 200 OK, no un error del servidor.

2.3. Actualizar el perfil de un psicólogo
BR-PRF-1 (Autorización Propietaria): Un psicólogo SOLO puede actualizar su propio perfil. El sistema debe comparar el ID enviado en la petición con el ID extraído del Token JWT del usuario que hace la llamada. Si no coinciden, se rechaza con 403 Forbidden.

## 3. Módulo de Gestión de Citas (Agendamiento)
 
3.1. Concurrencia y Restricciones de Tiempo
BR-CIT-1 (Duración Estandarizada): Todas las citas tienen una duración fija de 50 minutos. El sistema rechaza cualquier intento de reserva en minutos arbitrarios (ej. 10:13 AM); las citas solo pueden agendarse estrictamente en punto (ej. 09:00, 10:00, 11:00, etc.).

BR-CIT-2: Las citas deben ser agendadas con horario de las 09:00 a las 18:00 hrs de lunes a viernes (ultima cita disponible 18:00 - 18:50), y de las 09:00 a las 13:00 hrs. De lunes a viernes existe el bloqueo de las 14:00 a las 15:00 hrs (hora de comida) 

BR-CIT-3: Anticipación Mínima: No se permiten reservas para el mismo día. Toda cita debe agendarse con un mínimo de 24 horas de anticipación.

BR-CIT-4 (Regla de Oro de Concurrencia): El sistema prohíbe estrictamente agendar más de una cita en la misma fecha y hora exacta con el mismo psicólogo, si existe una cita pendiente o confirmada en ese horario se marcará como error. Validar solapamientos (ej. máximo 1 cita por hora por doctor).

BR-CIT-5 (Fechas Pasadas): No está permitido agendar nuevas citas (estado inicial pendiente) con fechas y horas que ya hayan transcurrido en el servidor.

BR-CIT-6 (Asignación Automática de Médico): Si es la primera vez que un paciente agenda y su campo psicologo es nulo, el sistema debe vincularlo al psicologo de planta al menos la primera cita, despues el psicologo evalua si es necesario derivarlo con otro psicologo.


3.2. Sistema de Penalizaciones (Multas)
BR-PEN-1 (Penalizacion por no-show): Si cualquier cita del paciente es marcada con el estado no-show (inasistencia), el campo multaAplicada de esa cita pasará a true, lo cual debe cambiar el estado del paciente a penalizacionActiva = true, asi como el campo multa_aplicada para marcar la cita a la que corresponde esa penalizacion.

BR-PEN-2 (Penalizacion por cancelacion): Si un paciente decide cancelar una cita que ya habia sido aprobada por el psicologo menos de 20 horas antes de la sesion, recibirá una penalizacion y la cita tendrá una multa aplicada

BR-PEN-3 (Bloqueo Preventivo): Si un paciente tiene el estatus penalizacionActiva = true, el sistema debe denegar inmediatamente cualquier intento de agendar nuevas citas (HTTP 403 o 400).

--- Máquina de Estados de Citas (Transiciones) --- 
El sistema maneja un control estricto sobre cómo una cita puede cambiar de estado (Pendiente, Confirmada, Cancelada, Rechazada, Finalizada, No-show). 

![Grafo de maquina de estados](images/Estados%20Citas.png)

3.3. Transiciones Activas
BR-EST-1 (Ciclo de Vida Ideal): Una cita normal transiciona de pendiente -> confirmada -> finalizada.

BR-EST-2: una cita agendada por un psicologo toma el estado "confirmada" automaticamente 

BR-EST-2: Todas las citas pueden cambiar de estado segun lo permita el sistema en un lapso de 24 horas, pasado el tiempo no podrá cambiar de estado.

BR-EST-3: una cita pendiente solo puede marcarse como confirmada o rechazada durante las 24 horas posteriores de hacer la selección; una cita que fue rechazada automáticamente por el Cron job no puede cambiar su estado.

BR-EST-4: una cita confirmada puede marcarse como rechazada o cancelada si aun no ha llegado la hora de la cita, por otro lado una vez terminada la cita (no durante ni antes) solo puede cambiar su estado a finalizada o no-show.

BR-EST-5: una cita rechazada solo puede cambiarse a confirmada solo si no fue rechazada por el cron job

BR-EST-6: una cita cancelada solo puede ser confirmada de nuevo

BR-EST-7: Una cita finalizada solo puede cambiar a no-show y viceversa 

BR-EST-8 (Trazabilidad de Citas): Todo UPDATE efectuado en la tabla citas (cambio de fecha o de estado) debe ser capturado obligatoriamente de forma asíncrona mediante un Trigger a nivel base de datos hacia la tabla auditoria_citas que cuenta con su propio id, el id de la cita y los valores viejos y nuevos como la fecha y el estado.


3.4. Edge Cases de Expiración de Tiempo
BR-EST-1 (Finalización Prematura): Es lógicamente imposible y está prohibido marcar una cita como finalizada si su fecha y hora aún están en el futuro (fechaHora > NOW()).

BR-EST-2 (Cita Pendiente Expirada): Si la hora de una cita ya transcurrió en el reloj del servidor y su estado seguía siendo pendiente, el único cambio de estado permitido será a rechazada, ya que nunca se confirmó ni se canceló.

BR-EST-3 (Cita Confirmada Expirada): Si la hora de una cita ya transcurrió y estaba confirmada, los únicos estados permitidos para su cierre son finalizada (si ocurrió la consulta) o no-show (si el paciente no llegó).


## 4. Módulo de Expedientes Clínicos y Seguimiento

4.1. Privacidad del Historial Clínico
BR-EXP-01 (Cerco de Seguridad Médico-Paciente): Un psicólogo SOLAMENTE tiene permisos para visualizar, consultar o actualizar los historiales clínicos de los pacientes que están directamente asignados a su ID. Si intenta acceder al expediente de un paciente asignado a otro colega, el sistema arrojará 403 Forbidden.


4.2. Notas de Evolución
BR-NOT-1 (Condición de Creación): Una Nota de Evolución (que incluye diagnóstico y plan de tratamiento) solo puede ser creada e insertada si está estrictamente ligada al ID de una cita cuyo estado sea finalizada.

BR-NOT-2 (Inmutabilidad del Histórico): Las notas de evolución firmadas y vinculadas a una cita pasada deben tratarse como documentos médicos inmutables de solo lectura, agregándose progresivamente al expediente del paciente.

4.3 Derivar un paciente
(Trazabilidad de Transferencias): Toda modificación en el campo id_psicologo de la tabla pacientes (cambiar a un paciente de doctor) debe registrar al doctor antiguo, el nuevo y la estampa de tiempo mediante Trigger hacia la tabla auditoria_pacientes que cuenta con su propio id, el id del paciente, el psicologo anterior y el nuevo.

