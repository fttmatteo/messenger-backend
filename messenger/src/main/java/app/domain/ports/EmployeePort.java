package app.domain.ports;

import app.domain.model.Employee;
import java.util.List;

public interface EmployeePort {
    void save(Employee employee);

    void deleteById(Long idEmployee);

    void deleteByDocument(Long document);

    Employee findById(Long idEmployee);

    Employee findByDocument(Long document);

    Employee findByUserName(String userName);

    List<Employee> findAll();
}