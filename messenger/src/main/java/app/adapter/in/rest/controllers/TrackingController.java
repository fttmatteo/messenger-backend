package app.adapter.in.rest.controllers;

import app.adapter.in.rest.request.LiveTrackingRequest;
import app.adapter.in.rest.response.LiveTrackingResponse;
import app.adapter.in.rest.response.TrackingHistoryResponse;
import app.adapter.in.rest.mapper.TrackingResponseMapper;
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
 * Controlador REST para tracking en tiempo real de mensajeros.
 *
 * Proporciona endpoints para actualizar ubicaciones en tiempo real
 * y consultar historial de tracking.
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
    @Autowired
    private TrackingResponseMapper responseMapper;

    /**
     * Actualiza la ubicación en tiempo real de un mensajero.
     *
     * Solo los propios mensajeros o administradores pueden realizar esta acción.
     * La ubicación se utiliza para el seguimiento en vivo y se archiva en el
     * histórico.
     *
     * @param request Objeto con los datos de telemetría (latitud, longitud,
     *                velocidad, etc).
     * @return ResponseEntity con la confirmación de la actualización y datos
     *         procesados.
     */
    @PostMapping("/update")
    @PreAuthorize("hasAnyRole('MESSENGER', 'ADMIN')")
    public ResponseEntity<LiveTrackingResponse> updateLocation(
            @Valid @RequestBody LiveTrackingRequest request) {

        // Mapeo manual de DTO a Dominio aquí en el controlador (Adaptador)
        LiveTracking domainTracking = new LiveTracking();
        domainTracking.setMessengerId(request.getMessengerId());

        // Crear Location value object
        app.domain.model.Location location = new app.domain.model.Location(
                request.getLatitude(),
                request.getLongitude(),
                java.time.LocalDateTime.now(),
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
     * Obtiene la última ubicación reportada de un mensajero específico.
     *
     * Uso exclusivo para administradores.
     *
     * @param messengerId ID del mensajero.
     * @return ResponseEntity con la última ubicación conocida o 404 si no hay
     *         datos.
     */
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

    /**
     * Obtiene la lista de todos los mensajeros activos con su ubicación actual.
     *
     * Permite a los administradores visualizar toda la flota en tiempo real.
     *
     * @return Lista de tracking en vivo de mensajeros activos.
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
     * Consulta el historial de recorrido de un mensajero en una fecha específica.
     *
     * @param messengerId ID del mensajero.
     * @param date        Fecha de consulta (Formato ISO DATE, ej: 2025-12-10).
     * @return Lista de puntos históricos registrados.
     */
    @GetMapping("/history/{messengerId}")
    @PreAuthorize("hasAnyRole('MESSENGER', 'ADMIN')")
    public ResponseEntity<List<TrackingHistoryResponse>> getHistory(
            @PathVariable Long messengerId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {

        List<TrackingHistory> history = getTrackingHistory.byMessengerAndDate(messengerId, date);

        List<TrackingHistoryResponse> response = history.stream()
                .map(responseMapper::toHistoryResponse)
                .collect(Collectors.toList());

        return ResponseEntity.ok(response);
    }

    /**
     * Consulta el historial de recorrido asociado a un servicio de entrega
     * específico.
     *
     * @param serviceId ID del servicio de entrega.
     * @return Lista de puntos históricos registrados durante el servicio.
     */
    @GetMapping("/service/{serviceId}")
    @PreAuthorize("hasAnyRole('MESSENGER', 'ADMIN')")
    public ResponseEntity<List<TrackingHistoryResponse>> getHistoryByService(
            @PathVariable Long serviceId) {

        List<TrackingHistory> history = getTrackingHistory.byService(serviceId);

        List<TrackingHistoryResponse> response = history.stream()
                .map(responseMapper::toHistoryResponse)
                .collect(Collectors.toList());

        return ResponseEntity.ok(response);
    }
}
