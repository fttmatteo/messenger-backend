package app.infrastructure.scheduler;

import java.time.LocalDateTime;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import app.domain.model.ServiceDelivery;
import app.domain.ports.ServiceDeliveryPort;

/**
 * Job programado para limpiar servicios de la papelera después de 60 días.
 * 
 * Se ejecuta diariamente a las 3:00 AM.
 */
@Component
public class TrashCleanupScheduler {

    private static final Logger logger = LoggerFactory.getLogger(TrashCleanupScheduler.class);

    @Value("${app.trash.retention-days:60}")
    private int retentionDays;

    @Autowired
    private ServiceDeliveryPort serviceDeliveryPort;

    /**
     * Ejecuta la limpieza de la papelera diariamente a las 3:00 AM.
     */
    @Scheduled(cron = "${app.trash.cleanup-cron:0 0 3 * * ?}")
    public void cleanupExpiredTrash() {
        logger.info(
                "Iniciando limpieza de papelera. Servicios eliminados hace más de {} días serán borrados permanentemente.",
                retentionDays);

        LocalDateTime expirationDate = LocalDateTime.now().minusDays(retentionDays);
        List<ServiceDelivery> expiredServices = serviceDeliveryPort.findDeletedExpiredBefore(expirationDate);

        if (expiredServices.isEmpty()) {
            logger.info("No hay servicios expirados en la papelera.");
            return;
        }

        logger.info("Encontrados {} servicios expirados en la papelera.", expiredServices.size());

        int deletedCount = 0;
        int errorCount = 0;

        for (ServiceDelivery service : expiredServices) {
            try {
                serviceDeliveryPort.hardDeleteById(service.getIdServiceDelivery());
                deletedCount++;
                logger.debug("Eliminado permanentemente servicio ID: {}", service.getIdServiceDelivery());
            } catch (Exception e) {
                errorCount++;
                logger.error("Error eliminando servicio ID: {}: {}", service.getIdServiceDelivery(), e.getMessage());
            }
        }

        logger.info("Limpieza de papelera completada. Eliminados: {}, Errores: {}", deletedCount, errorCount);
    }
}
