DROP PROCEDURE IF EXISTS DropLockedAtColumns;
DELIMITER //
CREATE PROCEDURE DropLockedAtColumns()
BEGIN
    IF EXISTS (
        SELECT * FROM information_schema.COLUMNS 
        WHERE TABLE_SCHEMA = DATABASE() 
        AND TABLE_NAME = 'service_deliveries' 
        AND COLUMN_NAME = 'locked_at'
    ) THEN
        ALTER TABLE service_deliveries DROP COLUMN locked_at;
    END IF;

    IF EXISTS (
        SELECT * FROM information_schema.COLUMNS 
        WHERE TABLE_SCHEMA = DATABASE() 
        AND TABLE_NAME = 'deleted_services' 
        AND COLUMN_NAME = 'locked_at'
    ) THEN
        ALTER TABLE deleted_services DROP COLUMN locked_at;
    END IF;
END //
DELIMITER ;
CALL DropLockedAtColumns();
DROP PROCEDURE DropLockedAtColumns;
