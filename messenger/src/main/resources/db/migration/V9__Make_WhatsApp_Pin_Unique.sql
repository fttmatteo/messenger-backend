-- Asegurar que el PIN de WhatsApp sea único para cada concesionario
ALTER TABLE dealerships MODIFY COLUMN whatsapp_pin VARCHAR(255) UNIQUE;
