package app.domain.services;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import app.adapter.in.validators.EmployeeValidator;
import app.application.exceptions.BusinessException;
import app.domain.model.Employee;
import app.domain.ports.EmployeePort;

@Service
public class EmployeeService {

    @Autowired
    private EmployeePort employeePort;
    @Autowired
    private PasswordEncoder passwordEncoder;
    @Autowired
    private EmployeeValidator validator;

    public void create(Employee employee) throws Exception {
        // Delegamos todas las validaciones al validator
        validator.validateCreation(employee);
        // Encriptar y guardar
        employee.setPassword(passwordEncoder.encode(employee.getPassword()));
        employeePort.save(employee);
    }

    public void update(Employee employee) throws Exception {
        Employee existing = employeePort.findById(employee.getIdEmployee());
        if (existing == null) {
            throw new BusinessException("El empleado no existe.");
        }
        validator.validateUpdate(employee, existing);
        // Lógica de contraseña
        if (employee.getPassword() == null || employee.getPassword().trim().isEmpty()) {
            employee.setPassword(existing.getPassword());
        } else {
            employee.setPassword(passwordEncoder.encode(employee.getPassword()));
        }
        employeePort.update(employee);
    }

    public List<Employee> findAll() {
        return employeePort.findAll();
    }

    public Employee findById(Long id) {
        return employeePort.findById(id);
    }
    
    public Employee findByDocument(Long document) throws Exception {
        return employeePort.findByDocument(document);
    }
    
    public Employee findByUserName(String userName) {
        return employeePort.findByUserName(userName);
    }

    public void deleteById(Long id) throws Exception {
        Employee employee = employeePort.findById(id);
        if (employee == null) throw new BusinessException("Empleado no encontrado");
        validator.validateDelete(employee.getDocument());
        employeePort.deleteById(id);
    }
    
    public void deleteByDocument(Long document) throws Exception {
        if (employeePort.findByDocument(document) == null) throw new BusinessException("Empleado no encontrado");
        validator.validateDelete(document);
        employeePort.deleteByDocument(document);
    }
}