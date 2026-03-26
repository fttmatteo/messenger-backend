package app.adapter.in.rest.controllers;

import app.adapter.in.rest.request.LiveTrackingRequest;
import app.adapter.in.rest.response.LiveTrackingResponse;
import app.adapter.in.rest.response.TrackingHistoryResponse;
import app.adapter.in.rest.mapper.TrackingResponseMapper;
import app.adapter.out.tracking.config.RedisPubSubConfig;
import app.application.usecase.tracking.GetTrackingHistoryUseCase;
import app.application.usecase.tracking.UpdateLiveTrackingUseCase;
import app.domain.model.LiveTracking;
import app.domain.model.Location;
import app.domain.model.ServiceDelivery;
import app.domain.model.TrackingHistory;
import app.domain.ports.TrackingPort;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
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
import app.application.usecase.EmployeeUseCase;
import app.application.usecase.ServiceDeliveryUseCase;
import app.domain.model.Employee;

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
    @Autowired
    private StringRedisTemplate redisTemplate;
    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private EmployeeUseCase employeeUseCase;
    @Autowired
    private ServiceDeliveryUseCase serviceDeliveryUseCase;

    /**
     * Actualiza la ubicación en tiempo real de un mensajero.
     */
    @PostMapping("/update")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<LiveTrackingResponse> updateLocation(
            @Valid @RequestBody LiveTrackingRequest request) {

        if (request.getLatitude() == null || request.getLongitude() == null) {
            logger.warn("Intento de actualización de tracking con coordenadas nulas para messengerId: {}",
                    request.getMessengerId());
            return ResponseEntity.badRequest().build();
        }

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
        LiveTrackingResponse response = responseMapper.toResponse(tracking);

        // Publicar en Redis Pub/Sub para notificar al admin en tiempo real
        try {
            String jsonResponse = objectMapper.writeValueAsString(response);
            redisTemplate.convertAndSend(RedisPubSubConfig.TRACKING_TOPIC, jsonResponse);
        } catch (Exception e) {
            logger.warn("Error publicando tracking update en Redis Pub/Sub: {}", e.getMessage());
        }

        return ResponseEntity.ok(response);
    }

    /**
     * Obtiene la última ubicación conocida de un mensajero (solo ADMIN).
     * Retorna 200 con null si no hay datos (mensajero nuevo o inactivo).
     */
    @GetMapping("/messenger/{messengerUuid}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<LiveTrackingResponse> getLastLocation(
            @PathVariable String messengerUuid) {

        Employee messenger = employeeUseCase.findByUuid(messengerUuid);
        logger.debug("Solicitud última ubicación mensajero UUID: {}", messengerUuid);
        LiveTracking tracking = trackingPort.getLastLocation(messenger.getIdEmployee()).orElse(null);

        if (tracking == null) {
            return ResponseEntity.ok(null);
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
    @GetMapping("/history/{messengerUuid}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<TrackingHistoryResponse>> getHistory(
            @PathVariable String messengerUuid,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {

        Employee messenger = employeeUseCase.findByUuid(messengerUuid);
        List<TrackingHistory> history = getTrackingHistory.byMessengerAndDate(messenger.getIdEmployee(), date);
        List<TrackingHistoryResponse> response = history.stream()
                .map(responseMapper::toHistoryResponse)
                .collect(Collectors.toList());

        return ResponseEntity.ok(response);
    }

    /**
     * Obtiene el historial de rastreo asociado a un servicio específico.
     */
    @GetMapping("/service/{serviceUuid}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<TrackingHistoryResponse>> getHistoryByService(
            @PathVariable String serviceUuid) {

        ServiceDelivery service = null;
        try {
            service = serviceDeliveryUseCase.findByUuid(serviceUuid);
        } catch (Exception e) {
            throw new app.domain.exception.ResourceNotFoundException("Servicio con UUID " + serviceUuid + " no encontrado");
        }
        List<TrackingHistory> history = getTrackingHistory.byService(service.getIdServiceDelivery());
        List<TrackingHistoryResponse> response = history.stream()
                .map(responseMapper::toHistoryResponse)
                .collect(Collectors.toList());

        return ResponseEntity.ok(response);
    }
}
