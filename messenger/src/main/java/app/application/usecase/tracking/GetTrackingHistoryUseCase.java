package app.application.usecase.tracking;

import app.domain.model.TrackingHistory;
import app.domain.ports.TrackingPort;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.time.LocalDate;
import java.util.List;

/**
 * Caso de uso para consultar historial de tracking de mensajeros.
 */
@Service
public class GetTrackingHistoryUseCase {

    @Autowired
    private TrackingPort trackingPort;

    /**
     * Consulta el historial de un mensajero por fecha.
     */
    public List<TrackingHistory> byMessengerAndDate(Long messengerId, LocalDate date) {
        return trackingPort.getHistoryByMessenger(messengerId, date);
    }

    /**
     * Consulta el historial asociado a un servicio.
     */
    public List<TrackingHistory> byService(Long serviceDeliveryId) {
        return trackingPort.getHistoryByService(serviceDeliveryId);
    }
}
