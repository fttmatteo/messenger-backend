package app.adapter.in.rest.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import app.adapter.in.builder.ServiceDeliveryBuilder;
import app.adapter.in.rest.mapper.ServiceDeliveryResponseMapper;
import app.adapter.in.rest.request.ServiceDeliveryCreateRequest;
import app.adapter.in.rest.request.ServiceDeliveryUpdateStatusRequest;
import app.adapter.in.rest.response.ServiceDeliveryResponse;
import app.application.exceptions.BusinessException;
import app.application.exceptions.InputsException;
import app.application.usecase.ServiceDeliveryUseCase;
import app.domain.model.ServiceDelivery;
import app.domain.model.enums.Status;
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
    private app.domain.ports.EmployeePort employeePort;
    @Autowired
    private app.infrastructure.helper.FileHelper fileHelper;

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
            @RequestParam(value = "manualPlateNumber", required = false) String manualPlateNumber) {

        File imageFile = null;
        try {
            org.springframework.security.core.Authentication auth = org.springframework.security.core.context.SecurityContextHolder
                    .getContext().getAuthentication();
            String currentUserName = auth.getName();
            app.domain.model.Employee currentUser = employeePort.findByUserName(currentUserName);

            if (currentUser == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("User authentication not found or invalid.");
            }

            String finalMessengerDocument = messengerDocument;
            if (currentUser.getRole() != app.domain.model.enums.Role.ADMIN) {
                finalMessengerDocument = String.valueOf(currentUser.getDocument());
            } else {
                if (messengerDocument == null || messengerDocument.trim().isEmpty()) {
                    return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                            .body("Messenger document is required for Admin users.");
                }
            }

            ServiceDeliveryCreateRequest request = new ServiceDeliveryCreateRequest(dealershipId,
                    finalMessengerDocument);
            request.setManualPlateNumber(manualPlateNumber);

            ServiceDeliveryBuilder.ServiceDeliveryCreateData data = builder.buildCreateData(request);

            imageFile = fileHelper.convertToFile(image);

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
        } catch (InputsException | BusinessException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error interno: " + e.getMessage());
        } finally {
            if (imageFile != null && imageFile.exists()) {
                imageFile.delete();
            }
        }
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
            @RequestParam(value = "photos", required = false) List<MultipartFile> photos) {

        List<File> tempFiles = new ArrayList<>();
        try {
            org.springframework.security.core.Authentication auth = org.springframework.security.core.context.SecurityContextHolder
                    .getContext().getAuthentication();
            String currentUserName = auth.getName();
            app.domain.model.Employee currentUser = employeePort.findByUserName(currentUserName);

            if (currentUser == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("User authentication not found or invalid.");
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

            List<File> photoFiles = new ArrayList<>();
            if (photos != null && !photos.isEmpty()) {
                for (MultipartFile mf : photos) {
                    if (!mf.isEmpty()) {
                        File f = fileHelper.convertToFile(mf);
                        photoFiles.add(f);
                        tempFiles.add(f);
                    }
                }
            }

            serviceDeliveryUseCase.updateStatusWithFiles(id, data.getStatus(), data.getObservation(),
                    signatureFile, photoFiles, data.getUserDocument());

            return ResponseEntity.ok("Estado actualizado exitosamente.");

        } catch (InputsException | BusinessException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error interno: " + e.getMessage());
        } finally {
            for (File f : tempFiles) {
                if (f.exists()) {
                    f.delete();
                }
            }
        }
    }

    /**
     * Busca un servicio de entrega por su ID.
     *
     * @param id ID del servicio.
     * @return Datos del servicio encontrado o 404 si no existe.
     */
    @GetMapping("/{id}")
    public ResponseEntity<ServiceDeliveryResponse> findById(@PathVariable Long id) throws Exception {
        ServiceDelivery service = serviceDeliveryUseCase.findById(id);
        if (service == null) {
            throw new app.application.exceptions.ResourceNotFoundException("Servicio con ID " + id + " no encontrado");
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
        org.springframework.security.core.Authentication auth = org.springframework.security.core.context.SecurityContextHolder
                .getContext().getAuthentication();
        String currentUserName = auth.getName();
        app.domain.model.Employee currentUser = employeePort.findByUserName(currentUserName);

        List<ServiceDelivery> services;

        if (currentUser != null && currentUser.getRole() == app.domain.model.enums.Role.MESSENGER) {
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
