DROP PROCEDURE IF EXISTS DropSignatureGifPath;
DELIMITER //
CREATE PROCEDURE DropSignatureGifPath()
BEGIN
    IF EXISTS (
        SELECT * FROM information_schema.COLUMNS 
        WHERE TABLE_SCHEMA = DATABASE() 
        AND TABLE_NAME = 'signatures' 
        AND COLUMN_NAME = 'gif_path'
    ) THEN
        ALTER TABLE signatures DROP COLUMN gif_path;
    END IF;
END //
DELIMITER ;
CALL DropSignatureGifPath();
DROP PROCEDURE DropSignatureGifPath;
