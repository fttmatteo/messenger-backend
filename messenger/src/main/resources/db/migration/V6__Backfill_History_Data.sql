-- Backfill observation and signature_id from service_deliveries to the latest status_history entry for each service

UPDATE status_history sh
JOIN (
    SELECT id_service_delivery, observation, signature_id, current_status
    FROM service_deliveries
) sd ON sh.service_delivery_id = sd.id_service_delivery
SET 
    sh.observation = sd.observation,
    sh.signature_id = sd.signature_id
WHERE 
    sh.new_status = sd.current_status
    AND sh.id_status_history = (
        SELECT MAX(sh2.id_status_history)
        FROM status_history sh2
        WHERE sh2.service_delivery_id = sh.service_delivery_id
    )
    AND (sd.observation IS NOT NULL OR sd.signature_id IS NOT NULL);
