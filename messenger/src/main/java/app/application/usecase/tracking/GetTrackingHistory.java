package app.application.usecase.tracking;

import app.domain.model.TrackingHistory;
import app.domain.ports.TrackingPort;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.time.LocalDate;
import java.util.List;

@Service
public class GetTrackingHistory {

    @Autowired
    private TrackingPort trackingPort;

    public List<TrackingHistory> byMessengerAndDate(Long messengerId, LocalDate date) {
        return trackingPort.getHistoryByMessenger(messengerId, date);
    }

    public List<TrackingHistory> byService(Long serviceDeliveryId) {
        return trackingPort.getHistoryByService(serviceDeliveryId);
    }
}
