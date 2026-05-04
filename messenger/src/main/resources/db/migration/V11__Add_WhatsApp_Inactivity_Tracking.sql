ALTER TABLE wa_sessions 
ADD COLUMN last_activity_at DATETIME,
ADD COLUMN timeout_notified BOOLEAN DEFAULT FALSE;

UPDATE wa_sessions SET last_activity_at = created_at WHERE last_activity_at IS NULL;
