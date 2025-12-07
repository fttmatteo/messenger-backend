package app.adapter.in.rest.controllers;

import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import app.adapter.in.builder.EmployeeBuilder;
import app.adapter.in.builder.PlateBuilder;
import app.adapter.in.builder.DealershipBuilder;
import app.adapter.in.rest.request.DealershipRequest;
import app.adapter.in.rest.request.EmployeeRequest;
import app.adapter.in.rest.request.PlateRequest;
import app.adapter.in.rest.request.ServiceUpdateRequest;
import app.application.exceptions.BusinessException;
import app.application.exceptions.InputsException;
import app.application.usecase.AdminUseCase;
import app.domain.model.Employee;
import app.domain.model.Plate;
import app.domain.model.Dealership;
import app.adapter.in.validators.EmployeeValidator;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import app.domain.services.ManageService;

@RestController
@RequestMapping("/employees")
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

    @Autowired
    private EmployeeBuilder employeeBuilder;
    @Autowired
    private DealershipBuilder dealershipBuilder;
    @Autowired
    private PlateBuilder plateBuilder;
    @Autowired
    private AdminUseCase adminUseCase;
    @Autowired
    private EmployeeValidator employeeValidator;
    @Autowired
    private ManageService manageService;

    @PostMapping("/messenger")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> createMessenger(@RequestBody EmployeeRequest request) {
        try {
            Employee employee = employeeBuilder.build(
                    request.getDocument(),
                    request.getFullName(),
                    request.getPhone(),
                    request.getUserName(),
                    request.getPassword());
            adminUseCase.createMessenger(employee);
            return ResponseEntity.status(HttpStatus.CREATED).body(employee);
        } catch (InputsException ie) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ie.getMessage());
        } catch (BusinessException be) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(be.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }
    }

    @PostMapping("/dealership")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> createDealership(@RequestBody DealershipRequest request) {
        try {
            Dealership dealership = dealershipBuilder.build(
                    request.getName(),
                    request.getAddress(),
                    request.getPhone(),
                    request.getZone());
            adminUseCase.createDealership(dealership);
            return ResponseEntity.status(HttpStatus.CREATED).body(dealership);
        } catch (InputsException ie) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ie.getMessage());
        } catch (BusinessException be) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(be.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }
    }

    @DeleteMapping("/employee/{document}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> deleteEmployee(@PathVariable String document) {
        try {
            long doc = employeeValidator.documentValidator(document);
            adminUseCase.deleteEmployee(doc);
            return ResponseEntity.ok(Map.of("message", "Empleado eliminado correctamente"));
        } catch (InputsException ie) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ie.getMessage());
        } catch (BusinessException be) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(be.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }
    }

    @PostMapping("/plates")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> createPlate(@RequestBody PlateRequest request) {
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            String username = auth.getName();
            Plate plate = plateBuilder.build(request.getPlateNumber());
            adminUseCase.createPlate(plate, username);
            return ResponseEntity.status(HttpStatus.CREATED).body(plate);
        } catch (InputsException ie) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ie.getMessage());
        } catch (BusinessException be) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(be.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }
    }

    @DeleteMapping("/plates/{idPlate}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> deletePlate(@PathVariable Long idPlate) {
        try {
            adminUseCase.deletePlate(idPlate);
            return ResponseEntity.ok(Map.of("message", "Placa eliminada correctamente"));
        } catch (InputsException ie) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ie.getMessage());
        } catch (BusinessException be) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(be.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }
    }

    @DeleteMapping("/dealership/{idDealership}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> deleteDealership(@PathVariable Long idDealership) {
        try {
            adminUseCase.deleteDealership(idDealership);
            return ResponseEntity.ok(Map.of("message", "Dealership eliminado correctamente"));
        } catch (InputsException ie) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ie.getMessage());
        } catch (BusinessException be) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(be.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }
    }

    @PatchMapping("/service/{idService}/status")
    @PreAuthorize("hasRole('ADMIN')")
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
                    "ADMIN");
            return ResponseEntity.ok(Map.of("message", "Servicio actualizado por Administrador"));
        } catch (BusinessException be) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(be.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }
    }
}