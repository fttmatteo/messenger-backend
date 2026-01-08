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
import app.infrastructure.logging.LogSanitizer;
import java.util.List;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Controlador REST para gestión de empleados (solo ADMIN).
 */
@RestController
@RequestMapping("/employees")
@PreAuthorize("hasRole('ADMIN')")
public class EmployeeController {

    private static final Logger logger = LoggerFactory.getLogger(EmployeeController.class);

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
        logger.info("Solicitud para crear empleado documento: {}",
            LogSanitizer.maskDocument(request.getDocument()));
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
     * Busca un empleado por su ID (solo ADMIN).
     */
    @GetMapping("/findByEmployeeId/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<EmployeeResponse> findById(@PathVariable Long id) {
        Employee employee = employeeUseCase.findById(id);
        if (employee == null) {
            throw new app.domain.exception.ResourceNotFoundException("Empleado con ID " + id + " no encontrado");
        }
        return ResponseEntity.ok(responseMapper.toResponse(employee));
    }

    /**
     * Actualiza los datos de un empleado (solo ADMIN).
     */
    @PutMapping("/updateEmployee/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @AuditableAction(action = "UPDATE_EMPLOYEE", description = "Actualizar empleado existente")
    public ResponseEntity<EmployeeResponse> update(@PathVariable Long id, @Valid @RequestBody EmployeeRequest request)
            throws Exception {
        Employee employee = builder.buildForUpdate(
                request.getDocument(),
                request.getFullName(),
                request.getPhone(),
                request.getPassword(),
                request.getRole());
        Employee updated = employeeUseCase.update(id, employee);
        return ResponseEntity.ok(responseMapper.toResponse(updated));
    }

    /**
     * Elimina un empleado por su ID (solo ADMIN).
     */
    @DeleteMapping("/deleteEmployee/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @AuditableAction(action = "DELETE_EMPLOYEE", description = "Eliminar empleado")
    public ResponseEntity<Void> delete(@PathVariable Long id) throws Exception {
        logger.info("Solicitud para eliminar empleado ID: {}", id);
        employeeUseCase.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}
