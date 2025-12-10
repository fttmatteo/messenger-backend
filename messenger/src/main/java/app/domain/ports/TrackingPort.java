package app.domain.ports;

import app.domain.model.LiveTracking;
import app.domain.model.TrackingHistory;
import java.time.LocalDate;
import java.util.List;

/**
 * Puerto para operaciones de tracking de mensajeros.
 * Combina Redis (tiempo real) y JPA (historial).
 */
public interface TrackingPort {

    /**
     * Guarda la ubicación actual de un mensajero en Redis.
     * Esta ubicación expira automáticamente después de X minutos.
     *
     * @param tracking Información de tracking actual
     */
    void saveLiveLocation(LiveTracking tracking);

    /**
     * Obtiene la última ubicación conocida de un mensajero.
     *
     * @param messengerId ID del mensajero
     * @return LiveTracking o null si no hay ubicación reciente
     */
    LiveTracking getLastLocation(Long messengerId);

    /**
     * Obtiene todos los mensajeros con tracking activo.
     *
     * @return Lista de LiveTracking de mensajeros activos
     */
    List<LiveTracking> getAllActiveMessengers();

    /**
     * Guarda una ubicación en el historial (base de datos).
     *
     * @param history Registro de tracking a guardar
     * @return TrackingHistory guardado con ID
     */
    TrackingHistory saveTrackingHistory(TrackingHistory history);

    /**
     * Obtiene el historial de ubicaciones de un mensajero en una fecha específica.
     *
     * @param messengerId ID del mensajero
     * @param date        Fecha a consultar
     * @return Lista de registros de tracking
     */
    List<TrackingHistory> getHistoryByMessenger(Long messengerId, LocalDate date);

    /**
     * Obtiene el historial de ubicaciones asociadas a un servicio de entrega.
     *
     * @param serviceDeliveryId ID del servicio
     * @return Lista de registros de tracking
     */
    List<TrackingHistory> getHistoryByService(Long serviceDeliveryId);
}
