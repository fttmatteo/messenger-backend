-- Migración V4: Agregar índices para optimización de queries en service_deliveries safely
DROP PROCEDURE IF EXISTS AddServiceIndices;
DELIMITER //
CREATE PROCEDURE AddServiceIndices()
BEGIN
    -- Index messenger_id
    IF NOT EXISTS (
        SELECT * FROM information_schema.STATISTICS 
        WHERE TABLE_SCHEMA = DATABASE() 
        AND TABLE_NAME = 'service_deliveries' 
        AND INDEX_NAME = 'idx_service_messenger_id'
    ) THEN
        CREATE INDEX idx_service_messenger_id ON service_deliveries(messenger_id);
    END IF;

    -- Index current_status
    IF NOT EXISTS (
        SELECT * FROM information_schema.STATISTICS 
        WHERE TABLE_SCHEMA = DATABASE() 
        AND TABLE_NAME = 'service_deliveries' 
        AND INDEX_NAME = 'idx_service_status'
    ) THEN
        CREATE INDEX idx_service_status ON service_deliveries(current_status);
    END IF;

    -- Index created_at
    IF NOT EXISTS (
        SELECT * FROM information_schema.STATISTICS 
        WHERE TABLE_SCHEMA = DATABASE() 
        AND TABLE_NAME = 'service_deliveries' 
        AND INDEX_NAME = 'idx_service_created_at'
    ) THEN
        CREATE INDEX idx_service_created_at ON service_deliveries(created_at DESC);
    END IF;

    -- Index deleted
    IF NOT EXISTS (
        SELECT * FROM information_schema.STATISTICS 
        WHERE TABLE_SCHEMA = DATABASE() 
        AND TABLE_NAME = 'service_deliveries' 
        AND INDEX_NAME = 'idx_service_deleted'
    ) THEN
         CREATE INDEX idx_service_deleted ON service_deliveries(deleted);
    END IF;

    -- Index messenger_active
    IF NOT EXISTS (
        SELECT * FROM information_schema.STATISTICS 
        WHERE TABLE_SCHEMA = DATABASE() 
        AND TABLE_NAME = 'service_deliveries' 
        AND INDEX_NAME = 'idx_service_messenger_active'
    ) THEN
         CREATE INDEX idx_service_messenger_active ON service_deliveries(messenger_id, deleted, created_at DESC);
    END IF;
END //
DELIMITER ;
CALL AddServiceIndices();
DROP PROCEDURE AddServiceIndices;
