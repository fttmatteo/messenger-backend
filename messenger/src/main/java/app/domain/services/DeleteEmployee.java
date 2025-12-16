package app.domain.services;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import app.application.exceptions.BusinessException;
import app.domain.model.Employee;
import app.domain.ports.EmployeePort;
import app.domain.ports.ServiceDeliveryPort;

/**
 * Servicio de dominio para eliminar empleados.
 * 
 * Valida que el empleado no tenga servicios de entrega asociados antes
 * de permitir su eliminación, manteniendo la integridad referencial.
 */
@Service
public class DeleteEmployee {

    private static final Logger logger = LoggerFactory.getLogger(DeleteEmployee.class);

    @Autowired
    private EmployeePort employeePort;
    @Autowired
    private ServiceDeliveryPort serviceDeliveryPort;

    /**
     * Elimina un empleado por su número de documento.
     * 
     * @param document Número de documento del empleado.
     * @throws Exception Si el empleado no existe o tiene servicios asociados.
     */
    public void deleteByDocument(Long document) throws Exception {
        logger.warn("Solicitud de eliminación de empleado por documento: {}", document);
        Employee employee = employeePort.findByDocument(document);
        if (employee == null) {
            throw new BusinessException("El empleado con documento " + document + " no existe.");
        }

        var deliveries = serviceDeliveryPort.findByMessengerDocument(document);
        if (deliveries != null && !deliveries.isEmpty()) {
            throw new BusinessException(
                    "No se puede eliminar el empleado porque tiene servicios de entrega asociados.");
        }

        employeePort.deleteByDocument(document);
        logger.info("Empleado eliminado por documento: {} ({})", document, employee.getFullName());
    }

    /**
     * Elimina un empleado por su ID.
     * 
     * @param id ID del empleado.
     * @throws Exception Si el empleado no existe o tiene servicios asociados.
     */
    public void deleteById(Long id) throws Exception {
        logger.warn("Solicitud de eliminación de empleado por ID: {}", id);
        Employee employee = employeePort.findById(id);
        if (employee == null) {
            throw new BusinessException("El empleado con ID " + id + " no existe.");
        }

        var deliveries = serviceDeliveryPort.findByMessengerDocument(employee.getDocument());
        if (deliveries != null && !deliveries.isEmpty()) {
            throw new BusinessException(
                    "No se puede eliminar el empleado porque tiene servicios de entrega asociados.");
        }

        employeePort.deleteById(id);
        logger.info("Empleado eliminado: ID {} ({})", id, employee.getFullName());
    }
}