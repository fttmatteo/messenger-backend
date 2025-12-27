package app.domain.ports;

import app.domain.model.LiveTracking;
import app.domain.model.TrackingHistory;
import java.time.LocalDate;
import java.util.List;

import java.util.Optional;

/**
 * Puerto de salida para rastreo GPS de mensajeros en tiempo real.
 */
public interface TrackingPort {

    /**
     * Actualiza la ubicación en tiempo real de un mensajero.
     */
    void saveLiveLocation(LiveTracking tracking);

    /**
     * Obtiene la última ubicación conocida de un mensajero.
     */
    Optional<LiveTracking> getLastLocation(Long messengerId);

    /**
     * Obtiene la ubicación actual de todos los mensajeros activos.
     */
    List<LiveTracking> getAllActiveMessengers();

    /**
     * Guarda un registro en el historial de ubicaciones persistente.
     */
    TrackingHistory saveTrackingHistory(TrackingHistory history);

    /**
     * Consulta el historial de ubicaciones de un mensajero en una fecha específica.
     */
    List<TrackingHistory> getHistoryByMessenger(Long messengerId, LocalDate date);

    /**
     * Consulta el historial de ubicaciones asociado a un servicio de entrega.
     */
    List<TrackingHistory> getHistoryByService(Long serviceDeliveryId);
}
