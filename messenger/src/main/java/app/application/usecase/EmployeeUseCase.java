package app.application.usecase;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import app.domain.model.Employee;
import app.domain.services.CreateEmployee;
import app.domain.services.DeleteEmployee;
import app.domain.services.SearchEmployee;
import app.domain.services.UpdateEmployee;

@Service
public class EmployeeUseCase {

    @Autowired
    private CreateEmployee createEmployee;
    @Autowired
    private UpdateEmployee updateEmployee;
    @Autowired
    private SearchEmployee searchEmployee;
    @Autowired
    private DeleteEmployee deleteEmployee;

    public Employee create(Employee employee) throws Exception {
        return createEmployee.create(employee);
    }

    public Employee update(Long id, Employee employee) throws Exception {
        return updateEmployee.update(id, employee);
    }

    public Employee findById(Long id) {
        return searchEmployee.findById(id);
    }

    public Employee findByDocument(Long document) throws Exception {
        return searchEmployee.findByDocument(document);
    }

    public List<Employee> findAll() {
        return searchEmployee.findAll();
    }

    public void deleteById(Long id) throws Exception {
        deleteEmployee.deleteById(id);
    }
}