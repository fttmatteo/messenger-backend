-- ============================================
-- Seed Data para Messenger Backend
-- Base de datos: MySQL
-- ============================================

USE messenger;

-- ============================================
-- 1. EMPLEADOS (Usuarios del sistema)
-- ============================================

-- Admin principal (usuario: admin, password: admin123)
INSERT INTO employees (document, full_name, phone, user_name, password, role)
VALUES (
    111111111,
    'Administrador Principal',
    '3001234567',
    'admin',
    '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhfS',
    'ADMIN'
);

-- Mensajero 1 (usuario: mensajero, password: admin123)
INSERT INTO employees (document, full_name, phone, user_name, password, role)
VALUES (
    222222222,
    'Carlos Mensajero',
    '3009876543',
    'mensajero',
    '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhfS',
    'MESSENGER'
);

-- Mensajero 2 (usuario: jrodriguez, password: admin123)
INSERT INTO employees (document, full_name, phone, user_name, password, role)
VALUES (
    333333333,
    'Juan Rodríguez',
    '3015551234',
    'jrodriguez',
    '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhfS',
    'MESSENGER'
);

-- ============================================
-- 2. CONCESIONARIOS
-- ============================================

-- Concesionario Norte
INSERT INTO dealerships (name, address, phone, zone)
VALUES (
    'Concesionario Norte',
    'Calle 100 #50-30, Sabaneta',
    '6015551111',
    'NORTE'
);

-- Concesionario Sur
INSERT INTO dealerships (name, address, phone, zone)
VALUES (
    'Concesionario Sur',
    'Carrera 45 #20-10, Sabaneta',
    '6015552222',
    'SUR'
);

-- Concesionario Centro
INSERT INTO dealerships (name, address, phone, zone)
VALUES (
    'Concesionario Centro',
    'Avenida Principal #15-25, Sabaneta',
    '6015553333',
    'CENTRO'
);

-- Concesionario Oriente
INSERT INTO dealerships (name, address, phone, zone)
VALUES (
    'Concesionario Oriente',
    'Carrera 70 #35-80, Sabaneta',
    '6015554444',
    'ORIENTE'
);

-- Concesionario Occidente
INSERT INTO dealerships (name, address, phone, zone)
VALUES (
    'Concesionario Occidente',
    'Calle 80 #90-15, Sabaneta',
    '6015555555',
    'OCCIDENTE'
);

-- ============================================
-- VERIFICACIÓN
-- ============================================

-- Mostrar empleados creados
SELECT 'EMPLEADOS CREADOS:' AS info;
SELECT document, full_name, user_name, role FROM employees;

-- Mostrar concesionarios creados
SELECT 'CONCESIONARIOS CREADOS:' AS info;
SELECT id_dealership, name, zone FROM dealerships;

-- ============================================
-- NOTAS IMPORTANTES
-- ============================================
-- 
-- Todos los usuarios tienen la misma contraseña de prueba: admin123
-- 
-- Para cambiar la contraseña en producción, genera un nuevo hash BCrypt:
-- - Online: https://bcrypt-generator.com/
-- - Java: BCryptPasswordEncoder.encode("tu_password")
-- 
-- Credenciales de prueba:
-- - Admin:      admin / admin123
-- - Mensajero1: cmensajero / admin123
-- - Mensajero2: jrodriguez / admin123
-- 
-- ============================================
