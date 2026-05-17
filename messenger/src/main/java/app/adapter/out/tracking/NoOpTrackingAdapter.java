package app.adapter.out.tracking;

import app.domain.model.LiveTracking;
import app.domain.model.TrackingHistory;
import app.domain.ports.TrackingPort;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * Implementación simulada de TrackingPort para entornos de prueba sin Redis.
 * Se utiliza cuando redis.enabled=false.
 */

@Component
@ConditionalOnProperty(name = "redis.enabled", havingValue = "false")
public class NoOpTrackingAdapter implements TrackingPort {

    @Override
    public void saveLiveLocation(LiveTracking tracking) {
    }

    @Override
    public Optional<LiveTracking> getLastLocation(Long messengerId) {
        return Optional.empty();
    }

    @Override
    public List<LiveTracking> getAllActiveMessengers() {
        return new ArrayList<>();
    }

    @Override
    public TrackingHistory saveTrackingHistory(TrackingHistory history) {
        return history;
    }

    @Override
    public Page<TrackingHistory> getHistoryByMessengerPaginated(Long messengerId, LocalDate date, Pageable pageable) {
        return Page.empty();
    }

    @Override
    public List<TrackingHistory> getHistoryByService(Long serviceDeliveryId) {
        return new ArrayList<>();
    }

    @Override
    public Optional<String> getMessengerName(Long messengerId) {
        return Optional.empty();
    }

    @Override
    public void saveMessengerName(Long messengerId, String name) {
        // No-op
    }
}
