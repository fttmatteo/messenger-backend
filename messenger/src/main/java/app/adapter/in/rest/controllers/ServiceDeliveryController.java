package app.adapter.in.rest.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import app.adapter.in.builder.ServiceDeliveryBuilder;
import app.adapter.in.rest.mapper.ServiceDeliveryResponseMapper;
import app.adapter.in.rest.request.ServiceDeliveryCreateRequest;
import app.adapter.in.rest.request.ServiceDeliveryUpdateStatusRequest;
import app.adapter.in.rest.response.DailyStatsResponse;
import app.adapter.in.rest.response.ServiceDeliveryResponse;
import app.domain.exception.InputsException;
import app.domain.exception.ResourceNotFoundException;
import app.domain.exception.UnauthorizedException;
import app.application.usecase.ServiceDeliveryUseCase;
import app.domain.model.DailyStatistics;
import app.domain.model.Employee;
import app.domain.model.ServiceDelivery;
import app.domain.model.enums.Role;
import app.infrastructure.helper.FileHelper;
import app.infrastructure.helper.SecurityHelper;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Controlador REST para gestión de servicios de entrega.
 * 
 * Reglas de negocio:
 * - Mensajero: solo puede usar PENDING, DELIVERED, RETURNED
 * - Admin: solo puede usar CANCELED, RESOLVED y reasignar mensajero
 * - PENDING bloquea al mensajero hasta que admin use CANCELED/RESOLVED
 * - DELIVERED/RESOLVED: 72h para editar, después bloqueado
 * - Eliminación va a papelera (60 días para borrado permanente)
 */
@RestController
@RequestMapping("/services")
@PreAuthorize("isAuthenticated()")
public class ServiceDeliveryController {

    private static final Logger logger = LoggerFactory.getLogger(ServiceDeliveryController.class);

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

    /**
     * Crea un nuevo servicio de entrega.
     * Soporta creación manual o mediante OCR de imagen.
     */
    @PostMapping("/createService")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ServiceDeliveryResponse> createService(
            @RequestParam("image") MultipartFile image,
            @RequestParam("dealershipId") String dealershipId,
            @RequestParam(value = "messengerId", required = false) String messengerId,
            @RequestParam(value = "manualPlateNumber", required = false) String manualPlateNumber,
            @RequestParam(value = "latitude", required = false) Double latitude,
            @RequestParam(value = "longitude", required = false) Double longitude) throws Exception {

        logger.info("Solicitud creación servicio. DealershipId: {}", dealershipId);

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
        request.setManualPlateNumber(manualPlateNumber);
        request.setLatitude(latitude);
        request.setLongitude(longitude);

        ServiceDeliveryBuilder.ServiceDeliveryCreateData data = builder.buildCreateData(request);

        return fileHelper.withTempFile(image, imageFile -> {
            ServiceDelivery created;
            if (manualPlateNumber != null && !manualPlateNumber.isEmpty()) {
                created = serviceDeliveryUseCase.createServiceWithManualPlate(
                        imageFile,
                        manualPlateNumber,
                        data.getDealershipId(),
                        data.getMessengerId(),
                        data.getLatitude(),
                        data.getLongitude());
            } else {
                created = serviceDeliveryUseCase.createServiceFromImage(
                        imageFile,
                        data.getDealershipId(),
                        data.getMessengerId(),
                        data.getLatitude(),
                        data.getLongitude());
            }
            return ResponseEntity.status(HttpStatus.CREATED).body(responseMapper.toResponse(created));
        });
    }

    /**
     * Actualiza el estado de un servicio.
     * Permite adjuntar evidencia (firma, fotos) si es necesario.
     */
    @PutMapping("/updateService/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ServiceDeliveryResponse> updateStatus(
            @PathVariable Long id,
            @RequestParam("status") String status,
            @RequestParam(value = "observation", required = false) String observation,
            @RequestParam(value = "signature", required = false) MultipartFile signature,
            @RequestParam(value = "photos", required = false) List<MultipartFile> photos,
            @RequestParam(value = "latitude", required = false) Double latitude,
            @RequestParam(value = "longitude", required = false) Double longitude) throws Exception {

        logger.info("Solicitud actualización servicio ID: {} a status: {}", id, status);

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
                signatureFile = fileHelper.convertToFile(signature);
                tempFiles.add(signatureFile);
            }

            List<File> photoFiles = fileHelper.convertToFiles(photos);
            tempFiles.addAll(photoFiles);

            ServiceDelivery updated = serviceDeliveryUseCase.updateStatusWithFiles(id, data.getStatus(),
                    data.getObservation(),
                    signatureFile, photoFiles, data.getUserId(), data.getLatitude(), data.getLongitude());

            return ResponseEntity.ok(responseMapper.toResponse(updated));
        } finally {
            fileHelper.cleanupTempFiles(tempFiles);
        }
    }

    /**
     * Reasigna un servicio a otro mensajero.
     * Solo disponible para ADMIN y solo cuando el servicio está en CANCELED.
     */
    /**
     * Reasigna un servicio a otro mensajero.
     * Solo disponible para ADMIN y solo cuando el servicio está en CANCELED.
     */
    @PutMapping("/reassign/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ServiceDeliveryResponse> reassignMessenger(
            @PathVariable Long id,
            @RequestBody Map<String, Long> request) throws Exception {

        logger.info("Solicitud de reasignación de servicio ID: {}", id);

        Long newMessengerId = request.get("messengerId");
        if (newMessengerId == null) {
            throw new InputsException("El ID del nuevo mensajero es requerido.");
        }

        Employee currentUser = securityHelper.getCurrentUser();
        ServiceDelivery reassigned = serviceDeliveryUseCase.reassignMessenger(
                id, newMessengerId, currentUser.getIdEmployee());

        return ResponseEntity.ok(responseMapper.toResponse(reassigned));
    }

    /**
     * Busca un servicio por su ID.
     */
    @GetMapping("/findByServiceId/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ServiceDeliveryResponse> findById(@PathVariable Long id) throws Exception {
        ServiceDelivery service = serviceDeliveryUseCase.findById(id);
        if (service == null) {
            throw new ResourceNotFoundException("Servicio con ID " + id + " no encontrado");
        }

        Employee currentUser = securityHelper.getCurrentUser();

        if (currentUser.getRole() == Role.MESSENGER) {
            if (service.getMessenger() == null ||
                    !service.getMessenger().getIdEmployee().equals(currentUser.getIdEmployee())) {
                throw new UnauthorizedException("No tienes permiso para ver este servicio");
            }
        }

        return ResponseEntity.ok(responseMapper.toResponse(service));
    }

    /**
     * Obtiene todos los servicios.
     * Los mensajeros solo ven sus propios servicios asignados.
     */
    @GetMapping("/allServices")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<ServiceDeliveryResponse>> findAll() {
        Employee currentUser = securityHelper.getCurrentUser();

        List<ServiceDelivery> services = serviceDeliveryUseCase.findAll();

        if (currentUser.getRole() == Role.MESSENGER) {
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

    /**
     * Mueve un servicio a la papelera (Soft Delete).
     */
    @DeleteMapping("/deleteService/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, String>> delete(@PathVariable Long id) throws Exception {
        logger.info("Solicitud eliminación servicio ID: {}", id);

        Employee currentUser = securityHelper.getCurrentUser();
        serviceDeliveryUseCase.deleteById(id, currentUser.getIdEmployee());

        return ResponseEntity.ok(Map.of(
                "message",
                "El servicio ha sido movido a la papelera. Será eliminado permanentemente después de 60 días."));
    }

    /**
     * Lista los servicios que están en la papelera.
     */
    @GetMapping("/trash")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<ServiceDeliveryResponse>> findDeleted() {
        logger.info("Consultando servicios en papelera");

        List<ServiceDelivery> services = serviceDeliveryUseCase.findDeleted();
        List<ServiceDeliveryResponse> responses = services.stream()
                .map(responseMapper::toResponse)
                .collect(Collectors.toList());

        return ResponseEntity.ok(responses);
    }

    /**
     * Restaura un servicio desde la papelera (solo ADMIN).
     */
    /**
     * Restaura un servicio desde la papelera (solo ADMIN).
     */
    @PostMapping("/trash/restore/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ServiceDeliveryResponse> restore(@PathVariable Long id) throws Exception {
        logger.info("Solicitud restaurar servicio ID: {} desde papelera", id);

        Employee currentUser = securityHelper.getCurrentUser();
        ServiceDelivery restored = serviceDeliveryUseCase.restore(id, currentUser.getIdEmployee());

        return ResponseEntity.ok(responseMapper.toResponse(restored));
    }

    /**
     * Vacía la papelera eliminando permanentemente todos los servicios (solo
     * ADMIN).
     */
    /**
     * Vacía la papelera eliminando permanentemente todos los servicios (solo
     * ADMIN).
     */
    @DeleteMapping("/trash/empty")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> emptyTrash() {
        logger.info("Solicitud vaciar papelera");

        Employee currentUser = securityHelper.getCurrentUser();
        int deletedCount = serviceDeliveryUseCase.emptyTrash(currentUser.getIdEmployee());

        return ResponseEntity.ok(Map.of(
                "message", "Papelera vaciada correctamente",
                "deletedCount", deletedCount));
    }

    /**
     * Elimina permanentemente un servicio individual de la papelera (solo ADMIN).
     */
    @DeleteMapping("/trash/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, String>> permanentDeleteFromTrash(@PathVariable Long id) throws Exception {
        logger.info("Solicitud eliminar permanentemente servicio ID: {} de papelera", id);

        Employee currentUser = securityHelper.getCurrentUser();
        serviceDeliveryUseCase.permanentDeleteById(id, currentUser.getIdEmployee());

        return ResponseEntity.ok(Map.of("message", "Servicio eliminado permanentemente"));
    }

    /**
     * Obtiene estadísticas diarias de servicios para un mensajero.
     */
    @GetMapping("/stats/daily")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<DailyStatsResponse>> getDailyStats(
            @RequestParam Long messengerId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) java.time.LocalDate from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) java.time.LocalDate to) {

        List<DailyStatistics> stats = serviceDeliveryUseCase.getDailyStats(messengerId, from, to);

        List<DailyStatsResponse> response = stats.stream()
                .map(s -> new DailyStatsResponse(
                        s.date(),
                        s.assigned(),
                        s.delivered(),
                        s.returned(),
                        s.canceled(),
                        s.total()))
                .collect(Collectors.toList());

        return ResponseEntity.ok(response);
    }
}
