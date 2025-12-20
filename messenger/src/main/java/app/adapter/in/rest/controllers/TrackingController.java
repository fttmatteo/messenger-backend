package app.adapter.in.rest.controllers;

import app.adapter.in.rest.request.LiveTrackingRequest;
import app.adapter.in.rest.response.LiveTrackingResponse;
import app.adapter.in.rest.response.TrackingHistoryResponse;
import app.adapter.in.rest.mapper.TrackingResponseMapper;
import app.application.usecase.tracking.GetTrackingHistory;
import app.application.usecase.tracking.UpdateLiveTracking;
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

/**
 * Controlador REST para tracking en tiempo real de mensajeros.
 */
@RestController
@RequestMapping("/tracking")
@PreAuthorize("isAuthenticated()")
public class TrackingController {

    @Autowired
    private UpdateLiveTracking updateLiveTracking;
    @Autowired
    private GetTrackingHistory getTrackingHistory;
    @Autowired
    private TrackingPort trackingPort;
    @Autowired
    private TrackingResponseMapper responseMapper;

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

    @GetMapping("/messenger/{messengerId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<LiveTrackingResponse> getLastLocation(
            @PathVariable Long messengerId) {

        LiveTracking tracking = trackingPort.getLastLocation(messengerId);

        if (tracking == null) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok(responseMapper.toResponse(tracking));
    }

    @GetMapping("/active")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<LiveTrackingResponse>> getAllActive() {
        List<LiveTracking> activeMessengers = trackingPort.getAllActiveMessengers();
        List<LiveTrackingResponse> response = activeMessengers.stream()
                .map(responseMapper::toResponse)
                .collect(Collectors.toList());

        return ResponseEntity.ok(response);
    }

    @GetMapping("/history/{messengerId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<TrackingHistoryResponse>> getHistory(
            @PathVariable Long messengerId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {

        List<TrackingHistory> history = getTrackingHistory.byMessengerAndDate(messengerId, date);
        List<TrackingHistoryResponse> response = history.stream()
                .map(responseMapper::toHistoryResponse)
                .collect(Collectors.toList());

        return ResponseEntity.ok(response);
    }

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
