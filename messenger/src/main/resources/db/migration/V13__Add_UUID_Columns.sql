-- Agregar columna UUID a las tablas principales para exponer identificadores opacos en la API REST.
-- Las PKs internas (BIGINT AUTO_INCREMENT) se mantienen intactas para performance.

-- 1. Employees
ALTER TABLE employees ADD COLUMN uuid CHAR(36) NULL;
UPDATE employees SET uuid = UUID() WHERE uuid IS NULL;
ALTER TABLE employees MODIFY COLUMN uuid CHAR(36) NOT NULL;
ALTER TABLE employees ADD CONSTRAINT uk_employees_uuid UNIQUE (uuid);

-- 2. Dealerships
ALTER TABLE dealerships ADD COLUMN uuid CHAR(36) NULL;
UPDATE dealerships SET uuid = UUID() WHERE uuid IS NULL;
ALTER TABLE dealerships MODIFY COLUMN uuid CHAR(36) NOT NULL;
ALTER TABLE dealerships ADD CONSTRAINT uk_dealerships_uuid UNIQUE (uuid);

-- 3. Service Deliveries
ALTER TABLE service_deliveries ADD COLUMN uuid CHAR(36) NULL;
UPDATE service_deliveries SET uuid = UUID() WHERE uuid IS NULL;
ALTER TABLE service_deliveries MODIFY COLUMN uuid CHAR(36) NOT NULL;
ALTER TABLE service_deliveries ADD CONSTRAINT uk_service_deliveries_uuid UNIQUE (uuid);
