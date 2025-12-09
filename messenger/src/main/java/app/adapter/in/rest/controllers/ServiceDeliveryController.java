package app.adapter.in.rest.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import app.adapter.in.builder.ServiceDeliveryBuilder;
import app.adapter.in.rest.mapper.ServiceDeliveryResponseMapper;
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
            @RequestParam("dealershipId") String dealershipIdStr,
            @RequestParam("messengerDocument") String messengerDocumentStr) {
        try {
            Long dealershipId = builder.buildDealershipId(dealershipIdStr);
            Long messengerDocument = builder.buildMessengerDocument(messengerDocumentStr);

            File imageFile = convertToFile(image);
            serviceDeliveryUseCase.createServiceFromImage(imageFile, dealershipId, messengerDocument);
            // Ideally should delete temp file handling, but StoragePort copies it.
            // We can delete it here if StoragePort copies. FileSystemStorageAdapter uses
            // Files.copy
            // with StandardCopyOption.REPLACE_EXISTING.
            // We should use a try-finally and delete the temp file.
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
            @RequestParam("status") String statusStr,
            @RequestParam(value = "observation", required = false) String observationStr,
            @RequestParam(value = "signature", required = false) MultipartFile signature,
            @RequestParam(value = "photos", required = false) List<MultipartFile> photos,
            @RequestParam("userDocument") String userDocumentStr) {

        List<File> tempFiles = new ArrayList<>();
        try {
            Status status = builder.buildStatus(statusStr);
            String observation = builder.buildObservation(observationStr);
            Long userDocument = builder.buildUserDocument(userDocumentStr);

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

            serviceDeliveryUseCase.updateStatusWithFiles(id, status, observation, signatureFile, photoFiles,
                    userDocument);

            return ResponseEntity.ok("Estado actualizado exitosamente.");

        } catch (InputsException | BusinessException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error interno: " + e.getMessage());
        } finally {
            // Cleanup temp files
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
