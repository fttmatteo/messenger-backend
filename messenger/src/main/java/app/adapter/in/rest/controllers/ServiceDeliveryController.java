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
    @Autowired
    private app.domain.ports.EmployeePort employeePort;

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

            // If user is not ADMIN, force assignment to themselves
            String finalMessengerDocument = messengerDocument;
            if (currentUser.getRole() != app.domain.model.enums.Role.ADMIN) {
                finalMessengerDocument = String.valueOf(currentUser.getDocument());
            } else {
                // If ADMIN, messengerDocument is required
                if (messengerDocument == null || messengerDocument.trim().isEmpty()) {
                    return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                            .body("Messenger document is required for Admin users.");
                }
            }

            ServiceDeliveryCreateRequest request = new ServiceDeliveryCreateRequest(dealershipId,
                    finalMessengerDocument);
            request.setManualPlateNumber(manualPlateNumber);

            ServiceDeliveryBuilder.ServiceDeliveryCreateData data = builder.buildCreateData(request);

            imageFile = convertToFile(image);

            // Use manual plate number if provided, otherwise use OCR
            if (manualPlateNumber != null && !manualPlateNumber.isEmpty()) {
                System.out.println("Using manual plate number: " + manualPlateNumber);
                serviceDeliveryUseCase.createServiceWithManualPlate(
                        imageFile,
                        manualPlateNumber,
                        data.getDealershipId(),
                        data.getMessengerDocument());
            } else {
                System.out.println("Using OCR to detect plate number");
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
        org.springframework.security.core.Authentication auth = org.springframework.security.core.context.SecurityContextHolder
                .getContext().getAuthentication();
        String currentUserName = auth.getName();
        app.domain.model.Employee currentUser = employeePort.findByUserName(currentUserName);

        List<ServiceDelivery> services;

        if (currentUser != null && currentUser.getRole() == app.domain.model.enums.Role.MESSENGER) {
            services = serviceDeliveryUseCase.findByMessenger(currentUser.getIdEmployee());
        } else {
            services = serviceDeliveryUseCase.findAll();
        }

        List<ServiceDeliveryResponse> responses = services.stream()
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
        String originalName = multipartFile.getOriginalFilename();
        String extension = "";

        if (originalName != null && originalName.contains(".")) {
            extension = originalName.substring(originalName.lastIndexOf("."));
        }

        if (extension.isEmpty()) {
            String contentType = multipartFile.getContentType();
            if (contentType != null) {
                switch (contentType) {
                    case "image/jpeg":
                    case "image/jpg":
                        extension = ".jpeg";
                        break;
                    case "image/png":
                        extension = ".png";
                        break;
                    case "application/pdf":
                        extension = ".pdf";
                        break;
                }
            }
        }

        if (extension.isEmpty() || ".bin".equals(extension)) {
            try (java.io.InputStream is = multipartFile.getInputStream()) {
                byte[] header = new byte[8];
                int read = is.read(header);
                if (read >= 4) {
                    if (header[0] == (byte) 0x89 && header[1] == (byte) 0x50 &&
                            header[2] == (byte) 0x4E && header[3] == (byte) 0x47) {
                        extension = ".png";
                    } else if (header[0] == (byte) 0xFF && header[1] == (byte) 0xD8 && header[2] == (byte) 0xFF) {
                        extension = ".jpeg";
                    } else if (header[0] == (byte) 0x25 && header[1] == (byte) 0x50 &&
                            header[2] == (byte) 0x44 && header[3] == (byte) 0x46) {
                        extension = ".pdf";
                    }
                }
            } catch (Exception e) {
                System.err.println("Error detecting extension from bytes: " + e.getMessage());
            }
        }

        if (extension.isEmpty()) {
            extension = ".tmp";
        }

        System.out.println("DEBUG: Incoming file: " + originalName);
        System.out.println("DEBUG: Content-Type: " + multipartFile.getContentType());
        System.out.println("DEBUG: Final Extension: " + extension);

        File tempFile = File.createTempFile("upload-", extension);
        multipartFile.transferTo(tempFile);
        return tempFile;
    }
}
