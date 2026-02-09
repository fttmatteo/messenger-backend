-- Migración para añadir persistencia del estado de conversación en WhatsApp
ALTER TABLE wa_sessions ADD COLUMN conversation_state VARCHAR(50) DEFAULT 'MENU';

-- Los usuarios que no tienen sesión activa no necesitan estado persistente,
-- pero para los que sí tienen, el valor por defecto MENU es seguro.
UPDATE wa_sessions SET conversation_state = 'MENU' WHERE conversation_state IS NULL;
