package app.domain.ports;

import app.domain.model.LiveTracking;
import app.domain.model.TrackingHistory;
import java.time.LocalDate;
import java.util.List;

public interface TrackingPort {

    void saveLiveLocation(LiveTracking tracking);

    LiveTracking getLastLocation(Long messengerId);

    List<LiveTracking> getAllActiveMessengers();

    TrackingHistory saveTrackingHistory(TrackingHistory history);

    List<TrackingHistory> getHistoryByMessenger(Long messengerId, LocalDate date);

    List<TrackingHistory> getHistoryByService(Long serviceDeliveryId);
}
