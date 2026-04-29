package app.application.usecase.tracking;

import app.domain.model.TrackingHistory;
import app.domain.ports.TrackingPort;
import org.springframework.stereotype.Service;
import java.time.LocalDate;
import java.util.List;

/**
 * Caso de uso para consultar historial de tracking de mensajeros.
 */
@Service
public class GetTrackingHistoryUseCase {

    private final TrackingPort trackingPort;

    public GetTrackingHistoryUseCase(TrackingPort trackingPort) {
        this.trackingPort = trackingPort;
    }

    /**
     * Consulta el historial de un mensajero por fecha con paginación.
     */
    public org.springframework.data.domain.Page<TrackingHistory> byMessengerAndDatePaginated(Long messengerId, LocalDate date,
            org.springframework.data.domain.Pageable pageable) {
        return trackingPort.getHistoryByMessengerPaginated(messengerId, date, pageable);
    }

    /**
     * Consulta el historial asociado a un servicio.
     */
    public List<TrackingHistory> byService(Long serviceDeliveryId) {
        return trackingPort.getHistoryByService(serviceDeliveryId);
    }
}
