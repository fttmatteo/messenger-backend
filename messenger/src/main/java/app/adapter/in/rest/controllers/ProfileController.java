package app.adapter.in.rest.controllers;

import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import app.adapter.in.rest.mapper.EmployeeResponseMapper;
import app.adapter.in.rest.request.ProfileRequest;
import app.adapter.in.rest.response.EmployeeResponse;
import app.application.usecase.EmployeeUseCase;
import app.domain.model.Employee;
import app.infrastructure.helper.SecurityHelper;

/**
 * Controlador para la gestión del perfil del usuario autenticado.
 * Proporciona endpoints para ver y editar la propia información.
 */
@RestController
@RequestMapping("/profile")
public class ProfileController {

    @Autowired
    private EmployeeUseCase employeeUseCase;
    @Autowired
    private SecurityHelper securityHelper;
    @Autowired
    private EmployeeResponseMapper responseMapper;

    /**
     * Obtiene la información del perfil del usuario actualmente autenticado.
     */
    @GetMapping("/me")
    public ResponseEntity<EmployeeResponse> getMyProfile() {
        Employee currentUser = securityHelper.getCurrentUser();
        return ResponseEntity.ok(responseMapper.toResponse(currentUser));
    }

    /**
     * Actualiza la información del perfil del usuario actualmente autenticado.
     */
    @PutMapping("/me")
    public ResponseEntity<EmployeeResponse> updateMyProfile(@Valid @RequestBody ProfileRequest request) throws Exception {
        Employee currentUser = securityHelper.getCurrentUser();
        
        Employee incomingData = new Employee();
        incomingData.setFullName(request.getFullName());
        incomingData.setPhone(request.getPhone());
        incomingData.setPassword(request.getPassword());

        Employee updated = employeeUseCase.updateProfile(currentUser.getIdEmployee(), incomingData);
        return ResponseEntity.ok(responseMapper.toResponse(updated));
    }
}
