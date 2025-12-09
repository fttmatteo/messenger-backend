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
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/services")
public class ServiceDeliveryController {

    @Autowired
    private ServiceDeliveryUseCase serviceDeliveryUseCase;
    @Autowired
    private ServiceDeliveryBuilder builder;
    @Autowired
    private ServiceDeliveryResponseMapper responseMapper;

    @PostMapping("/create")
    public ResponseEntity<?> createService(
            @RequestParam("image") MultipartFile image,
            @RequestParam("dealershipId") String dealershipId,
            @RequestParam("messengerDocument") String messengerDocument,
            @RequestParam(value = "manualPlateNumber", required = false) String manualPlateNumber) {
        try {
            ServiceDeliveryCreateRequest request = new ServiceDeliveryCreateRequest(dealershipId, messengerDocument);
            request.setManualPlateNumber(manualPlateNumber); // NUEVO: Parámetro opcional

            ServiceDeliveryBuilder.ServiceDeliveryCreateData data = builder.buildCreateData(request);

            File imageFile = convertToFile(image);

            // NUEVO: Log si se proporciona placa manual como fallback
            if (manualPlateNumber != null && !manualPlateNumber.isEmpty()) {
                System.out.println("Placa manual proporcionada como fallback: " + manualPlateNumber);
            }

            serviceDeliveryUseCase.createServiceFromImage(
                    imageFile,
                    data.getDealershipId(),
                    data.getMessengerDocument());

            return ResponseEntity.status(HttpStatus.CREATED).body("Servicio creado exitosamente.");
        } catch (InputsException | BusinessException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error interno: " + e.getMessage());
        }
    }

    @PutMapping("/{id}/status")
    public ResponseEntity<?> updateStatus(
            @PathVariable Long id,
            @RequestParam("status") String status,
            @RequestParam(value = "observation", required = false) String observation,
            @RequestParam(value = "signature", required = false) MultipartFile signature,
            @RequestParam(value = "photos", required = false) List<MultipartFile> photos,
            @RequestParam("userDocument") String userDocument) {

        List<File> tempFiles = new ArrayList<>();
        try {
            ServiceDeliveryUpdateStatusRequest request = new ServiceDeliveryUpdateStatusRequest(status, observation,
                    userDocument);
            ServiceDeliveryBuilder.ServiceDeliveryUpdateData data = builder.buildUpdateStatusData(request);

            File signatureFile = null;
            if (signature != null && !signature.isEmpty()) {
                signatureFile = convertToFile(signature);
                tempFiles.add(signatureFile);
            }

            List<File> photoFiles = new ArrayList<>();
            if (photos != null && !photos.isEmpty()) {
                for (MultipartFile mf : photos) {
                    if (!mf.isEmpty()) {
                        File f = convertToFile(mf);
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

    @GetMapping("/{id}")
    public ResponseEntity<?> findById(@PathVariable Long id) {
        try {
            ServiceDelivery service = serviceDeliveryUseCase.findById(id);
            if (service == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
            }
            return ResponseEntity.ok(responseMapper.toResponse(service));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }
    }

    @GetMapping
    public ResponseEntity<List<ServiceDeliveryResponse>> findAll() {
        List<ServiceDeliveryResponse> responses = serviceDeliveryUseCase.findAll().stream()
                .map(responseMapper::toResponse)
                .collect(Collectors.toList());
        return ResponseEntity.ok(responses);
    }

    @GetMapping("/messenger/{messengerId}")
    public ResponseEntity<List<ServiceDeliveryResponse>> findByMessenger(@PathVariable Long messengerId) {
        List<ServiceDeliveryResponse> responses = serviceDeliveryUseCase.findByMessenger(messengerId).stream()
                .map(responseMapper::toResponse)
                .collect(Collectors.toList());
        return ResponseEntity.ok(responses);
    }

    @GetMapping("/dealership/{dealershipId}")
    public ResponseEntity<List<ServiceDeliveryResponse>> findByDealership(@PathVariable Long dealershipId) {
        List<ServiceDeliveryResponse> responses = serviceDeliveryUseCase.findByDealership(dealershipId).stream()
                .map(responseMapper::toResponse)
                .collect(Collectors.toList());
        return ResponseEntity.ok(responses);
    }

    @GetMapping("/status/{status}")
    public ResponseEntity<List<ServiceDeliveryResponse>> findByStatus(@PathVariable Status status) {
        List<ServiceDeliveryResponse> responses = serviceDeliveryUseCase.findByStatus(status).stream()
                .map(responseMapper::toResponse)
                .collect(Collectors.toList());
        return ResponseEntity.ok(responses);
    }

    private File convertToFile(MultipartFile multipartFile) throws IOException {
        // Create a temp file
        File tempFile = File.createTempFile("upload-", multipartFile.getOriginalFilename());
        multipartFile.transferTo(tempFile);
        return tempFile;
    }
}
