package app.adapter.in.rest.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import app.adapter.in.builder.EmployeeBuilder;
import app.adapter.in.rest.mapper.EmployeeResponseMapper;
import app.adapter.in.rest.request.EmployeeRequest;
import app.adapter.in.rest.response.EmployeeResponse;
import app.application.exceptions.BusinessException;
import app.application.exceptions.InputsException;
import app.application.usecase.EmployeeUseCase;
import app.domain.model.Employee;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Controlador REST para gestionar empleados y mensajeros.
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

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> create(@RequestBody EmployeeRequest request) {
        try {
            Employee employee = builder.build(request.getDocument(),
                    request.getFullName(),
                    request.getPhone(),
                    request.getUserName(),
                    request.getPassword(),
                    request.getRole());
            employeeUseCase.create(employee);
            return ResponseEntity.status(HttpStatus.CREATED).body("Empleado creado exitosamente");
        } catch (InputsException | BusinessException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<EmployeeResponse>> findAll() {
        List<EmployeeResponse> responses = employeeUseCase.findAll().stream()
                .map(responseMapper::toResponse)
                .collect(Collectors.toList());
        return ResponseEntity.ok(responses);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> findById(@PathVariable Long id) {
        try {
            Employee employee = employeeUseCase.findById(id);
            if (employee == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Empleado no encontrado");
            }
            return ResponseEntity.ok(responseMapper.toResponse(employee));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> update(@PathVariable Long id, @RequestBody EmployeeRequest request) {
        try {
            Employee employee = builder.build(
                    request.getDocument(),
                    request.getFullName(),
                    request.getPhone(),
                    request.getUserName(),
                    request.getPassword(),
                    request.getRole());
            employeeUseCase.update(id, employee);
            return ResponseEntity.ok("Empleado actualizado exitosamente");
        } catch (InputsException | BusinessException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> delete(@PathVariable Long id) {
        try {
            employeeUseCase.deleteById(id);
            return ResponseEntity.ok("Empleado eliminado exitosamente");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }
    }
}
