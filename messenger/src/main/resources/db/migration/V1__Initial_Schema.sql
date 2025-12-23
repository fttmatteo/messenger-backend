-- Consolidated Migration V1
-- Includes foundational schema + Soft Delete fields + Lock fields

-- 1. Dealerships
CREATE TABLE dealerships (
    id_dealership BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(50) NOT NULL UNIQUE,
    address VARCHAR(100) NOT NULL,
    phone VARCHAR(10) NOT NULL,
    zone VARCHAR(10) NOT NULL,
    latitude DOUBLE,
    longitude DOUBLE,
    is_geolocated BOOLEAN DEFAULT FALSE
);

-- 2. Employees (Note: using full_name, NO user_name column)
CREATE TABLE employees (
    id_employee BIGINT AUTO_INCREMENT PRIMARY KEY,
    document BIGINT NOT NULL UNIQUE,
    full_name VARCHAR(255) NOT NULL,
    phone VARCHAR(20),
    password VARCHAR(255) NOT NULL,
    role VARCHAR(50) NOT NULL
);

-- 3. Plates
CREATE TABLE plates (
    id_plate BIGINT AUTO_INCREMENT PRIMARY KEY,
    plate_number VARCHAR(255) NOT NULL UNIQUE,
    plate_type VARCHAR(50) NOT NULL,
    upload_date DATETIME(6)
);

-- 4. Signatures
CREATE TABLE signatures (
    id_signature BIGINT AUTO_INCREMENT PRIMARY KEY,
    signature_path VARCHAR(2048) NOT NULL,
    upload_date DATETIME(6) NOT NULL
);

-- 5. Service Deliveries
CREATE TABLE service_deliveries (
    id_service_delivery BIGINT AUTO_INCREMENT PRIMARY KEY,
    plate_id BIGINT NOT NULL,
    dealership_id BIGINT NOT NULL,
    messenger_id BIGINT NOT NULL,
    current_status VARCHAR(50) NOT NULL,
    observation VARCHAR(255),
    signature_id BIGINT,
    created_at DATETIME(6),
    
    -- Fields from V2 (Soft Delete + Lock)
    deleted BOOLEAN DEFAULT FALSE,
    deleted_at DATETIME(6) NULL,
    locked_at DATETIME(6) NULL,

    FOREIGN KEY (plate_id) REFERENCES plates(id_plate),
    FOREIGN KEY (dealership_id) REFERENCES dealerships(id_dealership),
    FOREIGN KEY (messenger_id) REFERENCES employees(id_employee),
    FOREIGN KEY (signature_id) REFERENCES signatures(id_signature)
);

-- Indexes from V2
CREATE INDEX idx_service_deliveries_deleted ON service_deliveries(deleted);
CREATE INDEX idx_service_deliveries_deleted_at ON service_deliveries(deleted_at);

-- 6. Status History
CREATE TABLE status_history (
    id_status_history BIGINT AUTO_INCREMENT PRIMARY KEY,
    previous_status VARCHAR(50),
    new_status VARCHAR(50) NOT NULL,
    change_date DATETIME(6) NOT NULL,
    changed_by_employee_id BIGINT,
    service_delivery_id BIGINT,
    delivery_latitude DOUBLE,
    delivery_longitude DOUBLE,
    FOREIGN KEY (changed_by_employee_id) REFERENCES employees(id_employee),
    FOREIGN KEY (service_delivery_id) REFERENCES service_deliveries(id_service_delivery)
);

-- 7. Photos
CREATE TABLE photos (
    id_photo BIGINT AUTO_INCREMENT PRIMARY KEY,
    photo_path VARCHAR(2048) NOT NULL,
    upload_date DATETIME(6),
    photo_type VARCHAR(50),
    service_delivery_id BIGINT,
    status_history_id BIGINT,
    FOREIGN KEY (service_delivery_id) REFERENCES service_deliveries(id_service_delivery),
    FOREIGN KEY (status_history_id) REFERENCES status_history(id_status_history)
);

-- 8. Tracking History
CREATE TABLE tracking_history (
    history_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    messenger_id BIGINT NOT NULL,
    latitude DOUBLE NOT NULL,
    longitude DOUBLE NOT NULL,
    recorded_at DATETIME(6) NOT NULL,
    service_delivery_id BIGINT,
    source VARCHAR(50) NOT NULL,
    speed DOUBLE
);

CREATE INDEX idx_messenger_date ON tracking_history(messenger_id, recorded_at);
CREATE INDEX idx_service ON tracking_history(service_delivery_id);
