package app.adapter.in.rest.controllers;

import app.adapter.in.rest.request.LiveTrackingRequest;
import app.adapter.in.rest.response.LiveTrackingResponse;
import app.adapter.in.rest.response.TrackingHistoryResponse;
import app.adapter.in.rest.mapper.TrackingResponseMapper;
import app.application.usecase.tracking.GetTrackingHistoryUseCase;
import app.application.usecase.tracking.UpdateLiveTrackingUseCase;
import app.domain.model.LiveTracking;
import app.domain.model.Location;
import app.domain.model.TrackingHistory;
import app.domain.ports.TrackingPort;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Controlador REST para tracking en tiempo real de mensajeros.
 */
@RestController
@RequestMapping("/tracking")
@PreAuthorize("isAuthenticated()")
public class TrackingController {

    private static final Logger logger = LoggerFactory.getLogger(TrackingController.class);

    @Autowired
    private UpdateLiveTrackingUseCase updateLiveTracking;
    @Autowired
    private GetTrackingHistoryUseCase getTrackingHistory;
    @Autowired
    private TrackingPort trackingPort;
    @Autowired
    private TrackingResponseMapper responseMapper;

    /**
     * Actualiza la ubicación en tiempo real de un mensajero.
     */
    @PostMapping("/update")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<LiveTrackingResponse> updateLocation(
            @Valid @RequestBody LiveTrackingRequest request) {

        LiveTracking domainTracking = new LiveTracking();
        domainTracking.setMessengerId(request.getMessengerId());

        Location location = new Location(
                request.getLatitude(),
                request.getLongitude(),
                LocalDateTime.now(),
                request.getAccuracy());
        domainTracking.setCurrentLocation(location);
        domainTracking.setSpeed(request.getSpeed());
        domainTracking.setHeading(request.getHeading());
        domainTracking.setDeviceId(request.getDeviceId());
        if (request.getStatus() != null) {
            domainTracking.setStatus(request.getStatus());
        }

        LiveTracking tracking = updateLiveTracking.execute(domainTracking);
        return ResponseEntity.ok(responseMapper.toResponse(tracking));
    }

    /**
     * Obtiene la última ubicación conocida de un mensajero (solo ADMIN).
     */
    @GetMapping("/messenger/{messengerId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<LiveTrackingResponse> getLastLocation(
            @PathVariable Long messengerId) {

        logger.debug("Solicitud última ubicación mensajero ID: {}", messengerId);
        LiveTracking tracking = trackingPort.getLastLocation(messengerId).orElse(null);

        if (tracking == null) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok(responseMapper.toResponse(tracking));
    }

    /**
     * Obtiene la ubicación de todos los mensajeros activos (solo ADMIN).
     */
    @GetMapping("/active")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<LiveTrackingResponse>> getAllActive() {
        List<LiveTracking> activeMessengers = trackingPort.getAllActiveMessengers();
        List<LiveTrackingResponse> response = activeMessengers.stream()
                .map(responseMapper::toResponse)
                .collect(Collectors.toList());

        return ResponseEntity.ok(response);
    }

    /**
     * Obtiene el historial de ubicaciones de un mensajero en una fecha específica.
     */
    @GetMapping("/history/{messengerId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<TrackingHistoryResponse>> getHistory(
            @PathVariable Long messengerId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {

        logger.info("Solicitud historial tracking mensajero ID: {}, fecha: {}", messengerId, date);
        List<TrackingHistory> history = getTrackingHistory.byMessengerAndDate(messengerId, date);
        List<TrackingHistoryResponse> response = history.stream()
                .map(responseMapper::toHistoryResponse)
                .collect(Collectors.toList());

        return ResponseEntity.ok(response);
    }

    /**
     * Obtiene el historial de rastreo asociado a un servicio específico.
     */
    @GetMapping("/service/{serviceId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<TrackingHistoryResponse>> getHistoryByService(
            @PathVariable Long serviceId) {

        List<TrackingHistory> history = getTrackingHistory.byService(serviceId);
        List<TrackingHistoryResponse> response = history.stream()
                .map(responseMapper::toHistoryResponse)
                .collect(Collectors.toList());

        return ResponseEntity.ok(response);
    }
}
