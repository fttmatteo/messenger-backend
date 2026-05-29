DROP PROCEDURE IF EXISTS CreateWhatsAppUserTerms;
DELIMITER //
CREATE PROCEDURE CreateWhatsAppUserTerms()
BEGIN
    IF NOT EXISTS (
        SELECT * FROM information_schema.TABLES 
        WHERE TABLE_SCHEMA = DATABASE() 
        AND TABLE_NAME = 'whatsapp_user_terms'
    ) THEN
        CREATE TABLE whatsapp_user_terms (
            id BIGINT AUTO_INCREMENT PRIMARY KEY,
            phone_number VARCHAR(255) NOT NULL UNIQUE,
            accepted_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
        );
    END IF;

    IF NOT EXISTS (
        SELECT * FROM information_schema.STATISTICS 
        WHERE TABLE_SCHEMA = DATABASE() 
        AND TABLE_NAME = 'whatsapp_user_terms' 
        AND INDEX_NAME = 'idx_whatsapp_user_terms_phone'
    ) THEN
        CREATE INDEX idx_whatsapp_user_terms_phone ON whatsapp_user_terms (phone_number);
    END IF;
END //
DELIMITER ;
CALL CreateWhatsAppUserTerms();
DROP PROCEDURE CreateWhatsAppUserTerms;
