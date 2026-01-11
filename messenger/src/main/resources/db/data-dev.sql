-- ============================================
-- DATOS DE DESARROLLO - Solo para perfil DEV
-- ============================================
-- Este archivo contiene datos de prueba para desarrollo local
-- Se ejecuta automáticamente cuando el perfil 'dev' está activo

-- ============================================
-- CONCESIONARIOS DE PRUEBA
-- ============================================
INSERT IGNORE INTO dealerships (id_dealership, name, address, phone, zone, latitude, longitude, is_geolocated) VALUES
(1001, 'AutoMax Norte', 'Calle 170 #45-23, Bogotá', '3014567890', 'NORTE', 4.7528, -74.0361, TRUE),
(1002, 'MotorCity Sur', 'Av. Villavicencio #68-12, Bogotá', '3025678901', 'SUR', 4.5980, -74.1304, TRUE),
(1003, 'CarCenter Usaquén', 'Cra 7 #140-15, Bogotá', '3036789012', 'NORTE', 4.7053, -74.0328, TRUE),
(1004, 'Automóviles Elite', 'Calle 80 #30-45, Bogotá', '3047890123', 'CENTRO', 4.6688, -74.0810, TRUE),
(1005, 'MegaAutos Suba', 'Av. Suba #104-50, Bogotá', '3058901234', 'NORTE', 4.7414, -74.0836, TRUE),
(1006, 'Concesionario Imperial', 'Cra 68 #37-45, Bogotá', '3069012345', 'OCCIDENTE', 4.6428, -74.1117, TRUE),
(1007, 'Autos del Valle', 'Calle 100 #15-20, Bogotá', '3070123456', 'NORTE', 4.6836, -74.0429, TRUE),
(1008, 'Chevrolet Central', 'Av. Boyacá #64-32, Bogotá', '3081234567', 'OCCIDENTE', 4.6576, -74.1079, TRUE);

-- ============================================
-- EMPLEADOS DE PRUEBA (Mensajeros y Admins)
-- ============================================
-- Contraseña para todos: 'dev123' (hash BCrypt)
INSERT IGNORE INTO employees (id_employee, document, full_name, phone, password, role) VALUES
(1001, 1000000001, 'Admin Desarrollo', '3101234567', '$2a$10$N9qo8uLOickgx2ZMRZoMye/r6T0k6M6X9dB3bOtG0l5H0V9f5h5Oa', 'ADMIN'),
(1002, 1000000002, 'Carlos Mensajero Dev', '3112345678', '$2a$10$N9qo8uLOickgx2ZMRZoMye/r6T0k6M6X9dB3bOtG0l5H0V9f5h5Oa', 'MESSENGER'),
(1003, 1000000003, 'María García Dev', '3123456789', '$2a$10$N9qo8uLOickgx2ZMRZoMye/r6T0k6M6X9dB3bOtG0l5H0V9f5h5Oa', 'MESSENGER'),
(1004, 1000000004, 'Juan Pérez Dev', '3134567890', '$2a$10$N9qo8uLOickgx2ZMRZoMye/r6T0k6M6X9dB3bOtG0l5H0V9f5h5Oa', 'MESSENGER'),
(1005, 1000000005, 'Ana Rodríguez Dev', '3145678901', '$2a$10$N9qo8uLOickgx2ZMRZoMye/r6T0k6M6X9dB3bOtG0l5H0V9f5h5Oa', 'MESSENGER');

-- ============================================
-- PLACAS DE PRUEBA
-- ============================================
INSERT IGNORE INTO plates (id_plate, plate_number, plate_type, upload_date) VALUES
(1001, 'DEV001', 'CAR', NOW()),
(1002, 'DEV002', 'CAR', NOW()),
(1003, 'DEV003', 'MOTORCYCLE', NOW()),
(1004, 'DEV004', 'CAR', NOW()),
(1005, 'DEV005', 'MOTORCYCLE', NOW()),
(1006, 'DEV006', 'CAR', NOW()),
(1007, 'DEV007', 'CAR', NOW()),
(1008, 'DEV008', 'MOTORCYCLE', NOW()),
(1009, 'DEV009', 'CAR', NOW()),
(1010, 'DEV010', 'CAR', NOW()),
(1011, 'DEV011', 'MOTORCYCLE', NOW()),
(1012, 'DEV012', 'CAR', NOW()),
(1013, 'DEV013', 'CAR', NOW()),
(1014, 'DEV014', 'MOTORCYCLE', NOW()),
(1015, 'DEV015', 'CAR', NOW()),
(1016, 'DEV016', 'CAR', NOW()),
(1017, 'DEV017', 'MOTORCYCLE', NOW()),
(1018, 'DEV018', 'CAR', NOW()),
(1019, 'DEV019', 'CAR', NOW()),
(1020, 'DEV020', 'MOTORCYCLE', NOW());

-- ============================================
-- 20 SERVICIOS DE ENTREGA DE PRUEBA
-- ============================================
-- Variedad de estados para testing completo

INSERT IGNORE INTO service_deliveries (id_service_delivery, plate_id, dealership_id, messenger_id, current_status, observation, created_at, deleted) VALUES
-- Servicios ASSIGNED (4 servicios - recién asignados)
(1001, 1001, 1001, 1002, 'ASSIGNED', 'Servicio asignado para entrega hoy', NOW(), FALSE),
(1002, 1002, 1002, 1002, 'ASSIGNED', 'Entrega prioritaria - cliente VIP', NOW(), FALSE),
(1003, 1003, 1003, 1003, 'ASSIGNED', 'Documentos completos verificados', NOW(), FALSE),
(1004, 1004, 1004, 1003, 'ASSIGNED', 'Primera entrega del día', NOW(), FALSE),

-- Servicios PENDING (4 servicios - en sitio esperando)
(1005, 1005, 1005, 1002, 'PENDING', 'En sitio - esperando cliente', DATE_SUB(NOW(), INTERVAL 2 HOUR), FALSE),
(1006, 1006, 1006, 1003, 'PENDING', 'Cliente solicitó esperar 30 minutos', DATE_SUB(NOW(), INTERVAL 1 HOUR), FALSE),
(1007, 1007, 1007, 1004, 'PENDING', 'Verificando documentación en recepción', DATE_SUB(NOW(), INTERVAL 45 MINUTE), FALSE),
(1008, 1008, 1008, 1004, 'PENDING', 'En cola de atención', DATE_SUB(NOW(), INTERVAL 3 HOUR), FALSE),

-- Servicios DELIVERED (6 servicios - entregados exitosamente)
(1009, 1009, 1001, 1002, 'DELIVERED', 'Entrega exitosa - firmado por recepcionista', DATE_SUB(NOW(), INTERVAL 1 DAY), FALSE),
(1010, 1010, 1002, 1003, 'DELIVERED', 'Entregado sin novedades', DATE_SUB(NOW(), INTERVAL 2 DAY), FALSE),
(1011, 1011, 1003, 1004, 'DELIVERED', 'Cliente satisfecho - buen servicio', DATE_SUB(NOW(), INTERVAL 3 DAY), FALSE),
(1012, 1012, 1004, 1002, 'DELIVERED', 'Documentos verificados y entregados', DATE_SUB(NOW(), INTERVAL 4 DAY), FALSE),
(1013, 1013, 1005, 1003, 'DELIVERED', 'Entrega completada en horario', DATE_SUB(NOW(), INTERVAL 5 DAY), FALSE),
(1014, 1014, 1006, 1004, 'DELIVERED', 'Proceso completado correctamente', DATE_SUB(NOW(), INTERVAL 6 DAY), FALSE),

-- Servicios RETURNED (3 servicios - devueltos)
(1015, 1015, 1007, 1002, 'RETURNED', 'Dirección incorrecta - cliente no encontrado', DATE_SUB(NOW(), INTERVAL 1 DAY), FALSE),
(1016, 1016, 1008, 1003, 'RETURNED', 'Documentación incompleta - requiere revisión', DATE_SUB(NOW(), INTERVAL 2 DAY), FALSE),
(1017, 1017, 1001, 1004, 'RETURNED', 'Establecimiento cerrado - horario incorrecto', DATE_SUB(NOW(), INTERVAL 3 DAY), FALSE),

-- Servicios CANCELED (2 servicios - cancelados)
(1018, 1018, 1002, 1002, 'CANCELED', 'Cancelado por solicitud del cliente', DATE_SUB(NOW(), INTERVAL 2 DAY), FALSE),
(1019, 1019, 1003, 1003, 'CANCELED', 'Servicio duplicado - eliminado por admin', DATE_SUB(NOW(), INTERVAL 4 DAY), FALSE),

-- Servicios RESOLVED (1 servicio - resuelto manualmente)
(1020, 1020, 1004, 1004, 'RESOLVED', 'Resuelto manualmente tras incidencia con documentos', DATE_SUB(NOW(), INTERVAL 5 DAY), FALSE);
