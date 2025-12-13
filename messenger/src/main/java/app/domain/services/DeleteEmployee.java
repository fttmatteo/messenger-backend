package app.domain.services;

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

    @Autowired
    private EmployeePort employeePort;
    @Autowired
    private ServiceDeliveryPort serviceDeliveryPort;

    public void deleteByDocument(Long document) throws Exception {
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
    }

    public void deleteById(Long id) throws Exception {
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
    }
}