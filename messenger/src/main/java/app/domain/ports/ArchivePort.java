package app.domain.ports;

import app.domain.model.ServiceDelivery;
import java.util.List;

/**
 * Puerto para archivar servicios eliminados.
 * Permite al dominio archivar servicios sin depender de infraestructura.
 */
public interface ArchivePort {
    void archiveService(ServiceDelivery service, Long deletedByEmployeeId, String deletionReason);

    void archiveServices(List<ServiceDelivery> services, Long deletedByEmployeeId, String deletionReason);
}
