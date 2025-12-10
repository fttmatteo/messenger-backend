-- ==========================================
-- Migration Script: Add Google Maps Integration
-- Date: 2025-12-10
-- Description: Añade soporte para geolocalización en concesionarios,
--              ubicaciones de entrega y tracking de mensajeros
-- ==========================================

-- 1. Añadir columnas de geolocalización a la tabla dealerships
ALTER TABLE dealerships 
ADD COLUMN latitude DOUBLE,
ADD COLUMN longitude DOUBLE,
ADD COLUMN is_geolocated BOOLEAN DEFAULT FALSE;

-- Índice para búsquedas geoespaciales (opcional pero recomendado)
CREATE INDEX idx_dealerships_location ON dealerships(latitude, longitude);

-- 2. Añadir columnas de ubicación de entrega a la tabla status_history
ALTER TABLE status_history
ADD COLUMN delivery_latitude DOUBLE,
ADD COLUMN delivery_longitude DOUBLE;

-- 3. Crear nueva tabla para historial de tracking de mensajeros
CREATE TABLE tracking_history (
    history_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    messenger_id BIGINT NOT NULL,
    latitude DOUBLE NOT NULL,
    longitude DOUBLE NOT NULL,
    recorded_at DATETIME NOT NULL,
    service_delivery_id BIGINT,
    source ENUM('GPS', 'NETWORK', 'MANUAL') NOT NULL,
    speed DOUBLE,
    
    -- Claves foráneas
    CONSTRAINT fk_tracking_messenger FOREIGN KEY (messenger_id) 
        REFERENCES employees(id_employee) ON DELETE CASCADE,
    CONSTRAINT fk_tracking_service FOREIGN KEY (service_delivery_id) 
        REFERENCES service_deliveries(id_service_delivery) ON DELETE SET NULL,
    
    -- Índices para mejorar rendimiento
    INDEX idx_messenger_date (messenger_id, recorded_at),
    INDEX idx_service (service_delivery_id),
    INDEX idx_recorded_at (recorded_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ==========================================
-- Fin del script de migración
-- ==========================================
