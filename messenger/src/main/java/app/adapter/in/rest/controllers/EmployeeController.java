package app.adapter.in.rest.controllers;

import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import app.adapter.in.builder.EmployeeBuilder;
import app.adapter.in.rest.mapper.EmployeeResponseMapper;
import app.adapter.in.rest.request.EmployeeRequest;
import app.adapter.in.rest.response.EmployeeResponse;
import app.application.usecase.EmployeeUseCase;
import app.domain.model.Employee;
import app.infrastructure.audit.AuditableAction;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Controlador REST para gestionar empleados y mensajeros.
 * 
 * Proporciona operaciones CRUD exclusivas para administradores.
 * Todos los endpoints requieren rol ADMIN.
 */
@RestController
@RequestMapping("/employees")
@PreAuthorize("hasRole('ADMIN')")
public class EmployeeController {

    @Autowired
    private EmployeeUseCase employeeUseCase;
    @Autowired
    private EmployeeBuilder builder;
    @Autowired
    private EmployeeResponseMapper responseMapper;

    @PostMapping("/createEmployee")
    @PreAuthorize("hasRole('ADMIN')")
    @AuditableAction(action = "CREATE_EMPLOYEE", description = "Crear nuevo empleado")
    public ResponseEntity<String> create(@Valid @RequestBody EmployeeRequest request) throws Exception {
        Employee employee = builder.build(request.getDocument(),
                request.getFullName(),
                request.getPhone(),
                request.getUserName(),
                request.getPassword(),
                request.getRole());
        employeeUseCase.create(employee);
        return ResponseEntity.status(HttpStatus.CREATED).body("Empleado creado exitosamente");
    }

    @GetMapping("/allEmployees")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<EmployeeResponse>> findAll() {
        List<EmployeeResponse> responses = employeeUseCase.findAll().stream()
                .map(responseMapper::toResponse)
                .collect(Collectors.toList());
        return ResponseEntity.ok(responses);
    }

    @GetMapping("/findEmployee/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<EmployeeResponse> findById(@PathVariable Long id) {
        Employee employee = employeeUseCase.findById(id);
        if (employee == null) {
            throw new app.application.exceptions.ResourceNotFoundException("Empleado con ID " + id + " no encontrado");
        }
        return ResponseEntity.ok(responseMapper.toResponse(employee));
    }

    @PutMapping("/updateEmployee/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @AuditableAction(action = "UPDATE_EMPLOYEE", description = "Actualizar empleado existente")
    public ResponseEntity<String> update(@PathVariable Long id, @Valid @RequestBody EmployeeRequest request)
            throws Exception {
        Employee employee = builder.build(
                request.getDocument(),
                request.getFullName(),
                request.getPhone(),
                request.getUserName(),
                request.getPassword(),
                request.getRole());
        employeeUseCase.update(id, employee);
        return ResponseEntity.ok("Empleado actualizado exitosamente");
    }

    @DeleteMapping("/deleteEmployee/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @AuditableAction(action = "DELETE_EMPLOYEE", description = "Eliminar empleado")
    public ResponseEntity<String> delete(@PathVariable Long id) throws Exception {
        employeeUseCase.deleteById(id);
        return ResponseEntity.ok("Empleado eliminado exitosamente");
    }
}
