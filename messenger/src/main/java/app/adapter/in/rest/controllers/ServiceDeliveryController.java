package app.adapter.in.rest.controllers;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
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

    private static final Logger logger = LoggerFactory.getLogger(ServiceDeliveryController.class);

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

    /**
     * Crea un nuevo servicio de entrega.
     *
     * Permite la creación mediante detección automática de placa (OCR) desde una
     * imagen
     * o mediante ingreso manual. Asigna el servicio a un mensajero específico.
     *
     * @param image             Archivo de imagen para OCR (requerido).
     * @param dealershipId      ID del concesionario origen.
     * @param messengerDocument Documento del mensajero asignado (requerido para
     *                          ADMIN).
     * @param manualPlateNumber Placa ingresada manualmente (opcional, omite OCR).
     * @return ResponseEntity con mensaje de éxito o error.
     */
    @PostMapping("/create")
    public ResponseEntity<?> createService(
            @RequestParam("image") MultipartFile image,
            @RequestParam("dealershipId") String dealershipId,
            @RequestParam(value = "messengerDocument", required = false) String messengerDocument,
            @RequestParam(value = "manualPlateNumber", required = false) String manualPlateNumber) throws Exception {

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String currentUserName = auth.getName();
        Employee currentUser = employeePort.findByUserName(currentUserName);

        logger.info("Creando servicio - usuario: {}, concesionario: {}", currentUserName, dealershipId);

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

    /**
     * Actualiza el estado de un servicio de entrega existente.
     *
     * Permite adjuntar evidencias obligatorias según el nuevo estado (firmas,
     * fotos).
     *
     * @param id          ID del servicio a actualizar.
     * @param status      Nuevo estado del servicio.
     * @param observation Observaciones adicionales sobre el cambio de estado.
     * @param signature   Archivo de imagen con la firma de conformidad (opcional
     *                    según estado).
     * @param photos      Lista de fotos de evidencia (opcional según estado).
     * @return ResponseEntity con mensaje de éxito o error.
     */
    @PutMapping("/{id}/status")
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

    /**
     * Busca un servicio de entrega por su ID.
     * 
     * Implementa validación de ownership:
     * - ADMIN puede ver cualquier servicio
     * - MESSENGER solo puede ver servicios asignados a él
     *
     * @param id ID del servicio.
     * @return Datos del servicio encontrado o 404 si no existe.
     * @throws UnauthorizedException si el usuario no tiene permiso para ver el
     *                               servicio.
     */
    @GetMapping("/{id}")
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
            // Verificar que el servicio pertenece al mensajero actual
            if (service.getMessenger() == null ||
                    !service.getMessenger().getDocument().equals(currentUser.getDocument())) {
                logger.warn("SEGURIDAD: Usuario {} intentó acceder a servicio {} sin permiso",
                        currentUserName, id);
                throw new UnauthorizedException("No tienes permiso para ver este servicio");
            }
        }

        return ResponseEntity.ok(responseMapper.toResponse(service));
    }

    /**
     * Obtiene todos los servicios asociados al usuario actual.
     *
     * Si es ADMIN, lista todos los servicios.
     * Si es MESSENGER, lista solo los servicios asignados a él.
     *
     * @return Lista de servicios correspondientes.
     */
    @GetMapping
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

    /**
     * Busca servicios asignados a un mensajero específico.
     *
     * @param messengerId Documento del mensajero.
     * @return Lista de servicios asignados al mensajero.
     */
    @GetMapping("/messenger/{messengerId}")
    public ResponseEntity<List<ServiceDeliveryResponse>> findByMessenger(@PathVariable Long messengerId) {
        List<ServiceDeliveryResponse> responses = serviceDeliveryUseCase.findByMessenger(messengerId).stream()
                .map(responseMapper::toResponse)
                .collect(Collectors.toList());
        return ResponseEntity.ok(responses);
    }

    /**
     * Busca servicios asociados a un concesionario específico.
     *
     * @param dealershipId ID del concesionario.
     * @return Lista de servicios del concesionario.
     */
    @GetMapping("/dealership/{dealershipId}")
    public ResponseEntity<List<ServiceDeliveryResponse>> findByDealership(@PathVariable Long dealershipId) {
        List<ServiceDeliveryResponse> responses = serviceDeliveryUseCase.findByDealership(dealershipId).stream()
                .map(responseMapper::toResponse)
                .collect(Collectors.toList());
        return ResponseEntity.ok(responses);
    }

    /**
     * Busca servicios que se encuentren en un estado específico.
     *
     * @param status Estado por el cual filtrar (PENDING, DELIVERED, etc).
     * @return Lista de servicios con el estado especificado.
     */
    @GetMapping("/status/{status}")
    public ResponseEntity<List<ServiceDeliveryResponse>> findByStatus(@PathVariable Status status) {
        List<ServiceDeliveryResponse> responses = serviceDeliveryUseCase.findByStatus(status).stream()
                .map(responseMapper::toResponse)
                .collect(Collectors.toList());
        return ResponseEntity.ok(responses);
    }

}
