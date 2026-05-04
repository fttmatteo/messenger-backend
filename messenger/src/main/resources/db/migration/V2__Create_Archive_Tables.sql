CREATE TABLE IF NOT EXISTS deleted_services (
    id_service_delivery BIGINT PRIMARY KEY,
    current_status VARCHAR(20) NOT NULL,
    observation TEXT,
    created_at DATETIME NOT NULL,
    deleted_at DATETIME,
    locked_at DATETIME,
    
    plate_id BIGINT,
    dealership_id BIGINT,
    messenger_id BIGINT,
    signature_id BIGINT,
    
    permanently_deleted_at DATETIME NOT NULL,
    permanently_deleted_by BIGINT COMMENT 'Employee who archived this service',
    deletion_reason VARCHAR(255) COMMENT 'Manual trash empty / Auto-archive after 60 days',
    
    messenger_name VARCHAR(255),
    messenger_document VARCHAR(50),
    messenger_phone VARCHAR(20),
    dealership_name VARCHAR(255),
    dealership_address VARCHAR(500),
    dealership_zone VARCHAR(100),
    plate_number VARCHAR(20) NOT NULL,
    plate_type VARCHAR(20) NOT NULL,
    
    INDEX idx_del_svc_deleted_at (permanently_deleted_at DESC),
    INDEX idx_del_svc_messenger_id (messenger_id),
    INDEX idx_del_svc_dealership_id (dealership_id),
    INDEX idx_del_svc_plate_number (plate_number),
    INDEX idx_del_svc_deletion_reason (deletion_reason)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
COMMENT='Permanent archive of deleted services with denormalized data for audit trail';

CREATE TABLE IF NOT EXISTS deleted_status_history (
    id_status_history BIGINT PRIMARY KEY,
    service_delivery_id BIGINT NOT NULL,
    previous_status VARCHAR(20),
    new_status VARCHAR(20) NOT NULL,
    change_date DATETIME NOT NULL,
    observation TEXT,
    
    changed_by_employee_id BIGINT,
    changed_by_name VARCHAR(255),
    changed_by_document VARCHAR(50),
    
    INDEX idx_del_sh_service_id (service_delivery_id),
    INDEX idx_del_sh_change_date (change_date)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
COMMENT='Status change history for archived services';

CREATE TABLE IF NOT EXISTS deleted_photos (
    id_photo BIGINT PRIMARY KEY,
    service_delivery_id BIGINT NOT NULL,
    status_history_id BIGINT COMMENT 'Which status change this photo was attached to',
    photo_path VARCHAR(500) NOT NULL COMMENT 'Path in Google Cloud Storage',
    photo_type VARCHAR(50) NOT NULL,
    upload_date DATETIME NOT NULL,
    
    INDEX idx_del_ph_service_id (service_delivery_id),
    INDEX idx_del_ph_photo_type (photo_type)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
COMMENT='Photo metadata for archived services';

CREATE TABLE IF NOT EXISTS deleted_tracking_history (
    history_id BIGINT PRIMARY KEY,
    service_delivery_id BIGINT NOT NULL,
    messenger_id BIGINT NOT NULL,
    latitude DECIMAL(10, 8) NOT NULL,
    longitude DECIMAL(11, 8) NOT NULL,
    speed DECIMAL(5, 2),
    source VARCHAR(20) NOT NULL COMMENT 'GPS, NETWORK, MANUAL',
    recorded_at DATETIME NOT NULL,
    
    INDEX idx_del_th_service_id (service_delivery_id),
    INDEX idx_del_th_messenger_id (messenger_id),
    INDEX idx_del_th_recorded_at (recorded_at),
    INDEX idx_del_th_location (latitude, longitude)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
COMMENT='GPS tracking history for archived services';

CREATE TABLE IF NOT EXISTS deleted_signatures (
    id_signature BIGINT PRIMARY KEY,
    service_delivery_id BIGINT NOT NULL,
    signature_path VARCHAR(500) NOT NULL COMMENT 'Path in Google Cloud Storage',
    created_at DATETIME NOT NULL,
    
    INDEX idx_del_sig_service_id (service_delivery_id)
)
 ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
COMMENT='Signature metadata for archived services';
