DROP PROCEDURE IF EXISTS CreateTimelineEventsTable;
DELIMITER //
CREATE PROCEDURE CreateTimelineEventsTable()
BEGIN
    IF NOT EXISTS (
        SELECT * FROM information_schema.TABLES 
        WHERE TABLE_SCHEMA = DATABASE() 
        AND TABLE_NAME = 'messenger_timeline_events'
    ) THEN
        CREATE TABLE messenger_timeline_events (
            id BIGINT AUTO_INCREMENT PRIMARY KEY,
            messenger_id BIGINT NOT NULL,
            event_date DATE NOT NULL,
            timestamp DATETIME(6) NOT NULL,
            status VARCHAR(50) NOT NULL,
            plate_number VARCHAR(50),
            dealership_name VARCHAR(255),
            latitude DOUBLE,
            longitude DOUBLE,
            changed_by_name VARCHAR(255),
            changed_by_role VARCHAR(50)
        );
    END IF;

    IF NOT EXISTS (
        SELECT * FROM information_schema.STATISTICS 
        WHERE TABLE_SCHEMA = DATABASE() 
        AND TABLE_NAME = 'messenger_timeline_events' 
        AND INDEX_NAME = 'idx_timeline_messenger_date'
    ) THEN
        CREATE INDEX idx_timeline_messenger_date ON messenger_timeline_events(messenger_id, event_date);
    END IF;
END //
DELIMITER ;
CALL CreateTimelineEventsTable();
DROP PROCEDURE CreateTimelineEventsTable;
