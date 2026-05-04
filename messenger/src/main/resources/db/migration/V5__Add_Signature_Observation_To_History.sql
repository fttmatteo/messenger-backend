DROP PROCEDURE IF EXISTS AddHistoryColumns;
DELIMITER //
CREATE PROCEDURE AddHistoryColumns()
BEGIN
    IF NOT EXISTS (
        SELECT * FROM information_schema.COLUMNS 
        WHERE TABLE_SCHEMA = DATABASE() 
        AND TABLE_NAME = 'status_history' 
        AND COLUMN_NAME = 'signature_id'
    ) THEN
        ALTER TABLE status_history
        ADD COLUMN signature_id BIGINT;
        
        ALTER TABLE status_history
        ADD CONSTRAINT fk_status_history_signature FOREIGN KEY (signature_id) REFERENCES signatures(id_signature);
    END IF;

    IF NOT EXISTS (
        SELECT * FROM information_schema.COLUMNS 
        WHERE TABLE_SCHEMA = DATABASE() 
        AND TABLE_NAME = 'status_history' 
        AND COLUMN_NAME = 'observation'
    ) THEN
        ALTER TABLE status_history
        ADD COLUMN observation VARCHAR(2048);
    END IF;

    IF NOT EXISTS (
        SELECT * FROM information_schema.COLUMNS 
        WHERE TABLE_SCHEMA = DATABASE() 
        AND TABLE_NAME = 'deleted_status_history' 
        AND COLUMN_NAME = 'signature_id'
    ) THEN
        ALTER TABLE deleted_status_history
        ADD COLUMN signature_id BIGINT;
    END IF;

    IF NOT EXISTS (
        SELECT * FROM information_schema.COLUMNS 
        WHERE TABLE_SCHEMA = DATABASE() 
        AND TABLE_NAME = 'deleted_status_history' 
        AND COLUMN_NAME = 'observation'
    ) THEN
        ALTER TABLE deleted_status_history
        ADD COLUMN observation VARCHAR(2048);
    END IF;
END //
DELIMITER ;
CALL AddHistoryColumns();
DROP PROCEDURE AddHistoryColumns;
