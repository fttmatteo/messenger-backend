package app.adapter.out.tracking;

import app.domain.model.LiveTracking;
import app.domain.model.Location;
import app.domain.model.TrackingHistory;
import app.domain.model.enums.TrackingSource;
import app.infrastructure.persistence.entities.TrackingHistoryEntity;
import org.springframework.stereotype.Component;
import java.time.LocalDateTime;

@Component
public class TrackingMapper {

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
