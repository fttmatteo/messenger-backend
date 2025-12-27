package app.domain.services;

import java.time.LocalDateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import app.domain.exception.BusinessException;
import app.domain.model.Employee;
import app.domain.model.ServiceDelivery;
import app.domain.model.StatusHistory;
import app.domain.model.enums.Role;
import app.domain.model.enums.Status;
import app.domain.ports.EmployeePort;
import app.domain.ports.ServiceDeliveryPort;

/**
 * Servicio para eliminar (soft delete) servicios de entrega.
 * 
 * Los servicios eliminados van a una papelera y se borran permanentemente
 * después de 60 días.
 */
@Service
public class DeleteServiceDelivery {

    @Autowired
    private ServiceDeliveryPort serviceDeliveryPort;
    @Autowired
    private EmployeePort employeePort;

    /**
     * Mueve un servicio a la papelera (soft delete).
     * El servicio permanecerá en la papelera por 60 días antes de ser eliminado
     * permanentemente.
     */
    /**
     * Mueve un servicio a la papelera (soft delete).
     * El servicio permanecerá en la papelera por 60 días antes de ser eliminado
     * permanentemente.
     */
    public void deleteById(Long id) throws Exception {
        ServiceDelivery service = serviceDeliveryPort.findByIdActive(id);
        if (service == null) {
            throw new BusinessException(
                    "El servicio de entrega que intenta eliminar no existe o ya está en la papelera.");
        }

        // Marcar como eliminado (soft delete)
        service.setDeleted(true);
        service.setDeletedAt(LocalDateTime.now());

        serviceDeliveryPort.save(service);
    }

    /**
     * Mueve un servicio a la papelera con registro de quién lo eliminó.
     */
    /**
     * Mueve un servicio a la papelera registrando el usuario que realizó la acción.
     */
    public void deleteById(Long id, Long userId) throws Exception {
        ServiceDelivery service = serviceDeliveryPort.findByIdActive(id);
        if (service == null) {
            throw new BusinessException(
                    "El servicio de entrega que intenta eliminar no existe o ya está en la papelera.");
        }

        Employee user = employeePort.findById(userId);
        if (user == null) {
            throw new BusinessException("Usuario no encontrado.");
        }

        Status previousStatus = service.getCurrentStatus();

        // Registrar en el historial quien eliminó el servicio
        StatusHistory history = new StatusHistory();
        history.setPreviousStatus(previousStatus);
        history.setNewStatus(Status.DELETED); // El estado no cambia, solo se marca como eliminado
        history.setChangeDate(LocalDateTime.now());
        history.setChangedBy(user);
        service.addHistory(history);

        // Marcar como eliminado (soft delete)
        service.setDeleted(true);
        service.setDeletedAt(LocalDateTime.now());

        serviceDeliveryPort.save(service);
    }

    /**
     * Restaura un servicio desde la papelera.
     */
    /**
     * Restaura un servicio previamente eliminado de la papelera.
     * Solo permitido para administradores.
     */
    public ServiceDelivery restore(Long id, Long userId) throws Exception {
        ServiceDelivery service = serviceDeliveryPort.findById(id);
        if (service == null) {
            throw new BusinessException("El servicio de entrega no existe.");
        }

        if (!service.isDeleted()) {
            throw new BusinessException("El servicio no está en la papelera.");
        }

        Employee user = employeePort.findById(userId);
        if (user == null) {
            throw new BusinessException("Usuario no encontrado.");
        }

        if (user.getRole() != Role.ADMIN) {
            throw new BusinessException("Solo los administradores pueden restaurar servicios de la papelera.");
        }

        service.setDeleted(false);
        service.setDeletedAt(null);

        // Registrar restauración en el historial
        StatusHistory history = new StatusHistory();
        history.setPreviousStatus(Status.DELETED);
        history.setNewStatus(service.getCurrentStatus());
        history.setChangeDate(LocalDateTime.now());
        history.setChangedBy(user);
        service.addHistory(history);

        return serviceDeliveryPort.save(service);
    }

    /**
     * Elimina permanentemente un servicio de la papelera.
     * Solo para uso administrativo o por el job de limpieza.
     */
    /**
     * Elimina permanentemente un servicio de la papelera (Hard Delete).
     */
    public void hardDelete(Long id) throws Exception {
        ServiceDelivery service = serviceDeliveryPort.findById(id);
        if (service == null) {
            throw new BusinessException("El servicio de entrega no existe.");
        }

        if (!service.isDeleted()) {
            throw new BusinessException("Solo se pueden eliminar permanentemente servicios que estén en la papelera.");
        }

        serviceDeliveryPort.hardDeleteById(id);
    }

    /**
     * Vacía la papelera eliminando permanentemente todos los servicios eliminados.
     * 
     * @return número de servicios eliminados permanentemente
     */
    /**
     * Vacía la papelera eliminando permanentemente todos los elementos.
     */
    public int emptyTrash() {
        return serviceDeliveryPort.hardDeleteAllDeleted();
    }
}