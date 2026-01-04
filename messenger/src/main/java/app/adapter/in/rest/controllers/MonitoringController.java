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
import java.time.LocalDateTime;
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

        List<ServiceDelivery> allServices = serviceDeliveryPort.findByMessengerId(messengerId);

        int assigned = 0;
        int delivered = 0;
        int returned = 0;
        int canceled = 0;
        int pendingCount = 0;
        int total = 0;

        List<ActivityEvent> timeline = new ArrayList<>();

        for (ServiceDelivery service : allServices) {
            boolean wasAssignedToday = false;

            if (service.getCreatedAt() != null && service.getCreatedAt().toLocalDate().equals(date)) {
                wasAssignedToday = true;
            }

            if (service.getHistory() != null) {
                Status latestStatusToday = null;
                LocalDateTime latestChangeDate = null;

                for (StatusHistory history : service.getHistory()) {
                    if (history.getChangeDate() != null && history.getChangeDate().toLocalDate().equals(date)) {
                        Status newStatus = history.getNewStatus();

                        // Track the latest status transition for this service today
                        if (latestChangeDate == null || history.getChangeDate().isAfter(latestChangeDate)) {
                            latestChangeDate = history.getChangeDate();
                            latestStatusToday = newStatus;
                        }

                        if (newStatus == Status.ASSIGNED) {
                            wasAssignedToday = true;
                        }

                        ActivityEvent event = new ActivityEvent();
                        event.setId(history.getIdStatusHistory());
                        event.setStatus(newStatus.name());
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

                if (wasAssignedToday) {
                    total++;
                }

                // Increment summary counters based ONLY on the last status reached today
                if (latestStatusToday != null) {
                    if (latestStatusToday == Status.DELIVERED) {
                        delivered++;
                    } else if (latestStatusToday == Status.RETURNED) {
                        returned++;
                    } else if (latestStatusToday == Status.CANCELED) {
                        canceled++;
                    } else if (latestStatusToday == Status.PENDING) {
                        pendingCount++;
                    }
                }
            } else if (wasAssignedToday) {
                total++;
            }
        }

        timeline.sort(Comparator.comparing(ActivityEvent::getTimestamp).reversed());

        int pending = pendingCount;

        DailyStats stats = new DailyStats(assigned, delivered, returned, canceled, pending, total);
        MessengerActivityResponse response = new MessengerActivityResponse(stats, timeline);

        return ResponseEntity.ok(response);
    }
}
