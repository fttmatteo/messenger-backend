package app.domain.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import app.application.exceptions.BusinessException;
import app.domain.model.Employee;
import app.domain.ports.EmployeePort;

@Service
public class DeleteEmployee {

    @Autowired
    private EmployeePort employeePort;

    public void deleteByDocument(Long document) throws Exception {
        try {
            Employee existing = employeePort.findByDocument(document);
            if (existing == null) {
                throw new BusinessException("El empleado con documento " + document + " no existe");
            }
            employeePort.deleteByDocument(document);

        } catch (DataIntegrityViolationException dive) {
            throw new BusinessException(
                    "No se puede eliminar el usuario porque tiene registros asociados en el sistema");
        } catch (Exception e) {
            throw e;
        }
    }
}