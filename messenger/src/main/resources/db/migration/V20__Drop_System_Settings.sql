DROP PROCEDURE IF EXISTS DropSystemSettingsTable;
DELIMITER //
CREATE PROCEDURE DropSystemSettingsTable()
BEGIN
    IF EXISTS (
        SELECT * FROM information_schema.TABLES 
        WHERE TABLE_SCHEMA = DATABASE() 
        AND TABLE_NAME = 'system_settings'
    ) THEN
        DROP TABLE system_settings;
    END IF;
END //
DELIMITER ;
CALL DropSystemSettingsTable();
DROP PROCEDURE DropSystemSettingsTable;
