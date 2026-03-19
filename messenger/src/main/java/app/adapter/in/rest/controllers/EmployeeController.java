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
 * Controlador REST para gestión de empleados (solo ADMIN).
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

    /**
     * Crea un nuevo empleado (solo ADMIN).
     */
    @PostMapping("/createEmployee")
    @PreAuthorize("hasRole('ADMIN')")
    @AuditableAction(action = "CREATE_EMPLOYEE", description = "Crear nuevo empleado")
    public ResponseEntity<EmployeeResponse> create(@Valid @RequestBody EmployeeRequest request) throws Exception {
        Employee employee = builder.build(request.getDocument(),
                request.getFullName(),
                request.getPhone(),
                request.getPassword(),
                request.getRole());
        Employee created = employeeUseCase.create(employee);
        return ResponseEntity.status(HttpStatus.CREATED).body(responseMapper.toResponse(created));
    }

    /**
     * Obtiene todos los empleados registrados (solo ADMIN).
     */
    @GetMapping("/allEmployees")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<EmployeeResponse>> findAll() {
        List<EmployeeResponse> responses = employeeUseCase.findAll().stream()
                .map(responseMapper::toResponse)
                .collect(Collectors.toList());
        return ResponseEntity.ok(responses);
    }

    /**
     * Busca un empleado por su UUID público (solo ADMIN).
     */
    @GetMapping("/findByEmployeeId/{uuid}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<EmployeeResponse> findByUuid(@PathVariable String uuid) {
        Employee employee = employeeUseCase.findByUuid(uuid);
        if (employee == null) {
            throw new app.domain.exception.ResourceNotFoundException("Empleado con UUID " + uuid + " no encontrado");
        }
        return ResponseEntity.ok(responseMapper.toResponse(employee));
    }

    /**
     * Actualiza los datos de un empleado (solo ADMIN).
     */
    @PutMapping("/updateEmployee/{uuid}")
    @PreAuthorize("hasRole('ADMIN')")
    @AuditableAction(action = "UPDATE_EMPLOYEE", description = "Actualizar empleado existente")
    public ResponseEntity<EmployeeResponse> update(@PathVariable String uuid, @Valid @RequestBody EmployeeRequest request)
            throws Exception {
        Employee employee = builder.buildForUpdate(
                request.getDocument(),
                request.getFullName(),
                request.getPhone(),
                request.getPassword(),
                request.getRole());
        Employee existing = employeeUseCase.findByUuid(uuid);
        Employee updated = employeeUseCase.update(existing.getIdEmployee(), employee);
        return ResponseEntity.ok(responseMapper.toResponse(updated));
    }

    /**
     * Elimina un empleado por su UUID (solo ADMIN).
     */
    @DeleteMapping("/deleteEmployee/{uuid}")
    @PreAuthorize("hasRole('ADMIN')")
    @AuditableAction(action = "DELETE_EMPLOYEE", description = "Eliminar empleado")
    public ResponseEntity<Void> delete(@PathVariable String uuid) throws Exception {
        Employee existing = employeeUseCase.findByUuid(uuid);
        employeeUseCase.deleteById(existing.getIdEmployee());
        return ResponseEntity.noContent().build();
    }
}
