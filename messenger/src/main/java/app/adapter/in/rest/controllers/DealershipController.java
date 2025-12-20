package app.adapter.in.rest.controllers;

import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.access.prepost.PreAuthorize;
import app.adapter.in.builder.DealershipBuilder;
import app.adapter.in.rest.mapper.DealershipResponseMapper;
import app.adapter.in.rest.request.DealershipRequest;
import app.adapter.in.rest.response.DealershipResponse;
import app.application.exceptions.GeolocationException;
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

    @PostMapping("/createDealership")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<DealershipResponse> create(@Valid @RequestBody DealershipRequest request) throws Exception {
        Dealership dealership = builder.build(request);
        Dealership created = dealershipUseCase.create(dealership);
        return ResponseEntity.status(HttpStatus.CREATED).body(responseMapper.toResponse(created));
    }

    @GetMapping("/allDealerships")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<DealershipResponse>> findAll() {
        List<DealershipResponse> responses = dealershipUseCase.findAll().stream()
                .map(responseMapper::toResponse)
                .collect(Collectors.toList());
        return ResponseEntity.ok(responses);
    }

    @GetMapping("/findDealership/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<DealershipResponse> findById(@PathVariable Long id) throws Exception {
        Dealership dealership = dealershipUseCase.findById(id);
        return ResponseEntity.ok(responseMapper.toResponse(dealership));
    }

    @PutMapping("/updateDealership/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<DealershipResponse> update(@PathVariable Long id,
            @Valid @RequestBody DealershipRequest request)
            throws Exception {
        Dealership dealership = builder.build(request);
        Dealership updated = dealershipUseCase.update(id, dealership);
        return ResponseEntity.ok(responseMapper.toResponse(updated));
    }

    @DeleteMapping("/deleteDealership/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> delete(@PathVariable Long id) throws Exception {
        dealershipUseCase.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/geocodeDealership/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<DealershipResponse> geocodeDealership(@PathVariable Long id)
            throws GeolocationException, Exception {
        Dealership dealership = geocodeDealership.execute(id);
        return ResponseEntity.ok(responseMapper.toResponse(dealership));
    }

    @PostMapping("/findDealershipByName/{name}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<DealershipResponse> findByName(@PathVariable String name) throws Exception {
        Dealership dealership = dealershipUseCase.findByName(name);
        return ResponseEntity.ok(responseMapper.toResponse(dealership));
    }
}
