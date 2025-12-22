-- V3: Delete legacy services with status FAILED
DELETE FROM service_deliveries WHERE current_status = 'FAILED';
