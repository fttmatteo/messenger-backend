package app.domain.services;

import java.time.LocalDateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import app.domain.exception.BusinessException;
import app.domain.model.Employee;
import app.domain.model.ServiceDelivery;
import app.domain.model.StatusHistory;
import app.domain.model.enums.Role;
import app.domain.model.enums.Status;
import app.domain.ports.ArchivePort;
import app.domain.ports.EmployeePort;
import app.domain.ports.ServiceDeliveryPort;

/**
 * Servicio para eliminar (soft delete) servicios de entrega.
 */
@Service
public class DeleteServiceDelivery {

    private static final Logger logger = LoggerFactory.getLogger(DeleteServiceDelivery.class);

    @Autowired
    private ServiceDeliveryPort serviceDeliveryPort;

    @Autowired
    private EmployeePort employeePort;

    @Autowired
    private ArchivePort archivePort;

    /**
     * Mueve un servicio a la papelera (soft delete).
     * El servicio permanecerá en la papelera por 60 días antes de ser archivado
     * permanentemente.
     */
    public void deleteById(Long id) throws Exception {
        ServiceDelivery service = validateServiceExists(id);

        service.setDeleted(true);
        service.setDeletedAt(LocalDateTime.now());

        serviceDeliveryPort.save(service);
        logger.info("Servicio ID {} movido a la papelera (Soft Delete de sistema/automático)", id);
    }

    /**
     * Mueve un servicio a la papelera con registro de quién lo eliminó.
     */
    public void deleteById(Long id, Long userId) throws Exception {
        ServiceDelivery service = validateServiceExists(id);

        Employee user = employeePort.findById(userId);
        if (user == null) {
            logger.warn("Intento de eliminar servicio con usuario inexistente: ID {}", userId);
            throw new BusinessException("Usuario indicado no existe.");
        }

        Status previousStatus = service.getCurrentStatus();

        StatusHistory history = new StatusHistory();
        history.setPreviousStatus(previousStatus);
        history.setNewStatus(Status.DELETED);
        history.setChangeDate(LocalDateTime.now());
        history.setChangedBy(user);
        service.addHistory(history);
        service.setDeleted(true);
        service.setDeletedAt(LocalDateTime.now());

        serviceDeliveryPort.save(service);
        logger.info("Servicio ID {} movido a la papelera por el usuario ID {} (Anterior estado: {})",
                id, userId, previousStatus);
    }

    /**
     * Restaura un servicio previamente eliminado de la papelera.
     * Solo permitido para administradores.
     */
    public ServiceDelivery restore(Long id, Long userId) throws Exception {
        ServiceDelivery service = serviceDeliveryPort.findById(id);
        if (service == null) {
            logger.warn("Intento de restaurar servicio inexistente: ID {}", id);
            throw new BusinessException("El servicio no existe.");
        }

        if (!service.isDeleted()) {
            logger.warn("Intento de restaurar servicio no eliminado: ID {}", id);
            throw new BusinessException("El servicio no está en la papelera.");
        }

        Employee user = employeePort.findById(userId);
        if (user == null) {
            logger.warn("Intento de restaurar servicio con usuario inexistente: ID {}", userId);
            throw new BusinessException("Usuario indicado no existe.");
        }

        if (user.getRole() != Role.ADMIN) {
            logger.warn("Intento de restaurar servicio por usuario sin permisos: ID {}", userId);
            throw new BusinessException("Solo administradores pueden restaurar servicios.");
        }

        service.setDeleted(false);
        service.setDeletedAt(null);

        StatusHistory history = new StatusHistory();
        history.setPreviousStatus(Status.DELETED);
        history.setNewStatus(service.getCurrentStatus());
        history.setChangeDate(LocalDateTime.now());
        history.setChangedBy(user);
        service.addHistory(history);

        ServiceDelivery restored = serviceDeliveryPort.save(service);
        logger.info("Servicio ID {} restaurado de la papelera por el administrador ID {}", id, userId);
        return restored;
    }

    /**
     * Archiva permanentemente un servicio de la papelera.
     * El servicio se mueve a la tabla de archivo en lugar de ser borrado.
     */
    public void archiveService(Long id) throws Exception {
        ServiceDelivery service = serviceDeliveryPort.findById(id);
        if (service == null) {
            logger.warn("Intento de archivar servicio inexistente: ID {}", id);
            throw new BusinessException("El servicio no existe.");
        }

        if (!service.isDeleted()) {
            logger.warn("Intento de archivar servicio no eliminado: ID {}", id);
            throw new BusinessException("Solo se pueden archivar servicios en la papelera.");
        }

        archivePort.archiveService(service, null, "Manual archive");
        logger.info("Servicio ID {} archivado permanentemente", id);
    }

    /**
     * Vacía la papelera archivando permanentemente todos los elementos.
     * Los servicios se mueven al archivo permanente en lugar de ser borrados.
     */
    public int emptyTrash() {
        int totalArchived = 0;
        int pageSize = 100;
        org.springframework.data.domain.Page<ServiceDelivery> page;

        logger.info("Iniciando proceso para vaciar papelera (archivado automático)...");
        do {
            page = serviceDeliveryPort.findDeleted(org.springframework.data.domain.PageRequest.of(0, pageSize));
            if (page.isEmpty()) {
                break;
            }

            for (ServiceDelivery service : page.getContent()) {
                try {
                    archivePort.archiveService(service, null, "Manual trash empty");
                    totalArchived++;
                } catch (Exception e) {
                    logger.error("Error archivando servicio {}: {}", service.getIdServiceDelivery(), e.getMessage());
                }
            }
        } while (page.hasNext());

        logger.info("Proceso de vaciado de papelera terminado. Total archivados: {}", totalArchived);
        return totalArchived;
    }

    /**
     * Valida que un servicio exista y esté activo (no eliminado).
     * 
     * @param id ID del servicio a validar
     * @return El servicio si existe y está activo
     * @throws BusinessException si el servicio no existe o está en la papelera
     */
    private ServiceDelivery validateServiceExists(Long id) throws BusinessException {
        ServiceDelivery service = serviceDeliveryPort.findByIdActive(id);
        if (service == null) {
            logger.warn("Intento de eliminar servicio inexistente: ID {}", id);
            throw new BusinessException("El servicio no existe o ya está en la papelera.");
        }
        return service;
    }

    /**
     * Elimina permanentemente un servicio de la papelera.
     */
    public void permanentDeleteById(Long id, Long userId) throws Exception {
        ServiceDelivery service = serviceDeliveryPort.findById(id);
        if (service == null) {
            logger.warn("Intento de eliminar permanentemente servicio inexistente: ID {}", id);
            throw new BusinessException("El servicio no existe.");
        }

        if (!service.isDeleted()) {
            logger.warn("Intento de eliminar permanentemente servicio no eliminado: ID {}", id);
            throw new BusinessException("Solo se pueden eliminar permanentemente servicios en la papelera.");
        }

        Employee user = employeePort.findById(userId);
        if (user == null) {
            logger.warn("Intento de eliminar permanentemente con usuario inexistente: ID {}", userId);
            throw new BusinessException("Usuario indicado no existe.");
        }

        archivePort.archiveService(service, userId, "Permanent delete by user");
        logger.info("Servicio ID {} eliminado permanentemente por usuario ID {}", id, userId);
    }
}
