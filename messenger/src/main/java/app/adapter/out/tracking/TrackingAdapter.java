package app.adapter.out.tracking;

import app.domain.model.LiveTracking;
import app.domain.model.TrackingHistory;
import app.domain.model.enums.TrackingStatus;
import app.domain.ports.TrackingPort;
import app.infrastructure.persistence.entities.TrackingHistoryEntity;
import app.infrastructure.persistence.repository.TrackingHistoryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

/**
 * Adaptador que implementa TrackingPort usando Redis + JPA.
 * - Redis: Ubicaciones en tiempo real con TTL automático
 * - JPA: Historial de ubicaciones para reportes
 */
@Component
public class TrackingAdapter implements TrackingPort {

    private static final String TRACKING_KEY_PREFIX = "tracking:messenger:";
    private static final long TRACKING_TTL_MINUTES = 5; // Expira después de 5 minutos sin actualizar

    private final RedisTemplate<String, LiveTracking> redisTemplate;
    private final TrackingHistoryRepository historyRepository;
    private final TrackingMapper mapper;

    @Autowired
    public TrackingAdapter(
            RedisTemplate<String, LiveTracking> redisTemplate,
            TrackingHistoryRepository historyRepository,
            TrackingMapper mapper) {
        this.redisTemplate = redisTemplate;
        this.historyRepository = historyRepository;
        this.mapper = mapper;
    }

    @Override
    public void saveLiveLocation(LiveTracking tracking) {
        if (tracking == null || tracking.getMessengerId() == null) {
            return;
        }

        String key = TRACKING_KEY_PREFIX + tracking.getMessengerId();
        tracking.setLastUpdate(LocalDateTime.now());

        // Guardar en Redis con TTL
        redisTemplate.opsForValue().set(key, tracking, TRACKING_TTL_MINUTES, TimeUnit.MINUTES);
    }

    @Override
    public LiveTracking getLastLocation(Long messengerId) {
        if (messengerId == null) {
            return null;
        }

        String key = TRACKING_KEY_PREFIX + messengerId;
        return redisTemplate.opsForValue().get(key);
    }

    @Override
    public List<LiveTracking> getAllActiveMessengers() {
        Set<String> keys = redisTemplate.keys(TRACKING_KEY_PREFIX + "*");

        if (keys == null || keys.isEmpty()) {
            return new ArrayList<>();
        }

        List<LiveTracking> activeMessengers = new ArrayList<>();
        for (String key : keys) {
            LiveTracking tracking = redisTemplate.opsForValue().get(key);
            if (tracking != null && tracking.getStatus() == TrackingStatus.ACTIVE) {
                activeMessengers.add(tracking);
            }
        }

        return activeMessengers;
    }

    @Override
    public TrackingHistory saveTrackingHistory(TrackingHistory history) {
        if (history == null) {
            return null;
        }

        TrackingHistoryEntity entity = mapper.toEntity(history);
        TrackingHistoryEntity saved = historyRepository.save(entity);
        return mapper.toDomain(saved);
    }

    @Override
    public List<TrackingHistory> getHistoryByMessenger(Long messengerId, LocalDate date) {
        if (messengerId == null || date == null) {
            return new ArrayList<>();
        }

        LocalDateTime startOfDay = date.atStartOfDay();
        LocalDateTime endOfDay = date.atTime(LocalTime.MAX);

        List<TrackingHistoryEntity> entities = historyRepository
                .findByMessengerIdAndRecordedAtBetween(messengerId, startOfDay, endOfDay);

        return entities.stream()
                .map(mapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public List<TrackingHistory> getHistoryByService(Long serviceDeliveryId) {
        if (serviceDeliveryId == null) {
            return new ArrayList<>();
        }

        List<TrackingHistoryEntity> entities = historyRepository
                .findByServiceDeliveryId(serviceDeliveryId);

        return entities.stream()
                .map(mapper::toDomain)
                .collect(Collectors.toList());
    }
}
