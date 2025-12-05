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

    public void deleteByDocument(long document) throws Exception {
        try {
            Employee probe = new Employee();
            probe.setDocument(document);
            Employee existing = employeePort.findByDocument(probe);
            if (existing == null) {
                throw new BusinessException("el empleado con documento " + document + " no existe");
            }
            employeePort.deleteByDocument(document);
        } catch (DataIntegrityViolationException dive) {
            throw new BusinessException(
                "no se puede eliminar el usuario porque tiene registros asociados en el sistema"
            );
        } catch (BusinessException be) {
            throw be;
        } catch (Exception e) {
            throw e;
        }
    }
}