DROP PROCEDURE IF EXISTS AddSnapshotDealership;
DELIMITER //
CREATE PROCEDURE AddSnapshotDealership()
BEGIN
    IF NOT EXISTS (
        SELECT * FROM information_schema.COLUMNS 
        WHERE TABLE_SCHEMA = DATABASE() 
        AND TABLE_NAME = 'status_history' 
        AND COLUMN_NAME = 'snapshot_origin_dealership_id'
    ) THEN
        ALTER TABLE status_history 
            ADD COLUMN snapshot_origin_dealership_id BIGINT NULL,
            ADD COLUMN snapshot_origin_dealership_name VARCHAR(255) NULL,
            ADD COLUMN snapshot_destination_dealership_id BIGINT NULL,
            ADD COLUMN snapshot_destination_dealership_name VARCHAR(255) NULL;
    END IF;

    IF NOT EXISTS (
        SELECT * FROM information_schema.COLUMNS 
        WHERE TABLE_SCHEMA = DATABASE() 
        AND TABLE_NAME = 'deleted_status_history' 
        AND COLUMN_NAME = 'snapshot_origin_dealership_id'
    ) THEN
        ALTER TABLE deleted_status_history 
            ADD COLUMN snapshot_origin_dealership_id BIGINT NULL,
            ADD COLUMN snapshot_origin_dealership_name VARCHAR(255) NULL,
            ADD COLUMN snapshot_destination_dealership_id BIGINT NULL,
            ADD COLUMN snapshot_destination_dealership_name VARCHAR(255) NULL;
    END IF;
END //
DELIMITER ;
CALL AddSnapshotDealership();
DROP PROCEDURE AddSnapshotDealership;
