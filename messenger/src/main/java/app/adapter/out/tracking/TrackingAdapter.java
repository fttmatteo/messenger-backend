package app.adapter.out.tracking;

import app.domain.model.LiveTracking;
import app.domain.model.TrackingHistory;
import app.domain.model.Location;
import app.domain.model.enums.TrackingStatus;
import app.domain.ports.TrackingPort;
import app.infrastructure.persistence.entities.TrackingHistoryEntity;
import app.infrastructure.persistence.repository.TrackingHistoryRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * Adapter de tracking que usa Redis para ubicación en tiempo real.
 */
@Component
@ConditionalOnProperty(name = "redis.enabled", havingValue = "true", matchIfMissing = true)
public class TrackingAdapter implements TrackingPort {

    private static final Logger logger = LoggerFactory.getLogger(TrackingAdapter.class);

    private static final String TRACKING_KEY_PREFIX = "tracking:messenger:";
    private static final long TRACKING_TTL_SECONDS = 90;

    @Autowired
    @Qualifier("liveTrackingRedisTemplate")
    private RedisTemplate<String, LiveTracking> redisTemplate;
    @Autowired
    private org.springframework.data.redis.core.StringRedisTemplate stringRedisTemplate;
    @Autowired
    private TrackingHistoryRepository historyRepository;
    @Autowired
    private TrackingMapper mapper;

    /**
     * Guarda la ubicación en tiempo real en Redis con un TTL (tiempo de vida)
     * definido.
     */
    @Override
    public void saveLiveLocation(LiveTracking tracking) {
        if (tracking == null || tracking.getMessengerId() == null) {
            return;
        }

        try {
            String key = TRACKING_KEY_PREFIX + tracking.getMessengerId();
            if (tracking.getLastUpdate() == null) {
                tracking.setLastUpdate(LocalDateTime.now());
            }

            if (tracking.getStatus() == TrackingStatus.OFFLINE) {
                redisTemplate.delete(key);
                return;
            }

            redisTemplate.opsForValue().set(key, tracking, TRACKING_TTL_SECONDS, TimeUnit.SECONDS);
        } catch (Exception e) {
            logger.error("Error guardando tracking en Redis: {}", e.getMessage());
            throw new RuntimeException("Error al guardar en Redis: " + e.getMessage());
        }
    }

    /**
     * Obtiene la última ubicación almacenada en Redis para un mensajero.
     */
    @Override
    public Optional<LiveTracking> getLastLocation(Long messengerId) {
        if (messengerId == null) {
            return Optional.empty();
        }

        try {
            String key = TRACKING_KEY_PREFIX + messengerId;
            LiveTracking tracking = redisTemplate.opsForValue().get(key);
            if (tracking != null) {
                return Optional.of(tracking);
            }
        } catch (Exception e) {
            logger.warn("Error leyendo de Redis para mensajero {}: {}", messengerId, e.getMessage());
        }

        try {
            TrackingHistoryEntity lastEntity = historyRepository
                    .findFirstByMessengerIdOrderByRecordedAtDesc(messengerId);
            if (lastEntity != null) {
                LiveTracking historyTracking = new LiveTracking();
                historyTracking.setMessengerId(lastEntity.getMessengerId());
                Location loc = new Location(
                        lastEntity.getLatitude(),
                        lastEntity.getLongitude(),
                        lastEntity.getRecordedAt(),
                        null);
                historyTracking.setCurrentLocation(loc);

                historyTracking.setLastUpdate(lastEntity.getRecordedAt());
                historyTracking.setSpeed(lastEntity.getSpeed());
                historyTracking.setHeading(0.0);
                historyTracking.setStatus(TrackingStatus.OFFLINE);

                return Optional.of(historyTracking);
            }
        } catch (Exception e) {
            logger.error("Error buscando historial en DB para mensajero {}: {}", messengerId, e.getMessage());
        }

        return Optional.empty();
    }

    /**
     * Retorna una lista con todos los mensajeros que tienen datos activos en Redis.
     */
    @Override
    public List<LiveTracking> getAllActiveMessengers() {
        List<LiveTracking> activeMessengers = new ArrayList<>();
        try {
            Set<String> keys = redisTemplate.keys(TRACKING_KEY_PREFIX + "*");

            if (keys == null || keys.isEmpty()) {
                return activeMessengers;
            }

            for (String key : keys) {
                try {
                    LiveTracking tracking = redisTemplate.opsForValue().get(key);
                    if (tracking != null && tracking.getStatus() == TrackingStatus.ACTIVE) {
                        activeMessengers.add(tracking);
                    }
                } catch (Exception e) {
                    logger.warn("Error deserializando tracking para key {}: {}", key, e.getMessage());
                    throw new RuntimeException(
                            "Error al deserializar tracking para key " + key + ": " + e.getMessage());
                }
            }
        } catch (Exception e) {
            logger.error("Error crítico listando mensajeros activos desde Redis: {}", e.getMessage());
            throw new RuntimeException("Error crítico al listar mensajeros desde Redis: " + e.getMessage());
        }

        return activeMessengers;
    }

    /**
     * Persiste el historial de seguimiento en la base de datos relacional.
     */
    @Override
    public TrackingHistory saveTrackingHistory(TrackingHistory history) {
        if (history == null) {
            return null;
        }

        TrackingHistoryEntity entity = mapper.toEntity(history);
        TrackingHistoryEntity saved = historyRepository.save(entity);
        return mapper.toDomain(saved);
    }

    /**
     * Consulta el historial de un mensajero en una fecha específica desde la BD con paginación.
     */
    @Override
    public org.springframework.data.domain.Page<TrackingHistory> getHistoryByMessengerPaginated(Long messengerId, LocalDate date,
            org.springframework.data.domain.Pageable pageable) {
        if (messengerId == null || date == null) {
            return org.springframework.data.domain.Page.empty();
        }

        LocalDateTime startOfDay = date.atStartOfDay();
        LocalDateTime endOfDay = date.atTime(LocalTime.MAX);

        return historyRepository
                .findByMessengerIdAndRecordedAtBetween(messengerId, startOfDay, endOfDay, pageable)
                .map(mapper::toDomain);
    }

    /**
     * Consulta el historial asociado a un servicio de entrega específico.
     */
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

    @Override
    public Optional<String> getMessengerName(Long messengerId) {
        try {
            String key = "tracking:name:" + messengerId;
            String name = stringRedisTemplate.opsForValue().get(key);
            if (name != null) {
                return Optional.of(name);
            }
            return getLastLocation(messengerId).map(LiveTracking::getMessengerName)
                    .filter(n -> n != null && !n.isEmpty());
        } catch (Exception e) {
            return Optional.empty();
        }
    }

    @Override
    public void saveMessengerName(Long messengerId, String name) {
        if (messengerId == null || name == null || name.isEmpty())
            return;
        try {
            String key = "tracking:name:" + messengerId;
            stringRedisTemplate.opsForValue().set(key, name, 24, TimeUnit.HOURS);
        } catch (Exception e) {
            logger.warn("Error guardando nombre de mensajero en Redis: {}", e.getMessage());
        }
    }
}
