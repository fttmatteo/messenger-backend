package app.adapter.in.rest.controllers;

import app.adapter.in.rest.response.MessengerActivityResponse;
import app.adapter.in.rest.response.MessengerActivityResponse.ActivityEvent;
import app.adapter.in.rest.response.MessengerActivityResponse.DailyStats;
import app.domain.model.ServiceDelivery;
import app.domain.model.StatusHistory;
import app.domain.model.enums.Status;
import app.domain.ports.ServiceDeliveryPort;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * Controlador REST para datos del panel de monitoreo.
 */
@RestController
@RequestMapping("/monitoring")
@PreAuthorize("hasRole('ADMIN')")
public class MonitoringController {

    @Autowired
    private ServiceDeliveryPort serviceDeliveryPort;

    /**
     * Obtiene la actividad de un mensajero para una fecha específica.
     * Devuelve estadísticas del día y línea de tiempo de eventos.
     */
    @GetMapping("/messenger/{messengerId}/activity")
    public ResponseEntity<MessengerActivityResponse> getMessengerActivity(
            @PathVariable Long messengerId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {

        // Get all services for this messenger
        List<ServiceDelivery> allServices = serviceDeliveryPort.findByMessengerId(messengerId);

        // Calculate daily stats based on CURRENT status of services created today
        int assigned = 0;
        int delivered = 0;
        int returned = 0;
        int canceled = 0;
        int total = 0;

        List<ActivityEvent> timeline = new ArrayList<>();

        for (ServiceDelivery service : allServices) {
            // Count services created on this date by their CURRENT status
            if (service.getCreatedAt() != null && service.getCreatedAt().toLocalDate().equals(date)) {
                total++;
                Status currentStatus = service.getCurrentStatus();
                if (currentStatus == Status.ASSIGNED) {
                    assigned++;
                } else if (currentStatus == Status.DELIVERED) {
                    delivered++;
                } else if (currentStatus == Status.RETURNED) {
                    returned++;
                } else if (currentStatus == Status.CANCELED) {
                    canceled++;
                }
            }

            // Process history events for timeline
            if (service.getHistory() != null) {
                for (StatusHistory history : service.getHistory()) {
                    if (history.getChangeDate() != null && history.getChangeDate().toLocalDate().equals(date)) {
                        // Add to timeline
                        ActivityEvent event = new ActivityEvent();
                        event.setId(history.getIdStatusHistory());
                        event.setStatus(history.getNewStatus().name());
                        event.setTimestamp(history.getChangeDate());
                        event.setPlateNumber(service.getPlate() != null ? service.getPlate().getPlateNumber() : null);
                        event.setDealershipName(
                                service.getDealership() != null ? service.getDealership().getName() : null);
                        event.setLatitude(history.getDeliveryLatitude());
                        event.setLongitude(history.getDeliveryLongitude());

                        if (history.getChangedBy() != null) {
                            event.setChangedByName(history.getChangedBy().getFullName());
                            event.setChangedByRole(history.getChangedBy().getRole() != null
                                    ? history.getChangedBy().getRole().name()
                                    : null);
                        }

                        timeline.add(event);
                    }
                }
            }
        }

        // Sort timeline by timestamp descending (newest first)
        timeline.sort(Comparator.comparing(ActivityEvent::getTimestamp).reversed());

        int pending = total - delivered - returned - canceled;
        if (pending < 0)
            pending = 0;

        DailyStats stats = new DailyStats(assigned, delivered, returned, canceled, pending, total);
        MessengerActivityResponse response = new MessengerActivityResponse(stats, timeline);

        return ResponseEntity.ok(response);
    }
}
