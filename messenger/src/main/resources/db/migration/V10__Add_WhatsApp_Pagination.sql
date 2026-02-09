-- Agregar columnas para paginación y filtros en sesiones de WhatsApp
ALTER TABLE wa_sessions 
ADD COLUMN current_page INT DEFAULT 0,
ADD COLUMN last_filter_statuses VARCHAR(500);
