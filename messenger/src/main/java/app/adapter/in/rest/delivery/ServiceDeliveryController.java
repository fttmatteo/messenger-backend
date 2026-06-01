package app.adapter.in.rest.delivery;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import app.adapter.in.builder.ServiceDeliveryBuilder;
import app.adapter.in.rest.common.PageResponse;
import app.domain.exception.InputsException;
import app.domain.exception.ResourceNotFoundException;
import app.domain.exception.UnauthorizedException;
import app.application.usecase.ServiceDeliveryUseCase;
import app.domain.model.Employee;
import app.domain.model.ServiceDelivery;
import app.domain.model.enums.Role;
import app.infrastructure.helper.FileHelper;
import app.infrastructure.helper.SecurityHelper;
import app.domain.services.FileValidationService;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;

/**
 * Controlador REST para gestión de servicios de entrega.
 * 
 * Reglas de negocio:
 * - Mensajero: solo puede usar PENDING, DELIVERED, RETURNED
 * - Admin: solo puede usar CANCELED, RESOLVED y reasignar mensajero (cuando
 * está CANCELED)
 * - Eliminación va a papelera (60 días para borrado permanente)
 */
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
    private SecurityHelper securityHelper;
    @Autowired
    private FileHelper fileHelper;
    @Autowired
    private FileValidationService fileValidationService;



    /**
     * Crea un nuevo servicio de entrega.
     * Soporta creación manual de imagen.
     */
    @PostMapping("/createService")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ServiceDeliveryResponse> createService(
            @RequestParam("dealershipId") String dealershipId,
            @RequestParam("originDealershipId") String originDealershipId,
            @RequestParam(value = "messengerId", required = false) String messengerId,
            @RequestParam(value = "manualPlateNumber", required = false) String manualPlateNumber,
            @RequestParam(value = "latitude", required = false) Double latitude,
            @RequestParam(value = "longitude", required = false) Double longitude,
            @RequestParam(value = "scheduledAt", required = false) String scheduledAtStr) throws Exception {

        Employee currentUser = securityHelper.getCurrentUser();

        String finalMessengerId = messengerId;
        if (currentUser.getRole() != Role.ADMIN) {
            finalMessengerId = String.valueOf(currentUser.getIdEmployee());
        } else {
            if (messengerId == null || messengerId.trim().isEmpty()) {
                throw new InputsException("El ID del mensajero es requerido para usuarios Admin.");
            }
        }

        ServiceDeliveryCreateRequest request = new ServiceDeliveryCreateRequest(dealershipId, finalMessengerId);
        request.setOriginDealershipId(originDealershipId);
        request.setManualPlateNumber(manualPlateNumber);
        request.setLatitude(latitude);
        request.setLongitude(longitude);

        ServiceDeliveryBuilder.ServiceDeliveryCreateData data = builder.buildCreateData(request);

        if (manualPlateNumber == null || manualPlateNumber.trim().isEmpty()) {
            throw new InputsException("El chasis es requerido para crear el servicio.");
        }
        if (manualPlateNumber.length() < 10 || manualPlateNumber.length() > 20) {
            throw new InputsException("El chasis debe tener entre 10 y 20 caracteres.");
        }

        java.time.LocalDateTime scheduledAt = null;
        if (scheduledAtStr != null && !scheduledAtStr.trim().isEmpty()) {
            try {
                scheduledAt = java.time.OffsetDateTime.parse(scheduledAtStr, java.time.format.DateTimeFormatter.ISO_DATE_TIME)
                        .atZoneSameInstant(java.time.ZoneId.systemDefault())
                        .toLocalDateTime();
            } catch (Exception e) {
                scheduledAt = java.time.LocalDateTime.parse(scheduledAtStr, java.time.format.DateTimeFormatter.ISO_DATE_TIME);
            }
        }
        
        ServiceDelivery created = serviceDeliveryUseCase.createServiceWithManualPlate(
                manualPlateNumber,
                data.getDealershipId(),
                data.getOriginDealershipId(),
                data.getMessengerId(),
                data.getLatitude(),
                data.getLongitude(),
                scheduledAt);
        
        return ResponseEntity.status(HttpStatus.CREATED).body(responseMapper.toResponse(created));
    }

    /**
     * Actualiza el estado de un servicio.
     * Permite adjuntar evidencia (firma, fotos) si es necesario.
     */
    @PutMapping("/updateService/{uuid}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ServiceDeliveryResponse> updateStatus(
            @PathVariable String uuid,
            @RequestParam("status") String status,
            @RequestParam(value = "observation", required = false) String observation,
            @RequestParam(value = "signature", required = false) MultipartFile signature,
            @RequestParam(value = "photos", required = false) List<MultipartFile> photos,
            @RequestParam(value = "latitude", required = false) Double latitude,
            @RequestParam(value = "longitude", required = false) Double longitude) throws Exception {

        List<File> tempFiles = new ArrayList<>();
        try {
            Employee currentUser = securityHelper.getCurrentUser();
            String userId = String.valueOf(currentUser.getIdEmployee());

            ServiceDeliveryUpdateStatusRequest request = new ServiceDeliveryUpdateStatusRequest(status, observation,
                    userId);
            request.setLatitude(latitude);
            request.setLongitude(longitude);

            ServiceDeliveryBuilder.ServiceDeliveryUpdateData data = builder.buildUpdateStatusData(request);

            File signatureFile = null;
            if (signature != null && !signature.isEmpty()) {
                try {
                    fileValidationService.validateSignatureFile(signature);
                } catch (SecurityException e) {
                    throw new InputsException("Error al procesar la firma adjunta.");
                }
                signatureFile = fileHelper.convertToFile(signature);
                tempFiles.add(signatureFile);
            }


            List<File> photoFiles = new ArrayList<>();
            if (photos != null && !photos.isEmpty()) {
                for (MultipartFile photo : photos) {
                    if (!photo.isEmpty()) {
                        try {
                            fileValidationService.validatePhotoFile(photo);
                        } catch (SecurityException e) {
                            throw new InputsException("Error al procesar la foto adjunta.");
                        }
                        photoFiles.add(fileHelper.convertToFile(photo));
                    }
                }
            }
            tempFiles.addAll(photoFiles);

            ServiceDelivery serviceForId = serviceDeliveryUseCase.findByUuid(uuid);
            ServiceDelivery updated = serviceDeliveryUseCase.updateStatusWithFiles(serviceForId.getIdServiceDelivery(), data.getStatus(),
                    data.getObservation(),
                    signatureFile, photoFiles, data.getUserId(), data.getLatitude(),
                    data.getLongitude());

            return ResponseEntity.ok(responseMapper.toResponse(updated));
        } finally {
            fileHelper.cleanupTempFiles(tempFiles);
        }
    }

    /**
     * Reasigna un servicio a otro mensajero.
     * Solo disponible para ADMIN y solo cuando el servicio está en CANCELED.
     */
    @PutMapping("/reassign/{uuid}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ServiceDeliveryResponse> reassignMessenger(
            @PathVariable String uuid,
            @RequestBody Map<String, Long> request) throws Exception {

        Long newMessengerId = request.get("messengerId");
        if (newMessengerId == null) {
            throw new InputsException("El ID del nuevo mensajero es requerido.");
        }

        Employee currentUser = securityHelper.getCurrentUser();
        ServiceDelivery serviceForId = serviceDeliveryUseCase.findByUuid(uuid);
        ServiceDelivery reassigned = serviceDeliveryUseCase.reassignMessenger(
                serviceForId.getIdServiceDelivery(), newMessengerId, currentUser.getIdEmployee());

        return ResponseEntity.ok(responseMapper.toResponse(reassigned));
    }

    /**
     * Edita el origen y/o destino de un servicio en estado CANCELED (solo ADMIN).
     * Genera un registro en el historial de estados con snapshot del cambio.
     * Funciona igual que reasignar: solo permitido cuando el servicio está cancelado.
     */
    @PutMapping("/editRoute/{uuid}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ServiceDeliveryResponse> editRoute(
            @PathVariable String uuid,
            @RequestBody Map<String, Long> request) throws Exception {

        Long newDealershipId = request.get("dealershipId");
        Long newOriginDealershipId = request.get("originDealershipId");

        if (newDealershipId == null && newOriginDealershipId == null) {
            throw new InputsException("Debe indicar al menos un nuevo destino u origen.");
        }

        Employee currentUser = securityHelper.getCurrentUser();
        ServiceDelivery serviceForId = serviceDeliveryUseCase.findByUuid(uuid);

        ServiceDelivery updated = serviceDeliveryUseCase.editRoute(
                serviceForId.getIdServiceDelivery(),
                newDealershipId,
                newOriginDealershipId,
                currentUser.getIdEmployee());

        return ResponseEntity.ok(responseMapper.toResponse(updated));
    }

    /**
     * Busca un servicio por su ID.
     */
    @GetMapping("/findByServiceId/{uuid}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ServiceDeliveryResponse> findByUuid(@PathVariable String uuid) throws Exception {
        ServiceDelivery service = serviceDeliveryUseCase.findByUuid(uuid);
        if (service == null) {
            throw new ResourceNotFoundException("Servicio con UUID " + uuid + " no encontrado");
        }

        Employee currentUser = securityHelper.getCurrentUser();

        if (currentUser.getRole() == Role.MESSENGER) {
            if (service.getCurrentStatus() == app.domain.model.enums.Status.SCHEDULED) {
                throw new UnauthorizedException("No tienes permiso para ver este servicio");
            }
            if (service.getMessenger() == null ||
                    !service.getMessenger().getIdEmployee().equals(currentUser.getIdEmployee())) {
                throw new UnauthorizedException("No tienes permiso para ver este servicio");
            }
        }

        return ResponseEntity.ok(responseMapper.toResponse(service));
    }

    /**
     * Obtiene todos los servicios con paginación.
     * Los mensajeros solo ven sus propios servicios asignados.
     */
    @GetMapping("/allServicesPageable")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<PageResponse<ServiceDeliveryResponse>> findAllPaginated(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDirection,
            @RequestParam(required = false) List<String> status,
            @RequestParam(required = false) String search) {

        Employee currentUser = securityHelper.getCurrentUser();

        Page<ServiceDelivery> servicePage;

        List<app.domain.model.enums.Status> statusEnums = null;
        if (status != null && !status.isEmpty()) {
            statusEnums = status.stream()
                    .map(s -> app.domain.model.enums.Status.valueOf(s.toUpperCase()))
                    .collect(Collectors.toList());
        }

        if (currentUser.getRole() == Role.MESSENGER) {
            Long messengerId = currentUser.getIdEmployee();
            List<app.domain.model.enums.Status> finalStatuses;
            if (statusEnums == null || statusEnums.isEmpty()) {
                finalStatuses = List.of(
                    app.domain.model.enums.Status.ASSIGNED,
                    app.domain.model.enums.Status.PENDING,
                    app.domain.model.enums.Status.DELIVERED,
                    app.domain.model.enums.Status.RETURNED,
                    app.domain.model.enums.Status.CANCELED,
                    app.domain.model.enums.Status.RESOLVED,
                    app.domain.model.enums.Status.FAILED
                );
            } else {
                finalStatuses = statusEnums.stream()
                    .filter(s -> s != app.domain.model.enums.Status.SCHEDULED)
                    .collect(Collectors.toList());
            }
            servicePage = serviceDeliveryUseCase.findByMessengerPaginated(
                    messengerId, page, size, sortBy, sortDirection, search, finalStatuses);
        } else {
            servicePage = serviceDeliveryUseCase.findAllPaginated(
                    page, size, sortBy, sortDirection, search, statusEnums);
        }

        List<ServiceDeliveryResponse> mappedContent = servicePage.getContent()
                .stream()
                .map(responseMapper::toSummaryResponse)
                .collect(Collectors.toList());

        Page<ServiceDeliveryResponse> responsePage = new org.springframework.data.domain.PageImpl<>(
                mappedContent,
                servicePage.getPageable(),
                servicePage.getTotalElements());
        PageResponse<ServiceDeliveryResponse> pageResponse = PageResponse.from(responsePage);

        return ResponseEntity.ok(pageResponse);
    }

    /**
     * Mueve un servicio a la papelera (Soft Delete).
     */
    @DeleteMapping("/deleteService/{uuid}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, String>> delete(@PathVariable String uuid) throws Exception {

        Employee currentUser = securityHelper.getCurrentUser();
        ServiceDelivery serviceForId = serviceDeliveryUseCase.findByUuid(uuid);
        serviceDeliveryUseCase.deleteById(serviceForId.getIdServiceDelivery(), currentUser.getIdEmployee());

        return ResponseEntity.ok(Map.of(
                "message",
                "El servicio ha sido movido a la papelera. Será eliminado permanentemente después de 60 días."));
    }

    /**
     * Lista los servicios que están en la papelera con paginación.
     */
    @GetMapping("/trash")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<PageResponse<ServiceDeliveryResponse>> findDeleted(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        org.springframework.data.domain.Pageable pageable = org.springframework.data.domain.PageRequest.of(page, size);
        Page<ServiceDelivery> servicePage = serviceDeliveryUseCase.findDeleted(pageable);

        List<ServiceDeliveryResponse> mappedContent = servicePage.getContent()
                .stream()
                .map(responseMapper::toSummaryResponse)
                .collect(Collectors.toList());

        Page<ServiceDeliveryResponse> responsePage = new org.springframework.data.domain.PageImpl<>(
                mappedContent,
                servicePage.getPageable(),
                servicePage.getTotalElements());

        return ResponseEntity.ok(PageResponse.from(responsePage));
    }

    /**
     * Restaura un servicio desde la papelera (solo ADMIN).
     */
    @PostMapping("/trash/restore/{uuid}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ServiceDeliveryResponse> restore(@PathVariable String uuid) throws Exception {

        Employee currentUser = securityHelper.getCurrentUser();
        ServiceDelivery serviceForId = serviceDeliveryUseCase.findByUuidIncludingDeleted(uuid);
        ServiceDelivery restored = serviceDeliveryUseCase.restore(serviceForId.getIdServiceDelivery(), currentUser.getIdEmployee());

        return ResponseEntity.ok(responseMapper.toResponse(restored));
    }

    /**
     * Vacía la papelera eliminando permanentemente todos los servicios (solo
     * ADMIN).
     */
    @DeleteMapping("/trash/empty")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> emptyTrash() {

        Employee currentUser = securityHelper.getCurrentUser();
        int deletedCount = serviceDeliveryUseCase.emptyTrash(currentUser.getIdEmployee());

        return ResponseEntity.ok(Map.of(
                "message", "Papelera vaciada correctamente",
                "deletedCount", deletedCount));
    }

    /**
     * Elimina permanentemente un servicio individual de la papelera (solo ADMIN).
     */
    @DeleteMapping("/trash/{uuid}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, String>> permanentDeleteFromTrash(@PathVariable String uuid) throws Exception {

        Employee currentUser = securityHelper.getCurrentUser();
        ServiceDelivery serviceForId = serviceDeliveryUseCase.findByUuidIncludingDeleted(uuid);
        serviceDeliveryUseCase.permanentDeleteById(serviceForId.getIdServiceDelivery(), currentUser.getIdEmployee());

        return ResponseEntity.ok(Map.of("message", "Servicio eliminado permanentemente"));
    }
}
