package app.domain.services;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

    private static final Logger logger = LoggerFactory.getLogger(DeleteEmployee.class);

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
            logger.warn("Intento de eliminar empleado inexistente.");
            throw new BusinessException("El empleado indicado no existe.");
        }

        var deliveriesPage = serviceDeliveryPort.findByMessengerPaginated(id, null, null, null,
                org.springframework.data.domain.PageRequest.of(0, 1));
        if (deliveriesPage.getTotalElements() > 0) {
            logger.warn("Intento de eliminar empleado con servicios asignados.");
            throw new BusinessException("No se puede eliminar. El empleado tiene servicios de entrega asociados.");
        }

        employeePort.deleteById(id);
    }
}
