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
import app.infrastructure.service.ArchiveServiceService;

/**
 * Job programado para archivar servicios de la papelera después de 60 días.
 * Los servicios se mueven al archivo permanente en lugar de ser borrados.
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

    @Autowired
    private ArchiveServiceService archiveServiceService;

    @Scheduled(cron = "${app.trash.cleanup-cron:0 0 3 * * ?}")
    public void cleanupExpiredTrash() {

        LocalDateTime expirationDate = LocalDateTime.now().minusDays(retentionDays);
        List<ServiceDelivery> expiredServices = serviceDeliveryPort.findDeletedExpiredBefore(expirationDate);

        if (expiredServices.isEmpty()) {
            return;
        }


        int archivedCount = 0;
        int errorCount = 0;

        for (ServiceDelivery service : expiredServices) {
            try {
                archiveServiceService.archiveService(service, null, "Auto-archive after " + retentionDays + " days");
                archivedCount++;
            } catch (Exception e) {
                errorCount++;
                logger.error("Error archivando servicio ID: {}: {}", service.getIdServiceDelivery(), e.getMessage());
            }
        }

        logger.info("Trash cleanup completed: {} services archived, {} errors.", archivedCount, errorCount);
    }
}
