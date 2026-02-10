-- V11: Agregar seguimiento de inactividad para sesiones de WhatsApp
ALTER TABLE wa_sessions 
ADD COLUMN last_activity_at DATETIME,
ADD COLUMN timeout_notified BOOLEAN DEFAULT FALSE;

-- Inicializar last_activity_at con created_at para sesiones existentes
UPDATE wa_sessions SET last_activity_at = created_at WHERE last_activity_at IS NULL;
