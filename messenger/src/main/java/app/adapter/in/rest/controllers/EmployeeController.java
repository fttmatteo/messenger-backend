package app.adapter.in.rest.controllers;

import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
import app.domain.model.enums.Role;
import app.infrastructure.helper.SecurityHelper;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Controlador REST para gestión de empleados (solo ADMIN).
 * Aplica principio de mínimo privilegio: un administrador solo puede
 * gestionar empleados con rol MESSENGER, no a otros administradores.
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
    @Autowired
    private SecurityHelper securityHelper;

    /**
     * Crea un nuevo empleado (solo ADMIN, solo rol MESSENGER).
     * Bloquea la creación de nuevos administradores desde el panel.
     */
    @PostMapping("/createEmployee")
    @PreAuthorize("hasRole('ADMIN')")

    public ResponseEntity<EmployeeResponse> create(@Valid @RequestBody EmployeeRequest request) throws Exception {
        if ("ADMIN".equalsIgnoreCase(request.getRole())) {
            throw new app.domain.exception.BusinessException(
                    "No tiene permisos para crear administradores desde este panel.");
        }

        Employee employee = builder.build(request.getDocument(),
                request.getFullName(),
                request.getPhone(),
                request.getPassword(),
                request.getRole());
        Employee created = employeeUseCase.create(employee);
        return ResponseEntity.status(HttpStatus.CREATED).body(responseMapper.toResponse(created));
    }

    /**
     * Obtiene empleados visibles para el administrador autenticado (solo MESSENGER).
     * Un admin no ve a otros administradores en el listado.
     */
    @GetMapping("/allEmployees")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<EmployeeResponse>> findAll() {
        List<EmployeeResponse> responses = employeeUseCase.findByRole(Role.MESSENGER).stream()
                .map(responseMapper::toResponse)
                .collect(Collectors.toList());
        return ResponseEntity.ok(responses);
    }

    /**
     * Busca un empleado por su UUID público (solo ADMIN).
     * Bloquea consulta detallada de otros administradores (security through obscurity).
     */
    @GetMapping("/findByEmployeeId/{uuid}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<EmployeeResponse> findByUuid(@PathVariable String uuid) {
        Employee employee = employeeUseCase.findByUuid(uuid);
        if (employee == null) {
            throw new app.domain.exception.ResourceNotFoundException("Empleado con UUID " + uuid + " no encontrado");
        }

        Employee currentUser = securityHelper.getCurrentUser();
        if (employee.getRole() == Role.ADMIN
                && !employee.getIdEmployee().equals(currentUser.getIdEmployee())) {
            logger.warn("Admin intentó acceder al perfil de otro admin.");
            throw new app.domain.exception.ResourceNotFoundException("Empleado con UUID " + uuid + " no encontrado");
        }

        return ResponseEntity.ok(responseMapper.toResponse(employee));
    }

    /**
     * Actualiza los datos de un empleado (solo ADMIN).
     * Bloquea modificación de otros administradores.
     */
    @PutMapping("/updateEmployee/{uuid}")
    @PreAuthorize("hasRole('ADMIN')")

    public ResponseEntity<EmployeeResponse> update(@PathVariable String uuid, @Valid @RequestBody EmployeeRequest request)
            throws Exception {
        Employee target = employeeUseCase.findByUuid(uuid);
        Employee currentUser = securityHelper.getCurrentUser();

        if (target.getRole() == Role.ADMIN
                && !target.getIdEmployee().equals(currentUser.getIdEmployee())) {
            logger.warn("Admin intentó modificar a otro admin.");
            throw new app.domain.exception.BusinessException(
                    "No tiene permisos para modificar a otro administrador.");
        }

        if ("ADMIN".equalsIgnoreCase(request.getRole())
                && target.getRole() != Role.ADMIN) {
            throw new app.domain.exception.BusinessException(
                    "No tiene permisos para promover empleado a administrador.");
        }

        Employee employee = builder.buildForUpdate(
                request.getDocument(),
                request.getFullName(),
                request.getPhone(),
                request.getPassword(),
                request.getRole());
        Employee updated = employeeUseCase.update(target.getIdEmployee(), employee);
        return ResponseEntity.ok(responseMapper.toResponse(updated));
    }

    /**
     * Elimina un empleado por su UUID (solo ADMIN).
     * Bloquea eliminación de otros administradores y auto-eliminación.
     */
    @DeleteMapping("/deleteEmployee/{uuid}")
    @PreAuthorize("hasRole('ADMIN')")

    public ResponseEntity<Void> delete(@PathVariable String uuid) throws Exception {
        Employee target = employeeUseCase.findByUuid(uuid);
        Employee currentUser = securityHelper.getCurrentUser();

        if (target.getIdEmployee().equals(currentUser.getIdEmployee())) {
            throw new app.domain.exception.BusinessException(
                    "No puede eliminarse a sí mismo.");
        }

        if (target.getRole() == Role.ADMIN) {
            logger.warn("Admin intentó eliminar a otro admin.");
            throw new app.domain.exception.BusinessException(
                    "No tiene permisos para eliminar a otro administrador.");
        }

        employeeUseCase.deleteById(target.getIdEmployee());
        return ResponseEntity.noContent().build();
    }
}
