DROP PROCEDURE IF EXISTS AddFullTextIndices;
DELIMITER //
CREATE PROCEDURE AddFullTextIndices()
BEGIN
    IF NOT EXISTS (
        SELECT * FROM information_schema.STATISTICS 
        WHERE TABLE_SCHEMA = DATABASE() 
        AND TABLE_NAME = 'employees' 
        AND INDEX_NAME = 'idx_employees_full_name_fulltext'
    ) THEN
        ALTER TABLE employees ADD FULLTEXT INDEX idx_employees_full_name_fulltext(full_name);
    END IF;

    IF NOT EXISTS (
        SELECT * FROM information_schema.STATISTICS 
        WHERE TABLE_SCHEMA = DATABASE() 
        AND TABLE_NAME = 'dealerships' 
        AND INDEX_NAME = 'idx_dealerships_name_fulltext'
    ) THEN
        ALTER TABLE dealerships ADD FULLTEXT INDEX idx_dealerships_name_fulltext(name);
    END IF;

    IF NOT EXISTS (
        SELECT * FROM information_schema.STATISTICS 
        WHERE TABLE_SCHEMA = DATABASE() 
        AND TABLE_NAME = 'plates' 
        AND INDEX_NAME = 'idx_plates_plate_number_fulltext'
    ) THEN
        ALTER TABLE plates ADD FULLTEXT INDEX idx_plates_plate_number_fulltext(plate_number);
    END IF;
END //
DELIMITER ;
CALL AddFullTextIndices();
DROP PROCEDURE AddFullTextIndices;
