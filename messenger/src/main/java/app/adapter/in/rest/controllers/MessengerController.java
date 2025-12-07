package app.adapter.in.rest.controllers;

import app.application.exceptions.BusinessException;
import app.application.exceptions.InputsException;
import app.application.usecase.MessengerUseCase;
import app.domain.model.Photo;
import app.domain.model.Plate;
import app.domain.model.Service;
import app.domain.model.Signature;
import app.domain.model.enums.PhotoType;
import app.domain.ports.ServicePort;
import app.domain.services.UpdateService;
import app.adapter.in.builder.PhotoBuilder;
import app.adapter.in.builder.SignatureBuilder;
import app.adapter.in.rest.request.ServiceRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/messenger")
@PreAuthorize("hasAnyRole('MESSENGER', 'ADMIN')")
public class MessengerController {

    @Autowired
    private MessengerUseCase messengerUseCase;
    @Autowired
    private UpdateService updateService;
    @Autowired
    private ServicePort servicePort;

    // Inyectamos los builders aquí
    @Autowired
    private PhotoBuilder photoBuilder;
    @Autowired
    private SignatureBuilder signatureBuilder;

    @PostMapping(value = "/create-plate", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasRole('MESSENGER') or hasRole('ADMIN')")
    public ResponseEntity<?> createPlate(
            @RequestParam("image") MultipartFile image,
            @RequestParam("idDealership") Long idDealership) {
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            Plate plate = messengerUseCase.processAndCreatePlateService(image, idDealership, auth.getName());
            return ResponseEntity.status(HttpStatus.CREATED).body(plate);
        } catch (InputsException ie) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ie.getMessage());
        } catch (BusinessException be) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(be.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }
    }

    @GetMapping("/query-services")
    @PreAuthorize("hasAnyRole('MESSENGER', 'ADMIN')")
    public ResponseEntity<List<Service>> getMyServices() {
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            List<Service> services = servicePort.findAllByEmployeeUserName(auth.getName());
            return ResponseEntity.ok(services);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PatchMapping("/update-service/{idService}/status")
    @PreAuthorize("hasAnyRole('MESSENGER', 'ADMIN')")
    public ResponseEntity<?> updateServiceStatus(@PathVariable Long idService,
            @RequestBody ServiceRequest request) {
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            String role = auth.getAuthorities().iterator().next().getAuthority().replace("ROLE_", "");

            // 1. Construimos los objetos con los Builders (Capa Adaptadora)
            Signature signature = signatureBuilder.build(request.getSignature());
            Photo photo = photoBuilder.build(request.getPhoto(), PhotoType.VISIT);

            // 2. Llamamos al servicio pasando OBJETOS DE DOMINIO, no strings sueltos
            updateService.updateStatus(
                    idService,
                    request.getStatus(),
                    request.getObservation(),
                    signature,
                    photo,
                    auth.getName(),
                    role);

            return ResponseEntity.ok(Map.of("message", "Estado actualizado correctamente"));
        } catch (BusinessException be) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(be.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }
    }
}