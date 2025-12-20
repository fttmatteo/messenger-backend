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

    /**
     * Elimina un empleado por su ID.
     * 
     * @param id ID del empleado.
     * @throws Exception Si el empleado no existe o tiene servicios asociados.
     */
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