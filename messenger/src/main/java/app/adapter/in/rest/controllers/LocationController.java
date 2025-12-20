package app.adapter.in.rest.controllers;

import app.adapter.in.rest.mapper.LocationResponseMapper;
import app.adapter.in.rest.request.GeocodeRequest;
import app.adapter.in.rest.request.RouteRequest;
import app.adapter.in.rest.response.DistanceResponse;
import app.adapter.in.rest.response.LocationResponse;
import app.adapter.in.rest.response.RouteResponse;
import app.application.usecase.location.GeocodeDealership;
import app.application.usecase.route.CalculateOptimalRoute;
import app.domain.model.Location;
import app.domain.model.Route;
import app.domain.ports.LocationPort;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * Controlador REST para operaciones de ubicación y rutas.
 *
 * Proporciona cálculo de rutas óptimas utilizando Google Maps Directions API,
 * así como servicios de geocodificación y cálculo de distancias.
 */
@RestController
@RequestMapping("/locations")
@PreAuthorize("isAuthenticated()")
public class LocationController {

    @Autowired
    private GeocodeDealership geocodeDealership;
    @Autowired
    private CalculateOptimalRoute calculateOptimalRoute;
    @Autowired
    private LocationPort locationPort;
    @Autowired
    private LocationResponseMapper responseMapper;

    @PostMapping("/geocode")
    public ResponseEntity<LocationResponse> geocodeAddress(
            @Valid @RequestBody GeocodeRequest request) {
        Location location = geocodeDealership.geocodeAddress(request.getAddress());
        String formattedAddress = locationPort.reverseGeocode(location);
        return ResponseEntity.ok(responseMapper.toLocationResponse(location, formattedAddress));
    }

    @PostMapping("/route")
    public ResponseEntity<RouteResponse> calculateRoute(
            @Valid @RequestBody RouteRequest request) {
        Route route = calculateOptimalRoute.execute(
                request.getOriginLatitude(),
                request.getOriginLongitude(),
                request.getDealershipIds());
        return ResponseEntity.ok(responseMapper.toRouteResponse(route));
    }

    @GetMapping("/distance")
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
    public ResponseEntity<LocationResponse> reverseGeocode(
            @RequestParam Double lat,
            @RequestParam Double lng) {
        Location location = new Location(lat, lng);
        String formattedAddress = locationPort.reverseGeocode(location);
        return ResponseEntity.ok(responseMapper.toLocationResponse(location, formattedAddress));
    }
}
