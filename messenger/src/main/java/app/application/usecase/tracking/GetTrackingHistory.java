package app.application.usecase.tracking;

import app.domain.model.TrackingHistory;
import app.domain.ports.TrackingPort;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

/**
 * Caso de uso para consultar historial de ubicaciones de un mensajero.
 */
@Service
public class GetTrackingHistory {

    @Autowired
    private TrackingPort trackingPort;

    /**
     * Obtiene el historial de ubicaciones de un mensajero en una fecha específica.
     */
    public List<TrackingHistory> byMessengerAndDate(Long messengerId, LocalDate date) {
        return trackingPort.getHistoryByMessenger(messengerId, date);
    }

    /**
     * Obtiene el historial de ubicaciones de un servicio de entrega.
     */
    public List<TrackingHistory> byService(Long serviceDeliveryId) {
        return trackingPort.getHistoryByService(serviceDeliveryId);
    }
}
