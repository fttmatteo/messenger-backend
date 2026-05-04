CREATE TABLE IF NOT EXISTS system_settings (
    setting_key VARCHAR(100) PRIMARY KEY,
    setting_value TEXT NOT NULL,
    updated_at DATETIME(6) DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6)
);

INSERT IGNORE INTO system_settings (setting_key, setting_value) VALUES ('STATUS_COLORS', '{"ASSIGNED":"#00eeffe1","PENDING":"#ff6f00e8","DELIVERED":"#04ff60dd","RETURNED":"#fbff00d1","CANCELED":"#ff00b7d6","RESOLVED":"#1900ffdb","DELETED":"#ff0000dd"}');
