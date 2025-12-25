package app.adapter.out.tracking;

import app.domain.model.LiveTracking;
import app.domain.model.TrackingHistory;
import app.domain.ports.TrackingPort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Mock implementation of TrackingPort for test environments without Redis.
 * This is used when redis.enabled=false.
 */
@Component
@ConditionalOnProperty(name = "redis.enabled", havingValue = "false")
public class NoOpTrackingAdapter implements TrackingPort {

    private static final Logger logger = LoggerFactory.getLogger(NoOpTrackingAdapter.class);

    @Override
    public void saveLiveLocation(LiveTracking tracking) {
        logger.debug("NoOp: saveLiveLocation called (Redis disabled)");
        // No-op: Redis is disabled in test environment
    }

    @Override
    public LiveTracking getLastLocation(Long messengerId) {
        logger.debug("NoOp: getLastLocation called for messenger {} (Redis disabled)", messengerId);
        return null; // No live tracking data without Redis
    }

    @Override
    public List<LiveTracking> getAllActiveMessengers() {
        logger.debug("NoOp: getAllActiveMessengers called (Redis disabled)");
        return new ArrayList<>(); // Empty list when Redis is disabled
    }

    @Override
    public TrackingHistory saveTrackingHistory(TrackingHistory history) {
        logger.debug("NoOp: saveTrackingHistory called (Redis disabled)");
        return history; // Pass-through without persistence
    }

    @Override
    public List<TrackingHistory> getHistoryByMessenger(Long messengerId, LocalDate date) {
        logger.debug("NoOp: getHistoryByMessenger called (Redis disabled)");
        return new ArrayList<>();
    }

    @Override
    public List<TrackingHistory> getHistoryByService(Long serviceDeliveryId) {
        logger.debug("NoOp: getHistoryByService called (Redis disabled)");
        return new ArrayList<>();
    }
}
