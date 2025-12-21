-- V2: Add soft delete (trash bin) and edit lock fields for service_deliveries

-- Soft delete: moves to trash instead of permanent deletion
ALTER TABLE service_deliveries ADD COLUMN deleted BOOLEAN DEFAULT FALSE;
ALTER TABLE service_deliveries ADD COLUMN deleted_at DATETIME(6) NULL;

-- Edit lock: timestamp when status changed to DELIVERED or RESOLVED (72h edit window)
ALTER TABLE service_deliveries ADD COLUMN locked_at DATETIME(6) NULL;

-- Index for efficient querying of non-deleted services
CREATE INDEX idx_service_deliveries_deleted ON service_deliveries(deleted);

-- Index for cleanup job to find expired trash items
CREATE INDEX idx_service_deliveries_deleted_at ON service_deliveries(deleted_at);
