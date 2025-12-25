-- V3: Datos de ejemplo para todas las tablas
-- Carga datos realistas para demostración y testing

-- ============================================
-- EMPLEADOS (Employees)
-- ============================================
-- Admin ya existe en V2, agregar mensajeros de ejemplo

INSERT INTO employees (document, full_name, phone, password, role) VALUES
    -- Mensajeros
    (200000001, 'Juan Rodriguez', '3101234567', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'MESSENGER'),
    (200000002, 'Carlos Gomez', '3102345678', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'MESSENGER'),
    (200000003, 'Luis Martinez', '3103456789', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'MESSENGER'),
    (200000004, 'Pedro Lopez', '3104567890', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'MESSENGER'),
    (200000005, 'Maria Garcia', '3105678901', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'MESSENGER')
ON DUPLICATE KEY UPDATE document = document;

-- Nota: Todos usan la misma contraseña "Admin123!" para facilidad en demos
-- En producción real, cada usuario debería tener su propia contraseña única

-- ============================================
-- CONCESIONARIOS (Dealerships)
-- ============================================
-- Ubicaciones reales en Bogotá

INSERT INTO dealerships (name, address, phone, zone, latitude, longitude, is_geolocated) VALUES
    ('Toyota Norte', 'Calle 170 # 45-12', '6011111111', 'Norte', 4.7523, -74.0456, TRUE),
    ('Chevrolet Centro', 'Cl. 34 # 15-20', '6012222222', 'Centro', 4.6212, -74.0723, TRUE),
    ('Mazda Sur', 'Av. Boyacá # 23-45', '6013333333', 'Sur', 4.5890, -74.1234, TRUE),
    ('Renault Occidente', 'Av. Cali # 80-90', '6014444444', 'Norte', 4.6987, -74.1012, TRUE),
    ('Kia Plaza', 'Calle 100 # 19-45', '6015555555', 'Norte', 4.6850, -74.0550, TRUE),
    ('Nissan Salitre', 'Av. El Dorado # 68-12', '6016666666', 'Centro', 4.6550, -74.1100, TRUE),
    ('Ford Chapinero', 'Cra 7 # 60-15', '6017777777', 'Centro', 4.6400, -74.0600, TRUE),
    ('Hyundai Autopista', 'Autopista Norte # 128-30', '6018888888', 'Norte', 4.7150, -74.0500, TRUE)
ON DUPLICATE KEY UPDATE name = name;

-- ============================================
-- PLACAS (Plates)
-- ============================================
-- Placas colombianas de ejemplo

INSERT INTO plates (plate_number, plate_type, upload_date) VALUES
    ('ABC-123', 'CAR', NOW() - INTERVAL 30 DAY),
    ('DEF-456', 'CAR', NOW() - INTERVAL 25 DAY),
    ('GHI-789', 'MOTORCYCLE', NOW() - INTERVAL 20 DAY),
    ('JKL-012', 'CAR', NOW() - INTERVAL 15 DAY),
    ('MNO-345', 'CAR', NOW() - INTERVAL 10 DAY),
    ('PQR-678', 'MOTORCYCLE', NOW() - INTERVAL 8 DAY),
    ('STU-901', 'CAR', NOW() - INTERVAL 5 DAY),
    ('VWX-234', 'CAR', NOW() - INTERVAL 3 DAY),
    ('YZA-567', 'MOTORCYCLE', NOW() - INTERVAL 2 DAY),
    ('BCD-890', 'CAR', NOW() - INTERVAL 1 DAY)
ON DUPLICATE KEY UPDATE plate_number = plate_number;

-- ============================================
-- SERVICIOS DE ENTREGA (Service Deliveries)
-- ============================================
-- Ejemplos de servicios en diferentes estados

-- Servicio 1: DELIVERED (completado)
INSERT INTO service_deliveries (plate_id, dealership_id, messenger_id, current_status, observation, created_at, locked_at, deleted, deleted_at)
VALUES (1, 1, 2, 'DELIVERED', 'Entregado exitosamente', NOW() - INTERVAL 5 DAY, NOW() - INTERVAL 5 DAY + INTERVAL 2 HOUR, FALSE, NULL);

-- Servicio 2: PENDING (pendiente de asignar)
INSERT INTO service_deliveries (plate_id, dealership_id, messenger_id, current_status, observation, created_at, locked_at, deleted, deleted_at)
VALUES (2, 2, 3, 'PENDING', 'Esperando asignación de mensajero', NOW() - INTERVAL 1 HOUR, NULL, FALSE, NULL);

-- Servicio 3: ASSIGNED (asignado y en ruta)
INSERT INTO service_deliveries (plate_id, dealership_id, messenger_id, current_status, observation, created_at, locked_at, deleted, deleted_at)
VALUES (3, 3, 4, 'ASSIGNED', 'Mensajero en camino al concesionario', NOW() - INTERVAL 30 MINUTE, NULL, FALSE, NULL);

-- Servicio 4: RESOLVED (incidencia resuelta)
INSERT INTO service_deliveries (plate_id, dealership_id, messenger_id, current_status, observation, created_at, locked_at, deleted, deleted_at)
VALUES (4, 4, 5, 'RESOLVED', 'Problema resuelto en sitio', NOW() - INTERVAL 3 DAY, NOW() - INTERVAL 3 DAY + INTERVAL 1 HOUR, FALSE, NULL);

-- Servicio 5: CANCELED (cancelado)
INSERT INTO service_deliveries (plate_id, dealership_id, messenger_id, current_status, observation, created_at, locked_at, deleted, deleted_at)
VALUES (5, 5, 2, 'CANCELED', 'Cancelado por cliente', NOW() - INTERVAL 2 DAY, NULL, FALSE, NULL);

-- Servicio 6: DELIVERED (otro completado)
INSERT INTO service_deliveries (plate_id, dealership_id, messenger_id, current_status, observation, created_at, locked_at, deleted, deleted_at)
VALUES (6, 6, 3, 'DELIVERED', 'Placa entregada sin problemas', NOW() - INTERVAL 1 DAY, NOW() - INTERVAL 1 DAY + INTERVAL 45 MINUTE, FALSE, NULL);

-- ============================================
-- HISTORIAL DE ESTADOS (Status History)
-- ============================================
-- Transiciones de estado para cada servicio

-- Servicio 1 (ID=1): PENDING -> ASSIGNED -> DELIVERED
INSERT INTO status_history (previous_status, new_status, change_date, changed_by_employee_id, service_delivery_id, delivery_latitude, delivery_longitude) VALUES
    (NULL, 'PENDING', NOW() - INTERVAL 5 DAY, NULL, 1, NULL, NULL),
    ('PENDING', 'ASSIGNED', NOW() - INTERVAL 5 DAY + INTERVAL 15 MINUTE, NULL, 1, NULL, NULL),
    ('ASSIGNED', 'DELIVERED', NOW() - INTERVAL 5 DAY + INTERVAL 2 HOUR, 2, 1, 4.7523, -74.0456);

-- Servicio 2 (ID=2): PENDING (aún pendiente)
INSERT INTO status_history (previous_status, new_status, change_date, changed_by_employee_id, service_delivery_id, delivery_latitude, delivery_longitude) VALUES
    (NULL, 'PENDING', NOW() - INTERVAL 1 HOUR, NULL, 2, NULL, NULL);

-- Servicio 3 (ID=3): PENDING -> ASSIGNED
INSERT INTO status_history (previous_status, new_status, change_date, changed_by_employee_id, service_delivery_id, delivery_latitude, delivery_longitude) VALUES
    (NULL, 'PENDING', NOW() - INTERVAL 1 HOUR, NULL, 3, NULL, NULL),
    ('PENDING', 'ASSIGNED', NOW() - INTERVAL 30 MINUTE, NULL, 3, NULL, NULL);

-- Servicio 4 (ID=4): PENDING -> ASSIGNED -> RESOLVED
INSERT INTO status_history (previous_status, new_status, change_date, changed_by_employee_id, service_delivery_id, delivery_latitude, delivery_longitude) VALUES
    (NULL, 'PENDING', NOW() - INTERVAL 3 DAY, NULL, 4, NULL, NULL),
    ('PENDING', 'ASSIGNED', NOW() - INTERVAL 3 DAY + INTERVAL 20 MINUTE, NULL, 4, NULL, NULL),
    ('ASSIGNED', 'RESOLVED', NOW() - INTERVAL 3 DAY + INTERVAL 1 HOUR, 5, 4, 4.6987, -74.1012);

-- Servicio 5 (ID=5): PENDING -> ASSIGNED -> CANCELED
INSERT INTO status_history (previous_status, new_status, change_date, changed_by_employee_id, service_delivery_id, delivery_latitude, delivery_longitude) VALUES
    (NULL, 'PENDING', NOW() - INTERVAL 2 DAY, NULL, 5, NULL, NULL),
    ('PENDING', 'ASSIGNED', NOW() - INTERVAL 2 DAY + INTERVAL 10 MINUTE, NULL, 5, NULL, NULL),
    ('ASSIGNED', 'CANCELED', NOW() - INTERVAL 2 DAY + INTERVAL 25 MINUTE, NULL, 5, NULL, NULL);

-- Servicio 6 (ID=6): PENDING -> ASSIGNED -> DELIVERED
INSERT INTO status_history (previous_status, new_status, change_date, changed_by_employee_id, service_delivery_id, delivery_latitude, delivery_longitude) VALUES
    (NULL, 'PENDING', NOW() - INTERVAL 1 DAY, NULL, 6, NULL, NULL),
    ('PENDING', 'ASSIGNED', NOW() - INTERVAL 1 DAY + INTERVAL 12 MINUTE, NULL, 6, NULL, NULL),
    ('ASSIGNED', 'DELIVERED', NOW() - INTERVAL 1 DAY + INTERVAL 45 MINUTE, 3, 6, 4.6550, -74.1100);

-- ============================================
-- TRACKING (Historial de GPS)
-- ============================================
-- Rutas de ejemplo para servicios completados

-- Tracking para Servicio 1 (mensajero ID=2, hacia Toyota Norte)
INSERT INTO tracking_history (messenger_id, latitude, longitude, recorded_at, service_delivery_id, source, speed) VALUES
    (2, 4.7323, -74.0656, NOW() - INTERVAL 5 DAY + INTERVAL 1 HOUR + INTERVAL 30 MINUTE, 1, 'GPS', 25.5),
    (2, 4.7373, -74.0606, NOW() - INTERVAL 5 DAY + INTERVAL 1 HOUR + INTERVAL 33 MINUTE, 1, 'GPS', 30.2),
    (2, 4.7423, -74.0556, NOW() - INTERVAL 5 DAY + INTERVAL 1 HOUR + INTERVAL 36 MINUTE, 1, 'GPS', 28.7),
    (2, 4.7473, -74.0506, NOW() - INTERVAL 5 DAY + INTERVAL 1 HOUR + INTERVAL 39 MINUTE, 1, 'GPS', 32.1),
    (2, 4.7523, -74.0456, NOW() - INTERVAL 5 DAY + INTERVAL 2 HOUR, 1, 'GPS', 15.0);

-- Tracking para Servicio 6 (mensajero ID=3, hacia Nissan Salitre)
INSERT INTO tracking_history (messenger_id, latitude, longitude, recorded_at, service_delivery_id, source, speed) VALUES
    (3, 4.6350, -74.1300, NOW() - INTERVAL 1 DAY + INTERVAL 15 MINUTE, 6, 'GPS', 22.3),
    (3, 4.6400, -74.1250, NOW() - INTERVAL 1 DAY + INTERVAL 18 MINUTE, 6, 'GPS', 26.8),
    (3, 4.6450, -74.1200, NOW() - INTERVAL 1 DAY + INTERVAL 21 MINUTE, 6, 'GPS', 29.4),
    (3, 4.6500, -74.1150, NOW() - INTERVAL 1 DAY + INTERVAL 24 MINUTE, 6, 'GPS', 31.2),
    (3, 4.6550, -74.1100, NOW() - INTERVAL 1 DAY + INTERVAL 45 MINUTE, 6, 'GPS', 12.5);

-- ============================================
-- NOTAS FINALES
-- ============================================
-- Datos de ejemplo listos para producción
-- Incluye:
--   - 6 empleados (1 admin + 5 mensajeros)
--   - 8 concesionarios en Bogotá
--   - 10 placas
--   - 6 servicios en diferentes estados
--   - Historial completo de estados
--   - Tracking GPS para servicios completados
--
-- Todas las contraseñas son: Admin123!
-- Para generar nuevos hashes: https://bcrypt-generator.com/
