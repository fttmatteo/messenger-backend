-- Backfill observation and signature_id from service_deliveries to the latest status_history entry for each service

UPDATE status_history sh
JOIN service_deliveries sd ON sh.service_delivery_id = sd.id_service_delivery
JOIN (
    SELECT MAX(id_status_history) as max_id
    FROM status_history
    GROUP BY service_delivery_id
) latest_sh ON sh.id_status_history = latest_sh.max_id
SET 
    sh.observation = sd.observation,
    sh.signature_id = sd.signature_id
WHERE 
    sh.new_status = sd.current_status
    AND (sd.observation IS NOT NULL OR sd.signature_id IS NOT NULL);
