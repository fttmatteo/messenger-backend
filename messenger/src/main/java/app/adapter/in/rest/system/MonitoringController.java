package app.adapter.in.rest.system;

import app.application.usecase.MonitoringUseCase;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

/**
 * Controlador REST para datos del panel de monitoreo.
 */
@RestController
@RequestMapping("/monitoring")
@PreAuthorize("hasRole('ADMIN')")
public class MonitoringController {

    @Autowired
    private MonitoringUseCase monitoringUseCase;

    /**
     * Obtiene la actividad de un mensajero para una fecha específica.
     * Devuelve estadísticas del día y línea de tiempo de eventos.
     */
    @GetMapping("/messenger/{messengerUuid}/activity")
    public ResponseEntity<MessengerActivityResponse> getMessengerActivity(
            @PathVariable String messengerUuid,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "100") int size) {

        Pageable pageable = PageRequest.of(page, size);
        MessengerActivityResponse response = monitoringUseCase.getDailyActivity(messengerUuid, date, pageable);

        return ResponseEntity.ok(response);
    }
}
