package app.infrastructure.scheduler;

import java.time.LocalDateTime;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import app.domain.model.ServiceDelivery;
import app.domain.ports.ServiceDeliveryPort;
import app.application.usecase.delivery.UpdateServiceDeliveryUseCase;
import net.javacrumbs.shedlock.spring.annotation.SchedulerLock;

/**
 * Planificador en segundo plano para activar servicios programados.
 * Se ejecuta cada minuto, busca servicios en estado SCHEDULED cuya fecha programada ya llegó
 * y los transiciona a ASSIGNED.
 */
@Component
public class ServiceActivationScheduler {

    private static final Logger logger = LoggerFactory.getLogger(ServiceActivationScheduler.class);

    @Autowired
    private ServiceDeliveryPort serviceDeliveryPort;

    @Autowired
    private UpdateServiceDeliveryUseCase updateServiceDeliveryUseCase;

    @Scheduled(cron = "${app.service.activation-cron:0 * * * * ?}")
    @SchedulerLock(name = "service_activation_lock", lockAtMostFor = "PT5M", lockAtLeastFor = "PT30S")
    @Transactional(rollbackFor = Exception.class)
    public void activateScheduledServices() {
        LocalDateTime now = LocalDateTime.now();
        List<ServiceDelivery> pendingServices = serviceDeliveryPort.findScheduledPendingActivation(now);

        if (pendingServices.isEmpty()) {
            return;
        }

        logger.info("Encontrados {} servicios programados para activar.", pendingServices.size());

        int activatedCount = 0;
        int errorCount = 0;

        for (ServiceDelivery service : pendingServices) {
            try {
                updateServiceDeliveryUseCase.activateScheduledService(service.getIdServiceDelivery());
                activatedCount++;
            } catch (Exception e) {
                errorCount++;
                logger.error("Error activando servicio programado: {}", e.getMessage());
            }
        }

        if (activatedCount > 0 || errorCount > 0) {
            logger.info("Activación de servicios programados completada: {} activados, {} errores.", activatedCount, errorCount);
        }
    }
}
