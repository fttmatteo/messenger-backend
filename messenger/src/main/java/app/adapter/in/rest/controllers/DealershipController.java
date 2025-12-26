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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Controlador REST para gestión de concesionarios.
 */
@RestController
@RequestMapping("/dealerships")
@PreAuthorize("isAuthenticated()")
public class DealershipController {

    private static final Logger logger = LoggerFactory.getLogger(DealershipController.class);

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
        logger.info("Solicitud para crear concesionario: {}", request.getName());
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
     * Busca un concesionario por su ID.
     */
    @GetMapping("/findByDealershipId/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<DealershipResponse> findById(@PathVariable Long id) throws Exception {
        Dealership dealership = dealershipUseCase.findById(id);
        return ResponseEntity.ok(responseMapper.toResponse(dealership));
    }

    /**
     * Actualiza los datos de un concesionario (solo ADMIN).
     */
    @PutMapping("/updateDealership/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<DealershipResponse> update(@PathVariable Long id,
            @Valid @RequestBody DealershipRequest request)
            throws Exception {
        Dealership dealership = builder.build(request);
        Dealership updated = dealershipUseCase.update(id, dealership);
        return ResponseEntity.ok(responseMapper.toResponse(updated));
    }

    /**
     * Elimina un concesionario por su ID (solo ADMIN).
     */
    @DeleteMapping("/deleteDealership/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> delete(@PathVariable Long id) throws Exception {
        logger.info("Solicitud para eliminar concesionario ID: {}", id);
        dealershipUseCase.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * Geocodifica la dirección de un concesionario para obtener sus coordenadas
     * (solo ADMIN).
     */
    @PostMapping("/geocodeDealership/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<DealershipResponse> geocodeDealership(@PathVariable Long id)
            throws GeolocationException, Exception {
        Dealership dealership = geocodeDealership.execute(id);
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
