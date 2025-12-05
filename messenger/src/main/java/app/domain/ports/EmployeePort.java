package app.domain.ports;

import app.domain.model.Employee;

public interface EmployeePort {
    Employee findByDocument(Employee employee) throws Exception;
    void save(Employee employee) throws Exception;
    void deleteByDocument(Long document) throws Exception;
    void update(Employee employee) throws Exception;
}
