package app.adapter.in.rest.controllers;

import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import app.adapter.in.builder.EmployeeBuilder;
import app.adapter.in.rest.request.EmployeeRequest;
import app.application.exceptions.BusinessException;
import app.application.exceptions.InputsException;
import app.application.usecase.AdminUseCase;
import app.domain.model.Employee;
import app.adapter.in.validators.EmployeeValidator;

@RestController
@RequestMapping("/employees")
    @PreAuthorize("hasRole('ADMIN')")
public class AdminController {

    @Autowired
    private EmployeeBuilder employeeBuilder;
    @Autowired
    private AdminUseCase adminUseCase;

    @Autowired
    private EmployeeValidator employeeValidator;

    @PostMapping("/messenger")
        @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> createMessenger(@RequestBody EmployeeRequest request) {
        try {
            Employee employee = employeeBuilder.build(
                    request.getDocument(),
                    request.getFullName(),
                    request.getPhone(),
                    request.getUserName(),
                    request.getPassword(),
                    request.getZone()
            );
            adminUseCase.createMessenger(employee);
            return ResponseEntity.status(HttpStatus.CREATED).body(employee);
        } catch (InputsException ie) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ie.getMessage());
        } catch (BusinessException be) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(be.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }
    }

    @DeleteMapping("/{document}")
        @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> deleteEmployee(@PathVariable String document) {
        try {
            long doc = employeeValidator.documentValidator(document);
            adminUseCase.deleteEmployee(doc);
            return ResponseEntity.ok(Map.of("message", "empleado eliminado"));
        } catch (InputsException ie) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ie.getMessage());
        } catch (BusinessException be) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(be.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }
    }
}