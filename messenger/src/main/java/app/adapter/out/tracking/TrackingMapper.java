package app.adapter.out.tracking;

import app.domain.model.LiveTracking;
import app.domain.model.Location;
import app.domain.model.TrackingHistory;
import app.domain.model.enums.TrackingSource;
import app.infrastructure.persistence.entities.TrackingHistoryEntity;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

/**
 * Mapper para convertir entre modelos de dominio y entidades de tracking.
 * 
 * Esta clase maneja las conversiones bidireccionales entre:
 * - TrackingHistory (dominio) y TrackingHistoryEntity (persistencia JPA)
 * - LiveTracking (tiempo real) a TrackingHistory (para guardar en historial)
 * 
 * Responsabilidades:
 * - Mapear coordenadas entre Location y campos latitude/longitude
 * - Preservar información de timestamp, velocidad y fuente
 * - Manejar conversiones nulas de forma segura
 * 
 * @see TrackingAdapter
 * @see app.domain.model.TrackingHistory
 * @see app.domain.model.LiveTracking
 * @see app.infrastructure.persistence.entities.TrackingHistoryEntity
 */
@Component
public class TrackingMapper {

    /**
     * Convierte TrackingHistory de dominio a TrackingHistoryEntity para
     * persistencia.
     * 
     * Extrae las coordenadas del objeto Location y las mapea a campos separados
     * de latitud y longitud en la entidad.
     * 
     * @param history Objeto de dominio
     * @return Entidad JPA para persistir, o null si history es null
     */
    public TrackingHistoryEntity toEntity(TrackingHistory history) {
        if (history == null) {
            return null;
        }

        TrackingHistoryEntity entity = new TrackingHistoryEntity();
        entity.setHistoryId(history.getHistoryId());
        entity.setMessengerId(history.getMessengerId());
        entity.setRecordedAt(history.getRecordedAt());
        entity.setServiceDeliveryId(history.getServiceDeliveryId());
        entity.setSource(history.getSource());
        entity.setSpeed(history.getSpeed());

        if (history.getLocation() != null) {
            entity.setLatitude(history.getLocation().getLatitude());
            entity.setLongitude(history.getLocation().getLongitude());
        }

        return entity;
    }

    /**
     * Convierte TrackingHistoryEntity de persistencia a TrackingHistory de dominio.
     * 
     * Reconstruye el objeto Location a partir de los campos separados de
     * latitud y longitud de la entidad.
     * 
     * @param entity Entidad JPA
     * @return Objeto de dominio, o null si entity es null
     */
    public TrackingHistory toDomain(TrackingHistoryEntity entity) {
        if (entity == null) {
            return null;
        }

        TrackingHistory history = new TrackingHistory();
        history.setHistoryId(entity.getHistoryId());
        history.setMessengerId(entity.getMessengerId());
        history.setRecordedAt(entity.getRecordedAt());
        history.setServiceDeliveryId(entity.getServiceDeliveryId());
        history.setSource(entity.getSource());
        history.setSpeed(entity.getSpeed());

        if (entity.getLatitude() != null && entity.getLongitude() != null) {
            history.setLocation(new Location(
                    entity.getLatitude(),
                    entity.getLongitude(),
                    entity.getRecordedAt(),
                    null));
        }

        return history;
    }

    /**
     * Convierte LiveTracking a TrackingHistory para guardar en el historial.
     * 
     * Toma un punto de tracking en tiempo real y lo convierte a formato de
     * historial
     * para persistencia permanente. Asigna timestamp actual y fuente de datos.
     * 
     * @param live   Tracking en tiempo real
     * @param source Fuente de los datos (GPS, MANUAL, WEBSOCKET)
     * @return Objeto TrackingHistory listo para persistir
     */
    public TrackingHistory liveToHistory(LiveTracking live, TrackingSource source) {
        if (live == null) {
            return null;
        }

        TrackingHistory history = new TrackingHistory();
        history.setMessengerId(live.getMessengerId());
        history.setLocation(live.getCurrentLocation());
        history.setRecordedAt(LocalDateTime.now());
        history.setSpeed(live.getSpeed());
        history.setSource(source != null ? source : TrackingSource.GPS);

        return history;
    }
}
