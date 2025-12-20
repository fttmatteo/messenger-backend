package app.domain.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import app.application.exceptions.BusinessException;
import app.domain.model.Employee;
import app.domain.ports.EmployeePort;
import app.domain.ports.ServiceDeliveryPort;

/**
 * Servicio para eliminar empleados validando dependencias.
 */
@Service
public class DeleteEmployee {

    @Autowired
    private EmployeePort employeePort;
    @Autowired
    private ServiceDeliveryPort serviceDeliveryPort;

    public void deleteById(Long id) throws Exception {
        Employee employee = employeePort.findById(id);
        if (employee == null) {
            throw new BusinessException("El empleado con ID " + id + " no existe.");
        }

        var deliveries = serviceDeliveryPort.findById(employee.getDocument());
        if (deliveries != null) {
            throw new BusinessException("El empleado con ID " + id + " tiene servicios de entrega asociados.");
        }

        employeePort.deleteById(id);
    }
}