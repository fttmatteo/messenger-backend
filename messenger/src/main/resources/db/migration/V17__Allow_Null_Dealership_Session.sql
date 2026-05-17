DROP PROCEDURE IF EXISTS AllowNullDealershipSession;
DELIMITER //
CREATE PROCEDURE AllowNullDealershipSession()
BEGIN
    IF EXISTS (
        SELECT * FROM information_schema.COLUMNS 
        WHERE TABLE_SCHEMA = DATABASE() 
        AND TABLE_NAME = 'wa_sessions' 
        AND COLUMN_NAME = 'dealership_id'
    ) THEN
        ALTER TABLE wa_sessions MODIFY COLUMN dealership_id BIGINT NULL;
    END IF;
END //
DELIMITER ;
CALL AllowNullDealershipSession();
DROP PROCEDURE AllowNullDealershipSession;
