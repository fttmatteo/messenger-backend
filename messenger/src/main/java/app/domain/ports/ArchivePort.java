package app.domain.ports;

import app.domain.model.ServiceDelivery;
import java.util.List;

/**
 * Puerto para archivar servicios eliminados.
 * Permite al dominio archivar servicios sin depender de infraestructura.
 */
public interface ArchivePort {

    /**
     * Archiva un servicio permanentemente.
     */
    void archiveService(ServiceDelivery service, Long deletedByEmployeeId, String deletionReason);

    /**
     * Archiva múltiples servicios en batch.
     */
    void archiveServices(List<ServiceDelivery> services, Long deletedByEmployeeId, String deletionReason);
}
