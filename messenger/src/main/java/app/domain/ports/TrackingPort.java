package app.domain.ports;

import app.domain.model.LiveTracking;
import app.domain.model.TrackingHistory;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

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
     * Consulta el historial de tracking de un mensajero en una fecha específica con
     * paginación.
     */
    Page<TrackingHistory> getHistoryByMessengerPaginated(Long messengerId,
            LocalDate date,
            Pageable pageable);

    /**
     * Consulta el historial de tracking asociado a un servicio específico.
     */
    java.util.List<TrackingHistory> getHistoryByService(Long serviceDeliveryId);

    /**
     * Obtiene el nombre del mensajero desde la caché o BD.
     */
    java.util.Optional<String> getMessengerName(Long messengerId);

    /**
     * Guarda el nombre del mensajero en la caché.
     */
    void saveMessengerName(Long messengerId, String name);
}
