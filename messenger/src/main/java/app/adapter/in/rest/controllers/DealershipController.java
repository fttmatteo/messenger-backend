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

    /**
     * Crea un nuevo concesionario.
     *
     * @param request Datos del concesionario a crear.
     * @return ResponseEntity con mensaje de éxito o error.
     */
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<String> create(@Valid @RequestBody DealershipRequest request) throws Exception {
        Dealership dealership = builder.build(request);
        dealershipUseCase.create(dealership);
        return ResponseEntity.status(HttpStatus.CREATED).body("Concesionario creado exitosamente");
    }

    /**
     * Obtiene todos los concesionarios registrados.
     *
     * @return Lista de concesionarios.
     */
    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<DealershipResponse>> findAll() {
        List<DealershipResponse> responses = dealershipUseCase.findAll().stream()
                .map(responseMapper::toResponse)
                .collect(Collectors.toList());
        return ResponseEntity.ok(responses);
    }

    /**
     * Busca un concesionario por su ID.
     *
     * @param id ID del concesionario.
     * @return Datos del concesionario encontrado o 404 si no existe.
     */
    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<DealershipResponse> findById(@PathVariable Long id) throws Exception {
        Dealership dealership = dealershipUseCase.findById(id);
        return ResponseEntity.ok(responseMapper.toResponse(dealership));
    }

    /**
     * Actualiza los datos de un concesionario existente.
     *
     * @param id      ID del concesionario a actualizar.
     * @param request Nuevos datos del concesionario.
     * @return ResponseEntity con mensaje de éxito o error.
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<String> update(@PathVariable Long id, @Valid @RequestBody DealershipRequest request)
            throws Exception {
        Dealership dealership = builder.build(request);
        dealershipUseCase.update(id, dealership);
        return ResponseEntity.ok("Concesionario actualizado exitosamente");
    }

    /**
     * Elimina un concesionario por su ID.
     *
     * @param id ID del concesionario a eliminar.
     * @return ResponseEntity con mensaje de éxito o error.
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<String> delete(@PathVariable Long id) throws Exception {
        dealershipUseCase.deleteById(id);
        return ResponseEntity.ok("Concesionario eliminado exitosamente");
    }

    /**
     * Geocodifica un concesionario existente usando Google Maps Geocoding API.
     * Actualiza las coordenadas (lat/lng) del concesionario.
     *
     * @param id ID del concesionario a geocodificar.
     * @return ResponseEntity con los datos del concesionario actualizado o mensaje
     *         de error.
     */
    @PostMapping("/{id}/geocode")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<DealershipResponse> geocodeDealership(@PathVariable Long id)
            throws GeolocationException, Exception {
        Dealership dealership = geocodeDealership.execute(id);
        return ResponseEntity.ok(responseMapper.toResponse(dealership));
    }
}
