package app.adapter.in.rest.controllers;

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
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

/**
 * Controller REST para operaciones de geolocalización.
 * Incluye geocodificación, cálculo de rutas y distancias.
 */
@RestController
@RequestMapping("/api/location")
public class LocationController {

    private final GeocodeDealership geocodeDealership;
    private final CalculateOptimalRoute calculateOptimalRoute;
    private final LocationPort locationPort;

    @Autowired
    public LocationController(
            GeocodeDealership geocodeDealership,
            CalculateOptimalRoute calculateOptimalRoute,
            LocationPort locationPort) {
        this.geocodeDealership = geocodeDealership;
        this.calculateOptimalRoute = calculateOptimalRoute;
        this.locationPort = locationPort;
    }

    /**
     * Geocodifica una dirección.
     * POST /api/location/geocode
     */
    @PostMapping("/geocode")
    public ResponseEntity<LocationResponse> geocodeAddress(
            @Valid @RequestBody GeocodeRequest request) {

        Location location = geocodeDealership.geocodeAddress(request.getAddress());

        String formattedAddress = locationPort.reverseGeocode(location);

        LocationResponse response = new LocationResponse(
                location.getLatitude(),
                location.getLongitude(),
                formattedAddress);

        return ResponseEntity.ok(response);
    }

    /**
     * Calcula una ruta optimizada.
     * POST /api/location/route
     */
    @PostMapping("/route")
    public ResponseEntity<RouteResponse> calculateRoute(
            @Valid @RequestBody RouteRequest request) {

        Route route = calculateOptimalRoute.execute(
                request.getOriginLatitude(),
                request.getOriginLongitude(),
                request.getDealershipIds());

        return ResponseEntity.ok(mapToRouteResponse(route));
    }

    /**
     * Calcula la distancia entre dos puntos.
     * GET /api/location/distance?fromLat=...&fromLng=...&toLat=...&toLng=...
     */
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

    /**
     * Convierte coordenadas en una dirección.
     * GET /api/location/reverse?lat=...&lng=...
     */
    @GetMapping("/reverse")
    public ResponseEntity<LocationResponse> reverseGeocode(
            @RequestParam Double lat,
            @RequestParam Double lng) {

        Location location = new Location(lat, lng);
        String formattedAddress = locationPort.reverseGeocode(location);

        return ResponseEntity.ok(new LocationResponse(lat, lng, formattedAddress));
    }

    private RouteResponse mapToRouteResponse(Route route) {
        RouteResponse response = new RouteResponse();

        if (route.getOrigin() != null) {
            response.setOrigin(new LocationResponse(
                    route.getOrigin().getLatitude(),
                    route.getOrigin().getLongitude(),
                    null));
        }

        if (route.getDestination() != null) {
            response.setDestination(new LocationResponse(
                    route.getDestination().getLatitude(),
                    route.getDestination().getLongitude(),
                    null));
        }

        if (route.getWaypoints() != null && !route.getWaypoints().isEmpty()) {
            List<LocationResponse> waypoints = new ArrayList<>();
            for (Location wp : route.getWaypoints()) {
                waypoints.add(new LocationResponse(
                        wp.getLatitude(),
                        wp.getLongitude(),
                        null));
            }
            response.setWaypoints(waypoints);
        }

        response.setDistanceMeters(route.getDistanceMeters());
        response.setDistanceKilometers(route.getDistanceKilometers());
        response.setDurationSeconds(route.getDurationSeconds());
        response.setDurationFormatted(route.getDurationFormatted());
        response.setPolyline(route.getPolyline());

        return response;
    }
}
