package app.domain.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import app.domain.exception.BusinessException;
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

    /**
     * Elimina un empleado validando que no tenga servicios asignados.
     */
    public void deleteById(Long id) throws Exception {
        Employee employee = employeePort.findById(id);
        if (employee == null) {
            throw new BusinessException("El empleado con ID " + id + " no existe.");
        }

        // Check if this employee (as messenger) has any associated service deliveries
        var deliveries = serviceDeliveryPort.findByMessengerId(id);
        if (deliveries != null && !deliveries.isEmpty()) {
            throw new BusinessException("No se puede eliminar. El empleado tiene " + deliveries.size()
                    + " servicios de entrega asociados.");
        }

        employeePort.deleteById(id);
    }
}