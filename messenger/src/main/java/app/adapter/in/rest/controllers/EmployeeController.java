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
import app.application.exceptions.BusinessException;
import app.application.exceptions.InputsException;
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

    private static final Logger logger = LoggerFactory.getLogger(EmployeeController.class);

    @Autowired
    private EmployeeUseCase employeeUseCase;
    @Autowired
    private EmployeeBuilder builder;
    @Autowired
    private EmployeeResponseMapper responseMapper;

    /**
     * Crea un nuevo empleado en el sistema.
     *
     * Utiliza el Builder para validar y construir el objeto Empleado antes de
     * persistirlo.
     *
     * @param request Datos del empleado a crear.
     * @return ResponseEntity con mensaje de éxito.
     * @throws InputsException   Si los datos de entrada son inválidos.
     * @throws BusinessException Si hay un error de lógica de negocio.
     */
    @PostMapping("/createEmployee")
    @PreAuthorize("hasRole('ADMIN')")
    @AuditableAction(action = "CREATE_EMPLOYEE", description = "Crear nuevo empleado")
    public ResponseEntity<String> create(@Valid @RequestBody EmployeeRequest request) throws Exception {
        logger.info("Creando empleado: {}", request.getUserName());
        Employee employee = builder.build(request.getDocument(),
                request.getFullName(),
                request.getPhone(),
                request.getUserName(),
                request.getPassword(),
                request.getRole());
        employeeUseCase.create(employee);
        logger.info("Empleado creado exitosamente: {} (doc: {})", request.getUserName(), request.getDocument());
        return ResponseEntity.status(HttpStatus.CREATED).body("Empleado creado exitosamente");
    }

    /**
     * Obtiene todos los empleados registrados.
     *
     * @return Lista de empleados mapeados al formato de respuesta.
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
     * Busca un empleado por su ID.
     *
     * @param id ID del empleado.
     * @return Datos del empleado encontrado.
     * @throws app.application.exceptions.ResourceNotFoundException Si el empleado
     *                                                              no existe.
     */
    @GetMapping("/findEmployee/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<EmployeeResponse> findById(@PathVariable Long id) {
        Employee employee = employeeUseCase.findById(id);
        if (employee == null) {
            throw new app.application.exceptions.ResourceNotFoundException("Empleado con ID " + id + " no encontrado");
        }
        return ResponseEntity.ok(responseMapper.toResponse(employee));
    }

    /**
     * Actualiza los datos de un empleado existente.
     *
     * @param id      ID del empleado a actualizar.
     * @param request Nuevos datos del empleado.
     * @return ResponseEntity con mensaje de éxito.
     * @throws InputsException   Si los datos de entrada son inválidos.
     * @throws BusinessException Si hay un error de lógica de negocio.
     */
    @PutMapping("/updateEmployee/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @AuditableAction(action = "UPDATE_EMPLOYEE", description = "Actualizar empleado existente")
    public ResponseEntity<String> update(@PathVariable Long id, @Valid @RequestBody EmployeeRequest request)
            throws Exception {
        logger.info("Actualizando empleado ID: {}", id);
        Employee employee = builder.build(
                request.getDocument(),
                request.getFullName(),
                request.getPhone(),
                request.getUserName(),
                request.getPassword(),
                request.getRole());
        employeeUseCase.update(id, employee);
        logger.info("Empleado actualizado: ID {} -> {}", id, request.getUserName());
        return ResponseEntity.ok("Empleado actualizado exitosamente");
    }

    /**
     * Elimina un empleado por su ID.
     *
     * @param id ID del empleado a eliminar.
     * @return ResponseEntity con mensaje de éxito.
     */
    @DeleteMapping("/deleteEmployee/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @AuditableAction(action = "DELETE_EMPLOYEE", description = "Eliminar empleado")
    public ResponseEntity<String> delete(@PathVariable Long id) throws Exception {
        logger.warn("Eliminando empleado ID: {}", id);
        employeeUseCase.deleteById(id);
        logger.info("Empleado eliminado: ID {}", id);
        return ResponseEntity.ok("Empleado eliminado exitosamente");
    }
}
