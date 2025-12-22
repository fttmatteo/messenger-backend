package app.adapter.in.rest.controllers;

import app.adapter.in.rest.mapper.LocationResponseMapper;
import app.adapter.in.rest.request.GeocodeRequest;
import app.adapter.in.rest.request.RouteRequest;
import app.adapter.in.rest.response.DistanceResponse;
import app.adapter.in.rest.response.LocationResponse;
import app.adapter.in.rest.response.RouteResponse;
import app.application.usecase.location.GeocodeDealershipUseCase;
import app.application.usecase.route.CalculateOptimalRouteUseCase;
import app.domain.model.Location;
import app.domain.model.Route;
import app.domain.ports.LocationPort;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Controlador REST para geocodificación y cálculo de rutas.
 */
@RestController
@RequestMapping("/locations")
@PreAuthorize("isAuthenticated()")
public class LocationController {

    private static final Logger logger = LoggerFactory.getLogger(LocationController.class);

    @Autowired
    private GeocodeDealershipUseCase geocodeDealership;
    @Autowired
    private CalculateOptimalRouteUseCase calculateOptimalRoute;
    @Autowired
    private LocationPort locationPort;
    @Autowired
    private LocationResponseMapper responseMapper;

    @PostMapping("/geocode")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<LocationResponse> geocodeAddress(
            @Valid @RequestBody GeocodeRequest request) {
        logger.info("Solicitud geocodificación: {}", request.getAddress());
        Location location = geocodeDealership.geocodeAddress(request.getAddress());
        String formattedAddress = locationPort.reverseGeocode(location);
        return ResponseEntity.ok(responseMapper.toLocationResponse(location, formattedAddress));
    }

    @PostMapping("/route")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<RouteResponse> calculateRoute(
            @Valid @RequestBody RouteRequest request) {
        logger.info("Solicitud cálculo ruta desde {},{}", request.getOriginLatitude(), request.getOriginLongitude());
        Route route = calculateOptimalRoute.execute(
                request.getOriginLatitude(),
                request.getOriginLongitude(),
                request.getDealershipIds());
        return ResponseEntity.ok(responseMapper.toRouteResponse(route));
    }

    @GetMapping("/distance")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<DistanceResponse> calculateDistance(
            @RequestParam Double fromLat,
            @RequestParam Double fromLng,
            @RequestParam Double toLat,
            @RequestParam Double toLng) {
        Location from = new Location(fromLat, fromLng);
        Location to = new Location(toLat, toLng);
        Double distanceMeters = locationPort.calculateDistance(from, to);
        // Estimamos duración basado en velocidad promedio de 40 km/h
        Long durationSeconds = distanceMeters != null
                ? Math.round(distanceMeters / 11.11) // 40 km/h = 11.11 m/s
                : null;
        return ResponseEntity.ok(new DistanceResponse(distanceMeters, durationSeconds));
    }

    @GetMapping("/reverse")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<LocationResponse> reverseGeocode(
            @RequestParam Double lat,
            @RequestParam Double lng) {
        Location location = new Location(lat, lng);
        String formattedAddress = locationPort.reverseGeocode(location);
        return ResponseEntity.ok(responseMapper.toLocationResponse(location, formattedAddress));
    }
}
