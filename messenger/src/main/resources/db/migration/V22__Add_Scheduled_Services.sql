DROP PROCEDURE IF EXISTS AddScheduledServicesColumns;
DELIMITER //
CREATE PROCEDURE AddScheduledServicesColumns()
BEGIN
    IF NOT EXISTS (
        SELECT * FROM information_schema.COLUMNS 
        WHERE TABLE_SCHEMA = DATABASE() 
        AND TABLE_NAME = 'service_deliveries' 
        AND COLUMN_NAME = 'scheduled_at'
    ) THEN
        ALTER TABLE service_deliveries ADD COLUMN scheduled_at DATETIME(6) NULL;
    END IF;

    IF NOT EXISTS (
        SELECT * FROM information_schema.COLUMNS 
        WHERE TABLE_SCHEMA = DATABASE() 
        AND TABLE_NAME = 'deleted_services' 
        AND COLUMN_NAME = 'scheduled_at'
    ) THEN
        ALTER TABLE deleted_services ADD COLUMN scheduled_at DATETIME(6) NULL;
    END IF;

    IF NOT EXISTS (
        SELECT * FROM information_schema.STATISTICS 
        WHERE TABLE_SCHEMA = DATABASE() 
        AND TABLE_NAME = 'service_deliveries' 
        AND INDEX_NAME = 'idx_service_deliveries_schedule'
    ) THEN
        CREATE INDEX idx_service_deliveries_schedule ON service_deliveries (current_status, scheduled_at, deleted);
    END IF;
END //
DELIMITER ;
CALL AddScheduledServicesColumns();
DROP PROCEDURE AddScheduledServicesColumns;
