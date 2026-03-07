package app.adapter.out.tracking;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import java.util.Optional;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import app.domain.model.LiveTracking;
import app.domain.model.Location;
import app.domain.model.TrackingHistory;
import app.domain.model.enums.TrackingSource;
import app.domain.model.enums.TrackingStatus;
import app.infrastructure.persistence.entities.TrackingHistoryEntity;
import app.infrastructure.persistence.repository.TrackingHistoryRepository;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

@ExtendWith(MockitoExtension.class)
@DisplayName("TrackingAdapter Unit Tests")
/**
 * Clase de pruebas unitarias para el adaptador de seguimiento.
 */
class TrackingAdapterTest {

    @Mock
    private RedisTemplate<String, LiveTracking> redisTemplate;

    @Mock
    private ValueOperations<String, LiveTracking> valueOperations;

    @Mock
    private TrackingHistoryRepository trackingHistoryRepository;

    @Mock
    private TrackingMapper trackingMapper;

    @InjectMocks
    private TrackingAdapter trackingAdapter;

    @Test
    @DisplayName("Debe guardar ubicación en vivo en Redis")
    /**
     * Verifica que la ubicación en tiempo real se guarde en caché (Redis).
     */
    void shouldSaveLiveLocation() {
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);

        Location location = new Location(4.6097, -74.0817);
        LiveTracking tracking = new LiveTracking();
        tracking.setMessengerId(1L);
        tracking.setCurrentLocation(location);
        tracking.setStatus(TrackingStatus.ACTIVE);

        trackingAdapter.saveLiveLocation(tracking);

        verify(valueOperations).set(anyString(), eq(tracking), eq(90L), eq(TimeUnit.SECONDS));
    }

    @Test
    @DisplayName("Debe obtener última ubicación de Redis")
    /**
     * Verifica la recuperación de la última ubicación conocida desde caché.
     */
    void shouldGetLastLocation() {
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);

        LiveTracking tracking = new LiveTracking();
        tracking.setMessengerId(1L);

        when(valueOperations.get(anyString())).thenReturn(tracking);

        Optional<LiveTracking> result = trackingAdapter.getLastLocation(1L);

        assertTrue(result.isPresent());
        assertEquals(1L, result.get().getMessengerId());
    }

    @Test
    @DisplayName("Debe guardar historial de tracking")
    /**
     * Verifica la persistencia histórica de ubicaciones en base de datos.
     */
    void shouldSaveTrackingHistory() {
        Location location = new Location(4.6097, -74.0817);
        TrackingHistory history = new TrackingHistory(1L, location, TrackingSource.GPS);

        TrackingHistoryEntity entity = new TrackingHistoryEntity();

        when(trackingMapper.toEntity(history)).thenReturn(entity);
        when(trackingHistoryRepository.save(any(TrackingHistoryEntity.class))).thenReturn(entity);

        trackingAdapter.saveTrackingHistory(history);

        verify(trackingHistoryRepository).save(any(TrackingHistoryEntity.class));
    }
}
