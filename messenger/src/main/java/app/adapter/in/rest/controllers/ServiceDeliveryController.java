package app.adapter.in.rest.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import app.adapter.in.builder.ServiceDeliveryBuilder;
import app.adapter.in.rest.mapper.ServiceDeliveryResponseMapper;
import app.adapter.in.rest.request.ServiceDeliveryCreateRequest;
import app.adapter.in.rest.request.ServiceDeliveryUpdateStatusRequest;
import app.adapter.in.rest.response.ServiceDeliveryResponse;
import app.application.exceptions.InputsException;
import app.application.exceptions.ResourceNotFoundException;
import app.application.exceptions.UnauthorizedException;
import app.application.usecase.ServiceDeliveryUseCase;
import app.domain.model.Employee;
import app.domain.model.ServiceDelivery;
import app.domain.model.enums.Role;
import app.domain.ports.EmployeePort;
import app.infrastructure.helper.FileHelper;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/services")
@PreAuthorize("isAuthenticated()")
public class ServiceDeliveryController {

    @Autowired
    private ServiceDeliveryUseCase serviceDeliveryUseCase;
    @Autowired
    private ServiceDeliveryBuilder builder;
    @Autowired
    private ServiceDeliveryResponseMapper responseMapper;
    @Autowired
    private EmployeePort employeePort;
    @Autowired
    private FileHelper fileHelper;

    @PostMapping("/createService")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ServiceDeliveryResponse> createService(
            @RequestParam("image") MultipartFile image,
            @RequestParam("dealershipId") String dealershipId,
            @RequestParam(value = "messengerId", required = false) String messengerId,
            @RequestParam(value = "manualPlateNumber", required = false) String manualPlateNumber) throws Exception {

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String currentUserName = auth.getName();
        Employee currentUser = employeePort.findByUserName(currentUserName);

        if (currentUser == null) {
            throw new UnauthorizedException("Autenticación de usuario no encontrada o inválida.");
        }

        String finalMessengerId = messengerId;
        if (currentUser.getRole() != Role.ADMIN) {
            finalMessengerId = String.valueOf(currentUser.getIdEmployee());
        } else {
            if (messengerId == null || messengerId.trim().isEmpty()) {
                throw new InputsException("El ID del mensajero es requerido para usuarios Admin.");
            }
        }

        ServiceDeliveryCreateRequest request = new ServiceDeliveryCreateRequest(dealershipId, finalMessengerId);
        request.setManualPlateNumber(manualPlateNumber);

        ServiceDeliveryBuilder.ServiceDeliveryCreateData data = builder.buildCreateData(request);

        return fileHelper.withTempFile(image, imageFile -> {
            ServiceDelivery created;
            if (manualPlateNumber != null && !manualPlateNumber.isEmpty()) {
                created = serviceDeliveryUseCase.createServiceWithManualPlate(
                        imageFile,
                        manualPlateNumber,
                        data.getDealershipId(),
                        data.getMessengerId());
            } else {
                created = serviceDeliveryUseCase.createServiceFromImage(
                        imageFile,
                        data.getDealershipId(),
                        data.getMessengerId());
            }
            return ResponseEntity.status(HttpStatus.CREATED).body(responseMapper.toResponse(created));
        });
    }

    @PutMapping("/updateServiceStatus/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ServiceDeliveryResponse> updateStatus(
            @PathVariable Long id,
            @RequestParam("status") String status,
            @RequestParam(value = "observation", required = false) String observation,
            @RequestParam(value = "signature", required = false) MultipartFile signature,
            @RequestParam(value = "photos", required = false) List<MultipartFile> photos) throws Exception {

        List<File> tempFiles = new ArrayList<>();
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            String currentUserName = auth.getName();
            Employee currentUser = employeePort.findByUserName(currentUserName);

            if (currentUser == null) {
                throw new UnauthorizedException("Autenticación de usuario no encontrada o inválida.");
            }

            String userId = String.valueOf(currentUser.getIdEmployee());

            ServiceDeliveryUpdateStatusRequest request = new ServiceDeliveryUpdateStatusRequest(status, observation,
                    userId);
            ServiceDeliveryBuilder.ServiceDeliveryUpdateData data = builder.buildUpdateStatusData(request);

            File signatureFile = null;
            if (signature != null && !signature.isEmpty()) {
                signatureFile = fileHelper.convertToFile(signature);
                tempFiles.add(signatureFile);
            }

            List<File> photoFiles = fileHelper.convertToFiles(photos);
            tempFiles.addAll(photoFiles);

            ServiceDelivery updated = serviceDeliveryUseCase.updateStatusWithFiles(id, data.getStatus(),
                    data.getObservation(),
                    signatureFile, photoFiles, data.getUserId());

            return ResponseEntity.ok(responseMapper.toResponse(updated));
        } finally {
            fileHelper.cleanupTempFiles(tempFiles);
        }
    }

    @GetMapping("/findService/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ServiceDeliveryResponse> findById(@PathVariable Long id) throws Exception {
        ServiceDelivery service = serviceDeliveryUseCase.findById(id);
        if (service == null) {
            throw new ResourceNotFoundException("Servicio con ID " + id + " no encontrado");
        }

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String currentUserName = auth.getName();
        Employee currentUser = employeePort.findByUserName(currentUserName);

        if (currentUser != null && currentUser.getRole() == Role.MESSENGER) {
            if (service.getMessenger() == null ||
                    !service.getMessenger().getIdEmployee().equals(currentUser.getIdEmployee())) {
                throw new UnauthorizedException("No tienes permiso para ver este servicio");
            }
        }

        return ResponseEntity.ok(responseMapper.toResponse(service));
    }

    @GetMapping("/allServices")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<ServiceDeliveryResponse>> findAll() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String currentUserName = auth.getName();
        Employee currentUser = employeePort.findByUserName(currentUserName);

        List<ServiceDelivery> services;

        services = serviceDeliveryUseCase.findAll();

        if (currentUser != null && currentUser.getRole() == Role.MESSENGER) {
            Long messengerId = currentUser.getIdEmployee();
            services = services.stream()
                    .filter(s -> s.getMessenger() != null &&
                            s.getMessenger().getIdEmployee().equals(messengerId))
                    .collect(Collectors.toList());
        }

        List<ServiceDeliveryResponse> responses = services.stream()
                .map(responseMapper::toResponse)
                .collect(Collectors.toList());
        return ResponseEntity.ok(responses);
    }

    @DeleteMapping("/deleteService/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'MESSENGER')")
    public ResponseEntity<Void> delete(@PathVariable Long id) throws Exception {
        serviceDeliveryUseCase.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}
