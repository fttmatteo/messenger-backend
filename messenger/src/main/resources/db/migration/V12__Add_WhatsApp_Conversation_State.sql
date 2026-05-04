ALTER TABLE wa_sessions ADD COLUMN conversation_state VARCHAR(50) DEFAULT 'MENU';

UPDATE wa_sessions SET conversation_state = 'MENU' WHERE conversation_state IS NULL;
