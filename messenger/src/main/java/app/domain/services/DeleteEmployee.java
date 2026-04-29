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

        var deliveriesPage = serviceDeliveryPort.findByMessengerPaginated(id, null, null, null,
                org.springframework.data.domain.PageRequest.of(0, 1));
        if (deliveriesPage.getTotalElements() > 0) {
            throw new BusinessException("No se puede eliminar. El empleado tiene " + deliveriesPage.getTotalElements()
                    + " servicios de entrega asociados.");
        }

        employeePort.deleteById(id);
    }
}