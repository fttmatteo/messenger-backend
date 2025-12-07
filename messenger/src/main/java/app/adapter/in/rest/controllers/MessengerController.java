package app.adapter.in.rest.controllers;

import app.application.exceptions.BusinessException;
import app.application.exceptions.InputsException;
import app.domain.model.Plate;
import app.adapter.in.builder.PlateBuilder;
import app.adapter.in.rest.request.ServiceUpdateRequest;
import app.adapter.in.validators.PlateValidator;
import app.domain.model.Service;
import app.domain.ports.OcrPort;
import app.domain.ports.ServicePort;
import app.domain.services.ManageService;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.beans.factory.annotation.Autowired;
import app.application.usecase.MessengerUseCase;
import java.util.List;
import java.util.Map;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

@RestController
@RequestMapping("/messenger")
@PreAuthorize("hasRole('MESSENGER')")
public class MessengerController {

    @Autowired
    private ServicePort servicePort;
    @Autowired
    private ManageService manageService;
    @Autowired
    private PlateBuilder plateBuilder;
    @Autowired
    private MessengerUseCase messengerUseCase;
    @Autowired
    private OcrPort ocrPort;
    @Autowired
    private PlateValidator plateValidator;

    @PostMapping(value = "/plates", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasRole('MESSENGER')")
    public ResponseEntity<?> createPlate(
            @RequestParam("image") MultipartFile image,
            @RequestParam("idDealership") Long idDealership) {
        try {
            plateValidator.validateOCRInput(image, idDealership);
            String ocrResult = ocrPort.extractText(image.getInputStream());
            String cleanPlateText = ocrResult.toUpperCase()
                    .replaceAll("[^A-Z0-9 ]", "")
                    .trim();
            System.out.println("Texto crudo OCR: " + ocrResult);
            System.out.println("Texto limpio: " + cleanPlateText);
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            String username = auth.getName();
            Plate plate = plateBuilder.build(cleanPlateText);
            messengerUseCase.createPlateAndService(plate, username, idDealership);
            return ResponseEntity.status(HttpStatus.CREATED).body(plate);
        } catch (InputsException ie) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ie.getMessage());
        } catch (BusinessException be) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(be.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error procesando la solicitud: " + e.getMessage());
        }
    }

    @GetMapping("/my-services")
    public ResponseEntity<List<Service>> getMyServices() {
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            String username = auth.getName();
            List<Service> services = servicePort.findAllByEmployeeUserName(username);
            return ResponseEntity.ok(services);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PatchMapping("/service/{idService}/status")
    @PreAuthorize("hasRole('MESSENGER')")
    public ResponseEntity<?> updateServiceStatus(@PathVariable Long idService,
            @RequestBody ServiceUpdateRequest request) {
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            String username = auth.getName();
            manageService.updateStatus(
                    idService,
                    request.getStatus(),
                    request.getObservation(),
                    request.getSignature(),
                    request.getPhoto(),
                    username,
                    "MESSENGER");
            return ResponseEntity.ok(Map.of("message", "Estado actualizado correctamente"));
        } catch (BusinessException be) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(be.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }
    }
}