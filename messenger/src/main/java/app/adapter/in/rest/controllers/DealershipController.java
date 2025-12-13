package app.adapter.in.rest.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.access.prepost.PreAuthorize;
import app.adapter.in.builder.DealershipBuilder;
import app.adapter.in.rest.mapper.DealershipResponseMapper;
import app.adapter.in.rest.request.DealershipRequest;
import app.adapter.in.rest.response.DealershipResponse;
import app.application.exceptions.BusinessException;
import app.application.exceptions.GeolocationException;
import app.application.exceptions.InputsException;
import app.application.usecase.DealershipUseCase;
import app.application.usecase.location.GeocodeDealership;
import app.domain.model.Dealership;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Controlador REST para gestionar concesionarios.
 * 
 * Proporciona operaciones CRUD completas y geocodificación mediante Google Maps
 * API. Requiere rol ADMIN para operaciones de modificación.
 */
@RestController
@RequestMapping("/dealerships")
public class DealershipController {

    @Autowired
    private DealershipUseCase dealershipUseCase;
    @Autowired
    private DealershipBuilder builder;
    @Autowired
    private DealershipResponseMapper responseMapper;
    @Autowired
    private GeocodeDealership geocodeDealership;

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
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
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<DealershipResponse>> findAll() {
        List<DealershipResponse> responses = dealershipUseCase.findAll().stream()
                .map(responseMapper::toResponse)
                .collect(Collectors.toList());
        return ResponseEntity.ok(responses);
    }

    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> findById(@PathVariable Long id) {
        try {
            Dealership dealership = dealershipUseCase.findById(id);
            if (dealership == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Concesionario no encontrado");
            }
            return ResponseEntity.ok(responseMapper.toResponse(dealership));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> update(@PathVariable Long id, @RequestBody DealershipRequest request) {
        try {
            Dealership dealership = builder.build(request);
            dealershipUseCase.update(id, dealership);
            return ResponseEntity.ok("Concesionario actualizado exitosamente");
        } catch (InputsException | BusinessException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> delete(@PathVariable Long id) {
        try {
            dealershipUseCase.deleteById(id);
            return ResponseEntity.ok("Concesionario eliminado exitosamente");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }
    }

    /**
     * Geocodifica un concesionario existente usando Google Maps Geocoding API.
     * Actualiza las coordenadas (lat/lng) del concesionario.
     * POST /dealerships/{id}/geocode
     */
    @PostMapping("/{id}/geocode")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> geocodeDealership(@PathVariable Long id) {
        try {
            Dealership dealership = geocodeDealership.execute(id);
            return ResponseEntity.ok(responseMapper.toResponse(dealership));
        } catch (GeolocationException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }
    }
}
