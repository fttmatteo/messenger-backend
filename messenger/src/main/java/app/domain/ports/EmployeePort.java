package app.domain.ports;

import app.domain.model.Employee;
import java.util.List;

public interface EmployeePort {

    Employee save(Employee employee);

    void deleteById(Long idEmployee);

    Employee findById(Long idEmployee);

    boolean existsByDocument(Long document);

    Employee findByUserName(String userName);

    List<Employee> findAll();
}