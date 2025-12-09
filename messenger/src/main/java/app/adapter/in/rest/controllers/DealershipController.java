package app.adapter.in.rest.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import app.adapter.in.builder.DealershipBuilder;
import app.adapter.in.rest.request.DealershipRequest;
import app.application.exceptions.BusinessException;
import app.application.exceptions.InputsException;
import app.application.usecase.DealershipUseCase;
import app.domain.model.Dealership;

import java.util.List;

@RestController
@RequestMapping("/dealerships")
public class DealershipController {

    @Autowired
    private DealershipUseCase dealershipUseCase;

    @Autowired
    private DealershipBuilder builder;

    @PostMapping
    public ResponseEntity<?> create(@RequestBody DealershipRequest request) {
        try {
            Dealership dealership = builder.build(request);
            dealershipUseCase.create(dealership);
            return ResponseEntity.status(HttpStatus.CREATED).body("Concesionario creado exitosamente");
        } catch (InputsException | BusinessException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }
    }

    @GetMapping
    public ResponseEntity<List<Dealership>> findAll() {
        return ResponseEntity.ok(dealershipUseCase.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> findById(@PathVariable Long id) {
        try {
            Dealership dealership = dealershipUseCase.findById(id);
            if (dealership == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Concesionario no encontrado");
            }
            return ResponseEntity.ok(dealership);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> update(@PathVariable Long id, @RequestBody DealershipRequest request) {
        try {
            Dealership dealership = builder.build(request);
            dealership.setIdDealership(id);
            dealershipUseCase.update(dealership);
            return ResponseEntity.ok("Concesionario actualizado exitosamente");
        } catch (InputsException | BusinessException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable Long id) {
        try {
            dealershipUseCase.deleteById(id);
            return ResponseEntity.ok("Concesionario eliminado exitosamente");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }
    }
}
