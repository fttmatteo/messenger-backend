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
    void saveLiveLocation(LiveTracking tracking);

    Optional<LiveTracking> getLastLocation(Long messengerId);

    List<LiveTracking> getAllActiveMessengers();

    TrackingHistory saveTrackingHistory(TrackingHistory history);

    Page<TrackingHistory> getHistoryByMessengerPaginated(Long messengerId,
            LocalDate date,
            Pageable pageable);

    java.util.List<TrackingHistory> getHistoryByService(Long serviceDeliveryId);

    java.util.Optional<String> getMessengerName(Long messengerId);

    void saveMessengerName(Long messengerId, String name);
}
