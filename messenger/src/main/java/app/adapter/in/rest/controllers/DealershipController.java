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
import app.domain.exception.GeolocationException;
import app.application.usecase.DealershipUseCase;
import app.application.usecase.location.GeocodeDealershipUseCase;
import app.domain.model.Dealership;
import java.util.List;
import java.util.stream.Collectors;
/**
 * Controlador REST para gestión de concesionarios.
 */
@RestController
@RequestMapping("/dealerships")
@PreAuthorize("isAuthenticated()")
public class DealershipController {

    @Autowired
    private DealershipUseCase dealershipUseCase;
    @Autowired
    private DealershipBuilder builder;
    @Autowired
    private DealershipResponseMapper responseMapper;
    @Autowired
    private GeocodeDealershipUseCase geocodeDealership;

    /**
     * Crea un nuevo concesionario (solo ADMIN).
     */
    @PostMapping("/createDealership")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<DealershipResponse> create(@Valid @RequestBody DealershipRequest request) throws Exception {
        Dealership dealership = builder.build(request);
        Dealership created = dealershipUseCase.create(dealership);
        return ResponseEntity.status(HttpStatus.CREATED).body(responseMapper.toResponse(created));
    }

    /**
     * Obtiene todos los concesionarios registrados.
     */
    @GetMapping("/allDealerships")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<DealershipResponse>> findAll() {
        List<DealershipResponse> responses = dealershipUseCase.findAll().stream()
                .map(responseMapper::toResponse)
                .collect(Collectors.toList());
        return ResponseEntity.ok(responses);
    }

    /**
     * Busca un concesionario por su UUID.
     */
    @GetMapping("/findByDealershipId/{uuid}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<DealershipResponse> findByUuid(@PathVariable String uuid) throws Exception {
        Dealership dealership = dealershipUseCase.findByUuid(uuid);
        return ResponseEntity.ok(responseMapper.toResponse(dealership));
    }

    /**
     * Actualiza los datos de un concesionario (solo ADMIN).
     */
    @PutMapping("/updateDealership/{uuid}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<DealershipResponse> update(@PathVariable String uuid,
            @Valid @RequestBody DealershipRequest request)
            throws Exception {
        Dealership dealership = builder.build(request);
        Dealership existing = dealershipUseCase.findByUuid(uuid);
        Dealership updated = dealershipUseCase.update(existing.getIdDealership(), dealership);
        return ResponseEntity.ok(responseMapper.toResponse(updated));
    }

    /**
     * Elimina un concesionario por su UUID (solo ADMIN).
     */
    @DeleteMapping("/deleteDealership/{uuid}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> delete(@PathVariable String uuid) throws Exception {
        Dealership existing = dealershipUseCase.findByUuid(uuid);
        dealershipUseCase.deleteById(existing.getIdDealership());
        return ResponseEntity.noContent().build();
    }

    /**
     * Geocodifica la dirección de un concesionario para obtener sus coordenadas
     * (solo ADMIN).
     */
    @PostMapping("/geocodeDealership/{uuid}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<DealershipResponse> geocodeDealership(@PathVariable String uuid)
            throws GeolocationException, Exception {
        Dealership existing = dealershipUseCase.findByUuid(uuid);
        Dealership dealership = geocodeDealership.execute(existing.getIdDealership());
        return ResponseEntity.ok(responseMapper.toResponse(dealership));
    }

    /**
     * Busca un concesionario por nombre (coincidencia parcial).
     */
    @GetMapping("/findByDealershipName/{name}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<DealershipResponse> findByName(@PathVariable String name) throws Exception {
        Dealership dealership = dealershipUseCase.findByName(name);
        return ResponseEntity.ok(responseMapper.toResponse(dealership));
    }
}
