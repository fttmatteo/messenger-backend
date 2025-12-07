package app.domain.ports;

import app.domain.model.Employee;
import java.util.List;

public interface EmployeePort {
    Employee save(Employee employee);

    Employee update(Employee employee);

    void deleteById(Long idEmployee);

    void deleteByDocument(Long document);

    Employee findById(Long idEmployee);

    List<Employee> findAll();

    Employee findByDocument(Long document);

    Employee findByUserName(String userName);
}