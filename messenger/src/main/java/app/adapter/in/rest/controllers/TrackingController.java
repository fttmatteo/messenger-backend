package app.adapter.in.rest.controllers;

import app.adapter.in.rest.request.LiveTrackingRequest;
import app.adapter.in.rest.response.LiveTrackingResponse;
import app.adapter.in.rest.response.TrackingHistoryResponse;
import app.application.usecase.tracking.GetTrackingHistory;
import app.application.usecase.tracking.UpdateLiveTracking;
import app.domain.model.LiveTracking;
import app.domain.model.TrackingHistory;
import app.domain.ports.TrackingPort;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Controller REST para operaciones de tracking de mensajeros.
 */
@RestController
@RequestMapping("/api/tracking")
public class TrackingController {

    @Autowired
    private UpdateLiveTracking updateLiveTracking;
    @Autowired
    private GetTrackingHistory getTrackingHistory;
    @Autowired
    private TrackingPort trackingPort;

    /**
     * Actualiza la ubicación de un mensajero.
     * POST /api/tracking/update
     * Solo mensajeros pueden actualizar su ubicación.
     */
    @PostMapping("/update")
    @PreAuthorize("hasAnyRole('MESSENGER', 'ADMIN')")
    public ResponseEntity<LiveTrackingResponse> updateLocation(
            @Valid @RequestBody LiveTrackingRequest request) {

        LiveTracking tracking = updateLiveTracking.execute(request);
        return ResponseEntity.ok(mapToResponse(tracking));
    }

    /**
     * Obtiene la última ubicación de un mensajero.
     * GET /api/tracking/messenger/{messengerId}
     * Solo admins pueden ver ubicaciones de otros mensajeros.
     */
    @GetMapping("/messenger/{messengerId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<LiveTrackingResponse> getLastLocation(
            @PathVariable Long messengerId) {

        LiveTracking tracking = trackingPort.getLastLocation(messengerId);

        if (tracking == null) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok(mapToResponse(tracking));
    }

    /**
     * Obtiene todos los mensajeros activos con su ubicación actual.
     * GET /api/tracking/active
     * Solo admins pueden ver esta información.
     */
    @GetMapping("/active")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<LiveTrackingResponse>> getAllActive() {
        List<LiveTracking> activeMessengers = trackingPort.getAllActiveMessengers();

        List<LiveTrackingResponse> response = activeMessengers.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());

        return ResponseEntity.ok(response);
    }

    /**
     * Obtiene el historial de ubicaciones de un mensajero para una fecha.
     * GET /api/tracking/history/{messengerId}?date=2025-12-10
     */
    @GetMapping("/history/{messengerId}")
    @PreAuthorize("hasAnyRole('MESSENGER', 'ADMIN')")
    public ResponseEntity<List<TrackingHistoryResponse>> getHistory(
            @PathVariable Long messengerId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {

        List<TrackingHistory> history = getTrackingHistory.byMessengerAndDate(messengerId, date);

        List<TrackingHistoryResponse> response = history.stream()
                .map(this::mapToHistoryResponse)
                .collect(Collectors.toList());

        return ResponseEntity.ok(response);
    }

    /**
     * Obtiene el historial de ubicaciones de un servicio de entrega.
     * GET /api/tracking/service/{serviceId}
     */
    @GetMapping("/service/{serviceId}")
    @PreAuthorize("hasAnyRole('MESSENGER', 'ADMIN')")
    public ResponseEntity<List<TrackingHistoryResponse>> getHistoryByService(
            @PathVariable Long serviceId) {

        List<TrackingHistory> history = getTrackingHistory.byService(serviceId);

        List<TrackingHistoryResponse> response = history.stream()
                .map(this::mapToHistoryResponse)
                .collect(Collectors.toList());

        return ResponseEntity.ok(response);
    }

    private LiveTrackingResponse mapToResponse(LiveTracking tracking) {
        return new LiveTrackingResponse(
                tracking.getMessengerId(),
                tracking.getMessengerName(),
                tracking.getCurrentLocation() != null ? tracking.getCurrentLocation().getLatitude() : null,
                tracking.getCurrentLocation() != null ? tracking.getCurrentLocation().getLongitude() : null,
                tracking.getLastUpdate(),
                tracking.getStatus(),
                tracking.getSpeed(),
                tracking.getHeading());
    }

    private TrackingHistoryResponse mapToHistoryResponse(TrackingHistory history) {
        TrackingHistoryResponse response = new TrackingHistoryResponse();
        response.setHistoryId(history.getHistoryId());
        response.setMessengerId(history.getMessengerId());
        response.setLatitude(history.getLocation() != null ? history.getLocation().getLatitude() : null);
        response.setLongitude(history.getLocation() != null ? history.getLocation().getLongitude() : null);
        response.setRecordedAt(history.getRecordedAt());
        response.setServiceDeliveryId(history.getServiceDeliveryId());
        response.setSource(history.getSource() != null ? history.getSource().name() : null);
        response.setSpeed(history.getSpeed());
        return response;
    }
}
