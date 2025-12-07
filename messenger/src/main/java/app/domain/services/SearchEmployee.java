package app.domain.services;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import app.domain.model.Employee;
import app.domain.ports.EmployeePort;

@Service
public class SearchEmployee {

    @Autowired
    private EmployeePort employeePort;

    public List<Employee> findAll() {
        return employeePort.findAll();
    }

    public Employee findByDocument(Long document) throws Exception {
        return employeePort.findByDocument(document);
    }
    
    public Employee findById(Long id) {
        return employeePort.findById(id);
    }
}