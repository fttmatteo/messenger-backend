package app.domain.ports;

import app.domain.model.Employee;
import java.util.List;

public interface EmployeePort {

    Employee save(Employee employee);

    void deleteById(Long idEmployee);

    Employee findById(Long idEmployee);

    Employee findByDocument(Long document);

    List<Employee> findAll();
}