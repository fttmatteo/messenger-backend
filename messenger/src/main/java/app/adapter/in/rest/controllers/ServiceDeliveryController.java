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
import app.domain.model.enums.Status;
import app.domain.ports.EmployeePort;
import app.infrastructure.helper.FileHelper;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Controlador REST para gestionar servicios de entrega.
 *
 * Proporciona endpoints para:
 * - Crear servicios con detección OCR automática o entrada manual de placas.
 * - Actualizar estados con evidencias (firmas y fotos).
 * - Consultar servicios por diversos criterios (ID, mensajero, concesionario,
 * estado).
 *
 * Implementa control de acceso basado en roles:
 * - ADMIN puede asignar servicios a cualquier mensajero.
 * - MESSENGER solo puede gestionar sus propios servicios asignados.
 */
@RestController
@RequestMapping("/services")
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
    public ResponseEntity<?> createService(
            @RequestParam("image") MultipartFile image,
            @RequestParam("dealershipId") String dealershipId,
            @RequestParam(value = "messengerDocument", required = false) String messengerDocument,
            @RequestParam(value = "manualPlateNumber", required = false) String manualPlateNumber) throws Exception {

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String currentUserName = auth.getName();
        Employee currentUser = employeePort.findByUserName(currentUserName);

        if (currentUser == null) {
            throw new UnauthorizedException("Autenticación de usuario no encontrada o inválida.");
        }

        String finalMessengerDocument = messengerDocument;
        if (currentUser.getRole() != Role.ADMIN) {
            finalMessengerDocument = String.valueOf(currentUser.getDocument());
        } else {
            if (messengerDocument == null || messengerDocument.trim().isEmpty()) {
                throw new InputsException("El documento del mensajero es requerido para usuarios Admin.");
            }
        }

        ServiceDeliveryCreateRequest request = new ServiceDeliveryCreateRequest(dealershipId,
                finalMessengerDocument);
        request.setManualPlateNumber(manualPlateNumber);

        ServiceDeliveryBuilder.ServiceDeliveryCreateData data = builder.buildCreateData(request);

        // Usa withTempFile para cleanup automático del archivo temporal
        return fileHelper.withTempFile(image, imageFile -> {
            if (manualPlateNumber != null && !manualPlateNumber.isEmpty()) {
                serviceDeliveryUseCase.createServiceWithManualPlate(
                        imageFile,
                        manualPlateNumber,
                        data.getDealershipId(),
                        data.getMessengerDocument());
            } else {
                serviceDeliveryUseCase.createServiceFromImage(
                        imageFile,
                        data.getDealershipId(),
                        data.getMessengerDocument());
            }
            return ResponseEntity.status(HttpStatus.CREATED).body("Servicio creado exitosamente.");
        });
    }

    @PutMapping("/updateServiceStatus/{id}")
    public ResponseEntity<?> updateStatus(
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

            String userDocument = String.valueOf(currentUser.getDocument());

            ServiceDeliveryUpdateStatusRequest request = new ServiceDeliveryUpdateStatusRequest(status, observation,
                    userDocument);
            ServiceDeliveryBuilder.ServiceDeliveryUpdateData data = builder.buildUpdateStatusData(request);

            File signatureFile = null;
            if (signature != null && !signature.isEmpty()) {
                signatureFile = fileHelper.convertToFile(signature);
                tempFiles.add(signatureFile);
            }

            List<File> photoFiles = fileHelper.convertToFiles(photos);
            tempFiles.addAll(photoFiles);

            serviceDeliveryUseCase.updateStatusWithFiles(id, data.getStatus(), data.getObservation(),
                    signatureFile, photoFiles, data.getUserDocument());

            return ResponseEntity.ok("Estado actualizado exitosamente.");
        } finally {
            fileHelper.cleanupTempFiles(tempFiles);
        }
    }

    @GetMapping("/findService/{id}")
    public ResponseEntity<ServiceDeliveryResponse> findById(@PathVariable Long id) throws Exception {
        ServiceDelivery service = serviceDeliveryUseCase.findById(id);
        if (service == null) {
            throw new ResourceNotFoundException("Servicio con ID " + id + " no encontrado");
        }

        // ========== VALIDACIÓN DE OWNERSHIP ==========
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String currentUserName = auth.getName();
        Employee currentUser = employeePort.findByUserName(currentUserName);

        if (currentUser != null && currentUser.getRole() == Role.MESSENGER) {
            if (service.getMessenger() == null ||
                    !service.getMessenger().getDocument().equals(currentUser.getDocument())) {
                throw new UnauthorizedException("No tienes permiso para ver este servicio");
            }
        }

        return ResponseEntity.ok(responseMapper.toResponse(service));
    }

    @GetMapping("/allServices")
    public ResponseEntity<List<ServiceDeliveryResponse>> findAll() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String currentUserName = auth.getName();
        Employee currentUser = employeePort.findByUserName(currentUserName);

        List<ServiceDelivery> services;

        if (currentUser != null && currentUser.getRole() == Role.MESSENGER) {
            services = serviceDeliveryUseCase.findByMessenger(currentUser.getDocument());
        } else {
            services = serviceDeliveryUseCase.findAll();
        }

        List<ServiceDeliveryResponse> responses = services.stream()
                .map(responseMapper::toResponse)
                .collect(Collectors.toList());
        return ResponseEntity.ok(responses);
    }

    @GetMapping("/findServiceByMessenger/{Id}")
    public ResponseEntity<List<ServiceDeliveryResponse>> findByMessenger(@PathVariable Long messengerId) {
        List<ServiceDeliveryResponse> responses = serviceDeliveryUseCase.findByMessenger(messengerId).stream()
                .map(responseMapper::toResponse)
                .collect(Collectors.toList());
        return ResponseEntity.ok(responses);
    }

    @GetMapping("/findServiceByDealership/{Id}")
    public ResponseEntity<List<ServiceDeliveryResponse>> findByDealership(@PathVariable Long dealershipId) {
        List<ServiceDeliveryResponse> responses = serviceDeliveryUseCase.findByDealership(dealershipId).stream()
                .map(responseMapper::toResponse)
                .collect(Collectors.toList());
        return ResponseEntity.ok(responses);
    }

    @GetMapping("/findServiceByStatus/{status}")
    public ResponseEntity<List<ServiceDeliveryResponse>> findByStatus(@PathVariable Status status) {
        List<ServiceDeliveryResponse> responses = serviceDeliveryUseCase.findByStatus(status).stream()
                .map(responseMapper::toResponse)
                .collect(Collectors.toList());
        return ResponseEntity.ok(responses);
    }

    @DeleteMapping("/deleteService/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<String> delete(@PathVariable Long id) throws Exception {
        serviceDeliveryUseCase.deleteById(id);
        return ResponseEntity.ok("Servicio eliminado exitosamente");
    }
}
