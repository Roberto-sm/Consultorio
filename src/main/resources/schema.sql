CREATE TRIGGER tr_auditar_cambio_psicologo
    AFTER UPDATE ON pacientes
    FOR EACH ROW
BEGIN
    IF (OLD.id_psicologo <> NEW.id_psicologo OR
        (OLD.id_psicologo IS NULL AND NEW.id_psicologo IS NOT NULL) OR
        (OLD.id_psicologo IS NOT NULL AND NEW.id_psicologo IS NULL)) THEN

        INSERT INTO auditoria_pacientes (
            id_paciente, id_psicologo_anterior, id_psicologo_nuevo, fecha_modificacion
        )
        VALUES (
            OLD.id, OLD.id_psicologo, NEW.id_psicologo, NOW()
        );
END IF;
END;


CREATE TRIGGER tr_auditar_cambio_cita
    AFTER UPDATE ON citas
    FOR EACH ROW
BEGIN
    IF NOT (OLD.fecha_hora <=> NEW.fecha_hora)
       OR NOT (OLD.estado <=> NEW.estado)
       OR NOT (OLD.es_primera <=> NEW.es_primera) THEN

        INSERT INTO auditoria_citas (
            id_cita,
            fecha_anterior, fecha_nueva,
            estado_anterior, estado_nuevo,
            es_primera_anterior, es_primera_nuevo,
            fecha_modificacion
        ) VALUES (
            OLD.id,
            OLD.fecha_hora, NEW.fecha_hora,
            OLD.estado, NEW.estado,
            OLD.es_primera, NEW.es_primera,
            NOW()
        );
END IF;
END;