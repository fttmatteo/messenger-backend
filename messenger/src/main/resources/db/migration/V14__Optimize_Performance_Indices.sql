DROP PROCEDURE IF EXISTS OptimizePerformanceIndices;
DELIMITER //
CREATE PROCEDURE OptimizePerformanceIndices()
BEGIN
    IF NOT EXISTS (
        SELECT * FROM information_schema.STATISTICS 
        WHERE TABLE_SCHEMA = DATABASE() 
        AND TABLE_NAME = 'status_history' 
        AND INDEX_NAME = 'idx_sh_service_delivery_id'
    ) THEN
        CREATE INDEX idx_sh_service_delivery_id ON status_history(service_delivery_id);
    END IF;

    IF NOT EXISTS (
        SELECT * FROM information_schema.STATISTICS 
        WHERE TABLE_SCHEMA = DATABASE() 
        AND TABLE_NAME = 'service_deliveries' 
        AND INDEX_NAME = 'idx_service_messenger_status'
    ) THEN
        CREATE INDEX idx_service_messenger_status ON service_deliveries(messenger_id, deleted, current_status);
    END IF;

END //
DELIMITER ;
CALL OptimizePerformanceIndices();
DROP PROCEDURE OptimizePerformanceIndices;
