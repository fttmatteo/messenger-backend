-- Migración V4: Agregar índices para optimización de queries en service_deliveries
-- Mejora el rendimiento de consultas paginadas y filtradas
-- Parte de: Optimización de carga de servicios con paginación backend

-- Índice para búsquedas por mensajero
CREATE INDEX idx_service_messenger_id ON service_deliveries(messenger_id);

-- Índice para filtrado por estado
CREATE INDEX idx_service_status ON service_deliveries(current_status);

-- Índice para ordenamiento por fecha de creación (DESC para más recientes primero)
CREATE INDEX idx_service_created_at ON service_deliveries(created_at DESC);

-- Índice para filtrado de eliminados
CREATE INDEX idx_service_deleted ON service_deliveries(deleted);

-- Índice compuesto para patrón común: servicios activos de un mensajero ordenados por fecha
-- Cubre la consulta más frecuente: servicios no eliminados de un mensajero específico
CREATE INDEX idx_service_messenger_active ON service_deliveries(messenger_id, deleted, created_at DESC);
