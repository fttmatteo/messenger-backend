package app.adapter.in.rest.location;

import app.adapter.in.rest.tracking.RouteRequest;
import app.adapter.in.rest.tracking.DistanceResponse;
import app.adapter.in.rest.tracking.RouteResponse;
import app.application.usecase.location.GeocodeDealershipUseCase;
import app.application.usecase.route.CalculateOptimalRouteUseCase;
import app.application.usecase.route.OptimizeDeliveriesRouteUseCase;
import app.domain.model.Location;
import app.domain.model.Route;
import app.domain.model.DeliveryRoute;
import app.domain.ports.LocationPort;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * Controlador REST para geocodificación y cálculo de rutas.
 */
@RestController
@RequestMapping("/locations")
@PreAuthorize("isAuthenticated()")
public class LocationController {

    @Autowired
    private GeocodeDealershipUseCase geocodeDealership;
    @Autowired
    private CalculateOptimalRouteUseCase calculateOptimalRoute;
    @Autowired
    private OptimizeDeliveriesRouteUseCase optimizeDeliveriesRoute;
    @Autowired
    private LocationPort locationPort;
    @Autowired
    private LocationResponseMapper responseMapper;

    /**
     * Geocodifica una dirección para obtener sus coordenadas.
     */
    @PostMapping("/geocode")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<LocationResponse> geocodeAddress(
            @Valid @RequestBody GeocodeRequest request) {
        Location location = geocodeDealership.geocodeAddress(request.getAddress());
        String formattedAddress = locationPort.reverseGeocode(location);
        return ResponseEntity.ok(responseMapper.toLocationResponse(location, formattedAddress));
    }

    /**
     * Calcula la ruta óptima visitando múltiples concesionarios desde un origen.
     */
    @PostMapping("/route")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<RouteResponse> calculateRoute(
            @Valid @RequestBody RouteRequest request) {
        Route route = calculateOptimalRoute.execute(
                request.getOriginLatitude(),
                request.getOriginLongitude(),
                request.getDealershipIds());
        return ResponseEntity.ok(responseMapper.toRouteResponse(route));
    }

    /**
     * Calcula la ruta y secuencia óptima de paradas para múltiples entregas
     * con orígenes y destinos diferentes.
     */
    @PostMapping("/route/optimize-services")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<OptimizeDeliveriesResponse> optimizeServicesRoute(
            @Valid @RequestBody OptimizeDeliveriesRequest request) {
        org.slf4j.LoggerFactory.getLogger(LocationController.class).info(
                "Solicitud de optimización de rutas recibida para {} servicios", request.getServiceUuids().size());
        DeliveryRoute deliveryRoute = optimizeDeliveriesRoute.execute(
                request.getCurrentLatitude(),
                request.getCurrentLongitude(),
                request.getServiceUuids());
        return ResponseEntity.ok(responseMapper.toOptimizeDeliveriesResponse(deliveryRoute));
    }

    /**
     * Calcula la distancia y tiempo estimado entre dos puntos geográficos.
     */
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
        Long durationSeconds = distanceMeters != null
                ? Math.round(distanceMeters / 11.11)
                : null;
        return ResponseEntity.ok(new DistanceResponse(distanceMeters, durationSeconds));
    }

    /**
     * Convierte coordenadas geográficas en una dirección legible (Geocodificación
     * inversa).
     */
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
