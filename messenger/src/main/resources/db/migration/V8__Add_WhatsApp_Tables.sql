-- WhatsApp Bot Integration
-- PIN para autenticación de concesionarios y tabla de sesiones

-- Agregar PIN a concesionarios (texto plano, ej: 1234)
ALTER TABLE dealerships ADD COLUMN whatsapp_pin VARCHAR(255);

-- Tabla de sesiones de WhatsApp
CREATE TABLE wa_sessions (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    phone_number VARCHAR(20) NOT NULL,
    dealership_id BIGINT NOT NULL,
    expires_at DATETIME NOT NULL,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_wa_sessions_dealership 
        FOREIGN KEY (dealership_id) REFERENCES dealerships(id_dealership)
        ON DELETE CASCADE
);

CREATE INDEX idx_wa_sessions_phone ON wa_sessions(phone_number);
CREATE INDEX idx_wa_sessions_expires ON wa_sessions(expires_at);
