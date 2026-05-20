DROP PROCEDURE IF EXISTS AddOriginDealership;
DELIMITER //
CREATE PROCEDURE AddOriginDealership()
BEGIN
    IF NOT EXISTS (
        SELECT * FROM information_schema.COLUMNS 
        WHERE TABLE_SCHEMA = DATABASE() 
        AND TABLE_NAME = 'service_deliveries' 
        AND COLUMN_NAME = 'origin_dealership_id'
    ) THEN
        ALTER TABLE service_deliveries ADD COLUMN origin_dealership_id BIGINT NOT NULL AFTER dealership_id;
    END IF;

    IF NOT EXISTS (
        SELECT * FROM information_schema.TABLE_CONSTRAINTS 
        WHERE CONSTRAINT_SCHEMA = DATABASE() 
        AND TABLE_NAME = 'service_deliveries' 
        AND CONSTRAINT_NAME = 'fk_service_origin_dealership'
        AND CONSTRAINT_TYPE = 'FOREIGN KEY'
    ) THEN
        ALTER TABLE service_deliveries ADD CONSTRAINT fk_service_origin_dealership FOREIGN KEY (origin_dealership_id) REFERENCES dealerships(id_dealership);
    END IF;

    IF NOT EXISTS (
        SELECT * FROM information_schema.STATISTICS 
        WHERE TABLE_SCHEMA = DATABASE() 
        AND TABLE_NAME = 'service_deliveries' 
        AND INDEX_NAME = 'idx_service_deliveries_origin_dealership'
    ) THEN
        CREATE INDEX idx_service_deliveries_origin_dealership ON service_deliveries(origin_dealership_id);
    END IF;

    IF NOT EXISTS (
        SELECT * FROM information_schema.COLUMNS 
        WHERE TABLE_SCHEMA = DATABASE() 
        AND TABLE_NAME = 'deleted_services' 
        AND COLUMN_NAME = 'origin_dealership_id'
    ) THEN
        ALTER TABLE deleted_services ADD COLUMN origin_dealership_id BIGINT NOT NULL AFTER dealership_id;
    END IF;

    IF NOT EXISTS (
        SELECT * FROM information_schema.COLUMNS 
        WHERE TABLE_SCHEMA = DATABASE() 
        AND TABLE_NAME = 'deleted_services' 
        AND COLUMN_NAME = 'origin_dealership_name'
    ) THEN
        ALTER TABLE deleted_services ADD COLUMN origin_dealership_name VARCHAR(255) NOT NULL AFTER origin_dealership_id;
    END IF;

    IF NOT EXISTS (
        SELECT * FROM information_schema.COLUMNS 
        WHERE TABLE_SCHEMA = DATABASE() 
        AND TABLE_NAME = 'deleted_services' 
        AND COLUMN_NAME = 'origin_dealership_address'
    ) THEN
        ALTER TABLE deleted_services ADD COLUMN origin_dealership_address VARCHAR(500) NOT NULL AFTER origin_dealership_name;
    END IF;

    IF NOT EXISTS (
        SELECT * FROM information_schema.COLUMNS 
        WHERE TABLE_SCHEMA = DATABASE() 
        AND TABLE_NAME = 'deleted_services' 
        AND COLUMN_NAME = 'origin_dealership_zone'
    ) THEN
        ALTER TABLE deleted_services ADD COLUMN origin_dealership_zone VARCHAR(100) NOT NULL AFTER origin_dealership_address;
    END IF;
END //
DELIMITER ;
CALL AddOriginDealership();
DROP PROCEDURE AddOriginDealership;
