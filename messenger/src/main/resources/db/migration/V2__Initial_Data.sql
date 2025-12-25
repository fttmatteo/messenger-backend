-- V2: Datos iniciales para producción
-- Crea el usuario administrador inicial del sistema

-- Usuario Admin Principal
-- Documento: 1000000000
-- Password: Admin123!
-- Hash generado con BCrypt (10 rounds)
INSERT INTO employees (document, full_name, phone, password, role) VALUES (
    1000413081,
    'Administrador',
    '3000000000',
    'Admin123!',
    'ADMIN'
) ON DUPLICATE KEY UPDATE document = document;

-- NOTA: El hash de arriba corresponde a la contraseña "Admin123!"
-- Si necesitas cambiar la contraseña, genera un nuevo hash BCrypt en:
-- https://bcrypt-generator.com/ (usa 10 rounds)
